package com.tabaratodemais.offer;
import org.springframework.data.jpa.repository.JpaRepository;
public interface StoreRepository extends JpaRepository<Store, Long> {
    java.util.Optional<Store> findBySlug(String slug);
}
