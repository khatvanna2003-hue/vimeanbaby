package com.vimeanbaby.catalog.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class CatalogDtos {

    private CatalogDtos() {
    }

    public record CategoryResponse(
            Long id,
            String nameKm,
            String nameEn,
            String slug,
            String imageUrl,
            Integer sortOrder
    ) {
    }

    public record BrandResponse(
            Long id,
            String name,
            String slug,
            String logoUrl
    ) {
    }

    public record ProductImageResponse(
            Long id,
            String url,
            Integer sortOrder,
            boolean primary
    ) {
    }

    public record ProductVariantResponse(
            Long id,
            String sku,
            String optionName,
            BigDecimal price,
            BigDecimal compareAtPrice,
            Integer stockQty,
            LocalDate expiryDate
    ) {
    }

    public record ProductSummaryResponse(
            Long id,
            String nameKm,
            String nameEn,
            String slug,
            String brandName,
            String categorySlug,
            String primaryImageUrl,
            BigDecimal price,
            BigDecimal compareAtPrice,
            Integer stockQty,
            boolean featured,
            String ageRange
    ) {
    }

    public record ProductDetailResponse(
            Long id,
            String nameKm,
            String nameEn,
            String slug,
            String descriptionKm,
            String descriptionEn,
            String ageRange,
            String originCountry,
            boolean featured,
            CategoryResponse category,
            BrandResponse brand,
            List<ProductImageResponse> images,
            List<ProductVariantResponse> variants
    ) {
    }

    public record CategoryRequest(
            String nameKm,
            String nameEn,
            String slug,
            String imageUrl,
            Integer sortOrder,
            Boolean active
    ) {
    }

    public record BrandRequest(
            String name,
            String slug,
            String logoUrl,
            Boolean active
    ) {
    }

    public record ProductImageRequest(
            String url,
            String cloudinaryPublicId,
            Integer sortOrder,
            Boolean primary
    ) {
    }

    public record ProductVariantRequest(
            String sku,
            String optionName,
            BigDecimal price,
            BigDecimal compareAtPrice,
            Integer stockQty,
            LocalDate expiryDate,
            Boolean active
    ) {
    }

    public record ProductRequest(
            Long categoryId,
            Long brandId,
            String nameKm,
            String nameEn,
            String slug,
            String descriptionKm,
            String descriptionEn,
            String ageRange,
            String originCountry,
            Boolean featured,
            Boolean active,
            List<ProductImageRequest> images,
            List<ProductVariantRequest> variants
    ) {
    }
}
