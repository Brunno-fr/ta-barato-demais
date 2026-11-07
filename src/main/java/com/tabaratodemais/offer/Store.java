package com.tabaratodemais.offer;
import jakarta.persistence.*;
import com.tabaratodemais.shared.BaseEntity;
@Entity
@Table(name = "stores")

public class Store extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, unique = true, length = 100)
    private String slug;
    protected Store() {}
    public Store(String name, String slug) { this.name = name; this.slug = slug; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
}
