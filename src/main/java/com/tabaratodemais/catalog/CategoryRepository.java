package com.tabaratodemais.catalog;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("select c from Category c left join fetch c.parent order by lower(c.name), c.id")
    List<Category> findAllWithParent();
    Optional<Category> findBySlug(String slug);
}
