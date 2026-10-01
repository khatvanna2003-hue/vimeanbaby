package com.vimeanbaby.catalog.repository;

import com.vimeanbaby.catalog.entity.ProductVariant;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long>,
        JpaSpecificationExecutor<ProductVariant> {
    Optional<ProductVariant> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    @Query("""
            SELECT COUNT(v) FROM ProductVariant v JOIN v.product p
            WHERE v.active = true AND p.deletedAt IS NULL
            """)
    long countSellable();

    @Query("""
            SELECT COALESCE(SUM(v.stockQty), 0) FROM ProductVariant v JOIN v.product p
            WHERE v.active = true AND p.deletedAt IS NULL
            """)
    long sumStockUnits();

    @Query("""
            SELECT COALESCE(SUM(v.price * v.stockQty), 0) FROM ProductVariant v JOIN v.product p
            WHERE v.active = true AND p.deletedAt IS NULL
            """)
    BigDecimal sumInventoryValue();

    @Query("""
            SELECT COUNT(v) FROM ProductVariant v JOIN v.product p
            WHERE v.active = true AND p.deletedAt IS NULL AND v.stockQty <= :threshold AND v.stockQty > 0
            """)
    long countLowStock(@Param("threshold") int threshold);

    @Query("""
            SELECT COUNT(v) FROM ProductVariant v JOIN v.product p
            WHERE v.active = true AND p.deletedAt IS NULL AND v.stockQty <= 0
            """)
    long countOutOfStock();

    @Query("""
            SELECT COUNT(v) FROM ProductVariant v JOIN v.product p
            WHERE v.active = true AND p.deletedAt IS NULL AND v.stockQty > 0
              AND v.expiryDate IS NOT NULL AND v.expiryDate <= :before
            """)
    long countExpiringBefore(@Param("before") LocalDate before);

    @Query("""
            SELECT v FROM ProductVariant v JOIN FETCH v.product p
            WHERE v.active = true AND p.deletedAt IS NULL AND v.stockQty <= :threshold
            ORDER BY v.stockQty ASC, v.id ASC
            """)
    List<ProductVariant> findLowStock(@Param("threshold") int threshold, Pageable pageable);

    @Query("""
            SELECT v FROM ProductVariant v JOIN FETCH v.product p
            WHERE v.active = true AND p.deletedAt IS NULL AND v.stockQty > 0
              AND v.expiryDate IS NOT NULL AND v.expiryDate <= :before
            ORDER BY v.expiryDate ASC, v.id ASC
            """)
    List<ProductVariant> findExpiringBefore(@Param("before") LocalDate before, Pageable pageable);
}
