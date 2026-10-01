package com.vimeanbaby.catalog.repository;

import com.vimeanbaby.catalog.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT COUNT(p) FROM Product p WHERE p.deletedAt IS NULL")
    long countNotDeleted();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true AND p.deletedAt IS NULL")
    long countPublished();

    @Query("""
            SELECT c.nameEn, COUNT(p) FROM Product p JOIN p.category c
            WHERE p.deletedAt IS NULL
            GROUP BY c.id, c.nameEn
            ORDER BY COUNT(p) DESC
            """)
    List<Object[]> countByCategory();

    @Query("""
            SELECT p FROM Product p
            LEFT JOIN FETCH p.category
            WHERE p.deletedAt IS NULL
            ORDER BY p.createdAt DESC, p.id DESC
            """)
    List<Product> findRecent(Pageable pageable);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
