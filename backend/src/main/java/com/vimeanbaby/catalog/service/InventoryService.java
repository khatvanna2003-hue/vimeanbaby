package com.vimeanbaby.catalog.service;

import com.vimeanbaby.catalog.dto.InventoryDtos.InventoryItemResponse;
import com.vimeanbaby.catalog.entity.Product;
import com.vimeanbaby.catalog.entity.ProductImage;
import com.vimeanbaby.catalog.entity.ProductVariant;
import com.vimeanbaby.catalog.repository.ProductVariantRepository;
import com.vimeanbaby.common.PageResponse;
import com.vimeanbaby.exception.ResourceNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final ProductVariantRepository variantRepository;

    @Value("${app.inventory.low-stock-threshold:5}")
    private int lowStockThreshold;

    @Value("${app.inventory.expiry-warning-days:60}")
    private int expiryWarningDays;

    /** {@code filter}: all | low | out | expiring. Only variants of non-deleted products are listed. */
    public PageResponse<InventoryItemResponse> list(String filter, String q, int page, int size) {
        String mode = StringUtils.hasText(filter) ? filter.trim() : "all";
        Specification<ProductVariant> spec = (root, query, cb) -> {
            Join<ProductVariant, Product> product = root.join("product");
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(product.get("deletedAt")));
            switch (mode) {
                case "low" -> {
                    predicates.add(cb.isTrue(root.get("active")));
                    predicates.add(cb.greaterThan(root.get("stockQty"), 0));
                    predicates.add(cb.lessThanOrEqualTo(root.get("stockQty"), lowStockThreshold));
                }
                case "out" -> {
                    predicates.add(cb.isTrue(root.get("active")));
                    predicates.add(cb.lessThanOrEqualTo(root.get("stockQty"), 0));
                }
                case "expiring" -> {
                    predicates.add(cb.isTrue(root.get("active")));
                    predicates.add(cb.greaterThan(root.get("stockQty"), 0));
                    predicates.add(cb.isNotNull(root.get("expiryDate")));
                    predicates.add(cb.lessThanOrEqualTo(root.get("expiryDate"), expiryCutoff()));
                }
                default -> {
                }
            }
            if (StringUtils.hasText(q)) {
                String like = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("sku")), like),
                        cb.like(cb.lower(root.get("optionName")), like),
                        cb.like(cb.lower(product.get("nameEn")), like),
                        cb.like(cb.lower(product.get("nameKm")), like)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Sort sort = switch (mode) {
            case "expiring" -> Sort.by(Sort.Order.asc("expiryDate"), Sort.Order.asc("id"));
            case "low", "out" -> Sort.by(Sort.Order.asc("stockQty"), Sort.Order.asc("id"));
            default -> Sort.by(Sort.Order.desc("id"));
        };
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), sort);
        Page<ProductVariant> result = variantRepository.findAll(spec, pageable);
        return new PageResponse<>(
                result.getContent().stream().map(InventoryService::toItem).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional
    public InventoryItemResponse updateStock(Long variantId, int stockQty) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));
        variant.setStockQty(stockQty);
        return toItem(variant);
    }

    public int lowStockThreshold() {
        return lowStockThreshold;
    }

    public int expiryWarningDays() {
        return expiryWarningDays;
    }

    public LocalDate expiryCutoff() {
        return LocalDate.now().plusDays(expiryWarningDays);
    }

    public static InventoryItemResponse toItem(ProductVariant variant) {
        Product product = variant.getProduct();
        String image = product.getImages().stream()
                .sorted(Comparator.comparing((ProductImage i) -> !Boolean.TRUE.equals(i.getPrimary()))
                        .thenComparing(ProductImage::getSortOrder))
                .map(ProductImage::getUrl)
                .findFirst()
                .orElse(null);
        return new InventoryItemResponse(
                variant.getId(),
                product.getId(),
                product.getNameEn(),
                product.getNameKm(),
                image,
                variant.getSku(),
                variant.getOptionName(),
                variant.getPrice(),
                variant.getStockQty(),
                variant.getExpiryDate(),
                Boolean.TRUE.equals(variant.getActive()) && Boolean.TRUE.equals(product.getActive())
        );
    }
}
