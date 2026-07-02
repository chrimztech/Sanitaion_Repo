package com.unza.ubirr.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ContentTagId implements Serializable {
  @Column(name = "content_item_id")
  public UUID contentItemId;

  @Column(name = "tag_id")
  public UUID tagId;

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof ContentTagId other)) {
      return false;
    }
    return Objects.equals(contentItemId, other.contentItemId) && Objects.equals(tagId, other.tagId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(contentItemId, tagId);
  }
}
