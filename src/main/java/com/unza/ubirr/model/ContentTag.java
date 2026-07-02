package com.unza.ubirr.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "content_tags")
public class ContentTag {
  @EmbeddedId
  public ContentTagId id = new ContentTagId();

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("contentItemId")
  @JoinColumn(name = "content_item_id")
  public ContentItem contentItem;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("tagId")
  @JoinColumn(name = "tag_id")
  public Tag tag;
}
