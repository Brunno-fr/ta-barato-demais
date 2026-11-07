package com.tabaratodemais.offer;
import jakarta.persistence.*;
import com.tabaratodemais.shared.BaseEntity;
@Entity
@Table(name = "offers")

public class Offer extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "product_id")
    private com.tabaratodemais.catalog.Product product;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "store_id")
    private Store store;
    @Column(nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal price;
    @Column(precision = 12, scale = 2)
    private java.math.BigDecimal previousPrice;
    @Column(length = 2048)
    private String previousPriceEvidence;
    @Column(nullable = false, length = 2048)
    private String affiliateUrl;
    @Column(precision = 12, scale = 2)
    private java.math.BigDecimal shippingCost;
    @Column(columnDefinition = "text")
    private String purchaseConditions;
    @Column(nullable = false)
    private java.time.Instant lastCheckedAt;
    @Column(nullable = false)
    private boolean active;
    @Column(nullable = false)
    private boolean featured;
    @Version
    private long version;
    protected Offer() {}
    public Offer(com.tabaratodemais.catalog.Product product, Store store, java.math.BigDecimal price,
                 String affiliateUrl, java.time.Instant checkedAt) {
        this.product = java.util.Objects.requireNonNull(product);
        this.store = java.util.Objects.requireNonNull(store);
        var uri = java.net.URI.create(affiliateUrl);
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null)
            throw new IllegalArgumentException("Link de afiliado deve ser uma URL HTTPS válida.");
        this.affiliateUrl = affiliateUrl;
        updatePrice(price, checkedAt);
    }
    public void updatePrice(java.math.BigDecimal price, java.time.Instant checkedAt) {
        if (price == null || price.signum() <= 0)
            throw new IllegalArgumentException("Preço deve ser maior que zero.");
        this.price = price.setScale(2, java.math.RoundingMode.UNNECESSARY);
        this.lastCheckedAt = java.util.Objects.requireNonNull(checkedAt);
    }
    public com.tabaratodemais.catalog.Product getProduct() { return product; }
    public Store getStore() { return store; }
    public java.math.BigDecimal getPrice() { return price; }
    public java.math.BigDecimal getPreviousPrice() { return previousPrice; }
    public String getPreviousPriceEvidence() { return previousPriceEvidence; }
    public String getAffiliateUrl() { return affiliateUrl; }
    public java.math.BigDecimal getShippingCost() { return shippingCost; }
    public String getPurchaseConditions() { return purchaseConditions; }
    public java.time.Instant getLastCheckedAt() { return lastCheckedAt; }
    public boolean getActive() { return active; }
    public boolean getFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }
    public long getVersion() { return version; }
    public void update(Store store, java.math.BigDecimal price, String affiliateUrl,
                       java.math.BigDecimal shippingCost, String conditions, java.time.Instant checkedAt, boolean active) {
        this.store = store; this.affiliateUrl = affiliateUrl; this.shippingCost = shippingCost;
        this.purchaseConditions = conditions; this.active = active;
        updatePrice(price, checkedAt);
    }
}
