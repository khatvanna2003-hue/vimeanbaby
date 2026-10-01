package com.vimeanbaby.catalog.repository;

import com.vimeanbaby.catalog.entity.Brand;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    List<Brand> findByActiveTrueOrderByNameAsc();

    Optional<Brand> findBySlugAndActiveTrue(String slug);

    long countByActiveTrue();

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
