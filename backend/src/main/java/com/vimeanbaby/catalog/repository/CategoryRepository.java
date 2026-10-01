package com.vimeanbaby.catalog.repository;

import com.vimeanbaby.catalog.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByActiveTrueOrderBySortOrderAscIdAsc();

    Optional<Category> findBySlugAndActiveTrue(String slug);

    long countByActiveTrue();

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
