package com.tabaratodemais.catalog;
import jakarta.persistence.*;
import com.tabaratodemais.shared.BaseEntity;
@Entity
@Table(name = "categories")

public class Category extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, unique = true, length = 140)
    private String slug;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "parent_id")
    private Category parent;
    protected Category() {}
    public Category(String name, String slug, Category parent) {
        this.name = name; this.slug = slug; this.parent = parent;
    }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public Category getParent() { return parent; }
    public void update(String name, String slug, Category parent) {
        this.name = name; this.slug = slug; this.parent = parent;
    }
}
