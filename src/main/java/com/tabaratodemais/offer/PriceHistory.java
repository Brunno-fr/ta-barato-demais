package com.tabaratodemais.offer;
import jakarta.persistence.*;
import com.tabaratodemais.shared.BaseEntity;
@Entity
@Table(name = "price_history")
@org.hibernate.annotations.Immutable
public class PriceHistory extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "offer_id")
    private Offer offer;
    @Column(nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal price;
    @Column(nullable = false)
    private java.time.Instant recordedAt;
    protected PriceHistory() {}

    public Offer getOffer() { return offer; }
    public java.math.BigDecimal getPrice() { return price; }
    public java.time.Instant getRecordedAt() { return recordedAt; }
}
