package com.tabaratodemais.offer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
public interface OfferRepository extends JpaRepository<Offer, Long> {
    @Query("select o from Offer o join fetch o.store join fetch o.product p join fetch p.category where o.featured = true and o.active = true and p.status = com.tabaratodemais.catalog.ProductStatus.PUBLISHED order by o.lastCheckedAt desc, o.id desc")
    List<Offer> findFeatured(org.springframework.data.domain.Pageable pageable);
    @Query("select o from Offer o join fetch o.store where o.product.id = :productId order by o.price, o.id")
    List<Offer> findForProduct(Long productId);
    @Query("select o from Offer o join fetch o.store join fetch o.product p join fetch p.category where o.active = true and p.status = com.tabaratodemais.catalog.ProductStatus.PUBLISHED and p.id in :productIds order by o.price, o.id")
    List<Offer> findPublicForProducts(List<Long> productIds);
}
