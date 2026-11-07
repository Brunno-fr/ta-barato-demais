package com.tabaratodemais.catalog;
import jakarta.persistence.*;
import com.tabaratodemais.shared.BaseEntity;
@Entity
@Table(name = "products")

public class Product extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "category_id")
    private Category category;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(nullable = false, unique = true, length = 220)
    private String slug;
    @Column(nullable = false, length = 100)
    private String brand;
    @Column(nullable = false, length = 150)
    private String model;
    @Column(nullable = false, length = 150)
    private String variant;
    @Column(nullable = false, columnDefinition = "text")
    private String description = "";
    @Column(length = 2048)
    private String imageUrl;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private ProductStatus status = ProductStatus.DRAFT;
    @Column(nullable = false, updatable = false)
    private java.time.Instant createdAt = java.time.Instant.now();
    protected Product() {}
    public Product(Category category, String name, String slug, String brand, String model, String variant) {
        this.category = category; this.name = name; this.slug = slug;
        this.brand = brand; this.model = model; this.variant = variant;
    }
    public Category getCategory() { return category; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getVariant() { return variant; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public ProductStatus getStatus() { return status; }
    public java.time.Instant getCreatedAt() { return createdAt; }
    public void update(Category category, String name, String slug, String brand, String model,
                       String variant, String description, String imageUrl, ProductStatus status) {
        this.category = category; this.name = name; this.slug = slug; this.brand = brand;
        this.model = model; this.variant = variant; this.description = description;
        this.imageUrl = imageUrl; this.status = status;
    }
}
