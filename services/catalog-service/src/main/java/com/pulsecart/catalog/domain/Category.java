package com.pulsecart.catalog.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    @Column(name = "slug", length = 120, nullable = false, unique = true)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "parent_id", length = 36)
    private String parentId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Category() {}

    public Category(String name, String slug, String description, String parentId) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.slug = slug != null && !slug.isBlank() ? slug : name.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        this.description = description;
        this.parentId = parentId;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public String getParentId() { return parentId; }
    public Instant getCreatedAt() { return createdAt; }
}
