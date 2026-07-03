package com.unza.ubirr.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Runs once on startup to fix database constraints that Hibernate's ddl-auto:update
 * cannot modify automatically (e.g. CHECK constraints on enum columns).
 */
@Component
@Order(1)
public class SchemaUpdateRunner implements ApplicationRunner {

  private final JdbcTemplate jdbcTemplate;

  public SchemaUpdateRunner(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void run(ApplicationArguments args) {
    fixAuthTypeConstraint();
    setupFullTextSearch();
    fixPublishedDraftAccessLevels();
  }

  private void fixAuthTypeConstraint() {
    jdbcTemplate.execute("""
        DO $$
        BEGIN
            IF EXISTS (
                SELECT 1 FROM pg_constraint c JOIN pg_class t ON c.conrelid = t.oid
                WHERE  c.conname = 'users_auth_type_check' AND t.relname = 'users'
            ) THEN
                ALTER TABLE users DROP CONSTRAINT users_auth_type_check;
            END IF;
            IF NOT EXISTS (
                SELECT 1 FROM pg_constraint c JOIN pg_class t ON c.conrelid = t.oid
                WHERE  c.conname = 'users_auth_type_check' AND t.relname = 'users'
            ) THEN
                ALTER TABLE users ADD CONSTRAINT users_auth_type_check
                    CHECK (auth_type IN ('LOCAL', 'LDAP', 'OAP', 'HR'));
            END IF;
        END $$;
        """);
  }

  private void setupFullTextSearch() {
    // Column (Hibernate ddl-auto:update may already have added it; IF NOT EXISTS is safe)
    jdbcTemplate.execute(
        "ALTER TABLE content_items ADD COLUMN IF NOT EXISTS search_vector tsvector");

    // GIN index for sub-millisecond FTS lookups
    jdbcTemplate.execute(
        "CREATE INDEX IF NOT EXISTS idx_ci_search ON content_items USING GIN(search_vector)");

    // Trigger function — CREATE OR REPLACE so it stays up-to-date on restart
    jdbcTemplate.execute("""
        CREATE OR REPLACE FUNCTION update_content_search_vector()
        RETURNS trigger AS $$
        BEGIN
          NEW.search_vector :=
            setweight(to_tsvector('english', coalesce(NEW.title,    '')), 'A') ||
            setweight(to_tsvector('english', coalesce(NEW.summary,  '')), 'B') ||
            setweight(to_tsvector('english', coalesce(NEW.keywords, '')), 'B') ||
            setweight(to_tsvector('english', coalesce(NEW.authors,  '')), 'C') ||
            setweight(to_tsvector('english', coalesce(NEW.body,     '')), 'D');
          RETURN NEW;
        END;
        $$ LANGUAGE plpgsql
        """);

    // Recreate trigger (DROP IF EXISTS + CREATE is idempotent)
    jdbcTemplate.execute(
        "DROP TRIGGER IF EXISTS content_search_vector_update ON content_items");
    jdbcTemplate.execute("""
        CREATE TRIGGER content_search_vector_update
          BEFORE INSERT OR UPDATE ON content_items
          FOR EACH ROW EXECUTE FUNCTION update_content_search_vector()
        """);

    // Backfill any rows that predate this migration
    jdbcTemplate.execute("""
        UPDATE content_items
        SET search_vector =
            setweight(to_tsvector('english', coalesce(title,    '')), 'A') ||
            setweight(to_tsvector('english', coalesce(summary,  '')), 'B') ||
            setweight(to_tsvector('english', coalesce(keywords, '')), 'B') ||
            setweight(to_tsvector('english', coalesce(authors,  '')), 'C') ||
            setweight(to_tsvector('english', coalesce(body,     '')), 'D')
        WHERE search_vector IS NULL
        """);
  }

  private void fixPublishedDraftAccessLevels() {
    jdbcTemplate.execute("""
        UPDATE content_items
        SET access_level = 'PUBLIC'
        WHERE status = 'PUBLISHED' AND access_level = 'DRAFT'
        """);
  }
}
