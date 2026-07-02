package com.unza.ubirr.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

  private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

  @Bean
  public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
    RedisCacheConfiguration defaults = base(Duration.ofMinutes(10));

    Map<String, RedisCacheConfiguration> perCache = new HashMap<>();
    perCache.put("public-home",        base(Duration.ofMinutes(5)));
    perCache.put("public-resources",   base(Duration.ofMinutes(10)));
    perCache.put("public-resource",    base(Duration.ofMinutes(30)));
    perCache.put("public-categories",  base(Duration.ofMinutes(30)));
    perCache.put("public-departments", base(Duration.ofHours(1)));
    perCache.put("public-events",      base(Duration.ofMinutes(15)));
    perCache.put("public-sites",       base(Duration.ofHours(1)));
    perCache.put("public-gallery",     base(Duration.ofMinutes(15)));

    return RedisCacheManager.builder(factory)
      .cacheDefaults(defaults)
      .withInitialCacheConfigurations(perCache)
      .build();
  }

  private RedisCacheConfiguration base(Duration ttl) {
    return RedisCacheConfiguration.defaultCacheConfig()
      .entryTtl(ttl)
      .disableCachingNullValues()
      .serializeValuesWith(RedisSerializationContext.SerializationPair
        .fromSerializer(new GenericJackson2JsonRedisSerializer()));
  }

  @Override
  public CacheErrorHandler errorHandler() {
    return new CacheErrorHandler() {
      @Override
      public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
        log.warn("Cache GET error [{}]: {}", cache.getName(), e.getMessage());
      }

      @Override
      public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
        log.warn("Cache PUT error [{}]: {}", cache.getName(), e.getMessage());
      }

      @Override
      public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
        log.warn("Cache EVICT error [{}]: {}", cache.getName(), e.getMessage());
      }

      @Override
      public void handleCacheClearError(RuntimeException e, Cache cache) {
        log.warn("Cache CLEAR error [{}]: {}", cache.getName(), e.getMessage());
      }
    };
  }
}
