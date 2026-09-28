package com.vimeanbaby.catalog.repository;

import com.vimeanbaby.catalog.entity.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Query("""
            SELECT p FROM Product p
            LEFT JOIN FETCH p.brand
            LEFT JOIN FETCH p.category
            WHERE p.slug = :slug AND p.active = true AND p.deletedAt IS NULL
            """)
    Optional<Product> findPublicBySlug(@Param("slug") String slug);

    @Query("""
            SELECT p FROM Product p
            LEFT JOIN FETCH p.brand
            LEFT JOIN FETCH p.category
            WHERE p.id = :id
            """)
    Optional<Product> findDetailedById(@Param("id") Long id);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
