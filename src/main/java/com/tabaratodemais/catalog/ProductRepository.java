package com.tabaratodemais.catalog;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProductRepository extends JpaRepository<Product, Long> {
    java.util.Optional<Product> findBySlug(String slug);
    @org.springframework.data.jpa.repository.Query("select p from Product p join fetch p.category order by p.id desc")
    java.util.List<Product> findAllWithCategory();
}
