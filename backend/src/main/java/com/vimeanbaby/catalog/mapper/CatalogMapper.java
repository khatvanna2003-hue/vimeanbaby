package com.vimeanbaby.catalog.mapper;

import com.vimeanbaby.catalog.dto.CatalogDtos.BrandResponse;
import com.vimeanbaby.catalog.dto.CatalogDtos.CategoryResponse;
import com.vimeanbaby.catalog.dto.CatalogDtos.ProductDetailResponse;
import com.vimeanbaby.catalog.dto.CatalogDtos.ProductImageResponse;
import com.vimeanbaby.catalog.dto.CatalogDtos.ProductSummaryResponse;
import com.vimeanbaby.catalog.dto.CatalogDtos.ProductVariantResponse;
import com.vimeanbaby.catalog.entity.Brand;
import com.vimeanbaby.catalog.entity.Category;
import com.vimeanbaby.catalog.entity.Product;
import com.vimeanbaby.catalog.entity.ProductImage;
import com.vimeanbaby.catalog.entity.ProductVariant;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CatalogMapper {

    public CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getNameKm(),
                category.getNameEn(),
                category.getSlug(),
                category.getImageUrl(),
                category.getSortOrder(),
                Boolean.TRUE.equals(category.getActive())
        );
    }

    public BrandResponse toBrandResponse(Brand brand) {
        if (brand == null) {
            return null;
        }
        return new BrandResponse(
                brand.getId(),
                brand.getName(),
                brand.getSlug(),
                brand.getLogoUrl(),
                Boolean.TRUE.equals(brand.getActive())
        );
    }

    public ProductImageResponse toImageResponse(ProductImage image) {
        return new ProductImageResponse(
                image.getId(),
                image.getUrl(),
                image.getCloudinaryPublicId(),
                image.getSortOrder(),
                Boolean.TRUE.equals(image.getPrimary())
        );
    }

    public ProductVariantResponse toVariantResponse(ProductVariant variant) {
        return new ProductVariantResponse(
                variant.getId(),
                variant.getSku(),
                variant.getOptionName(),
                variant.getPrice(),
                variant.getCompareAtPrice(),
                variant.getStockQty(),
                variant.getExpiryDate(),
                Boolean.TRUE.equals(variant.getActive())
        );
    }

    public ProductSummaryResponse toSummary(Product product) {
        List<ProductVariant> activeVariants = product.getVariants().stream()
                .filter(v -> Boolean.TRUE.equals(v.getActive()))
                .toList();
        ProductVariant cheapest = activeVariants.stream()
                .min(Comparator.comparing(ProductVariant::getPrice))
                .orElse(null);
        int totalStock = activeVariants.stream()
                .mapToInt(v -> v.getStockQty() != null ? v.getStockQty() : 0)
                .sum();

        String imageUrl = product.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getPrimary()))
                .map(ProductImage::getUrl)
                .findFirst()
                .orElseGet(() -> product.getImages().stream()
                        .sorted(Comparator.comparing(ProductImage::getSortOrder))
                        .map(ProductImage::getUrl)
                        .findFirst()
                        .orElse(null));

        BigDecimal price = cheapest != null ? cheapest.getPrice() : BigDecimal.ZERO;
        BigDecimal compare = cheapest != null ? cheapest.getCompareAtPrice() : null;
        Integer stock = cheapest != null ? cheapest.getStockQty() : 0;

        return new ProductSummaryResponse(
                product.getId(),
                product.getNameKm(),
                product.getNameEn(),
                product.getSlug(),
                product.getBrand() != null ? product.getBrand().getName() : null,
                product.getCategory() != null ? product.getCategory().getSlug() : null,
                product.getCategory() != null ? product.getCategory().getNameEn() : null,
                imageUrl,
                price,
                compare,
                stock,
                totalStock,
                activeVariants.size(),
                Boolean.TRUE.equals(product.getFeatured()),
                Boolean.TRUE.equals(product.getActive()) && product.getDeletedAt() == null,
                product.getAgeRange()
        );
    }

    public ProductDetailResponse toDetail(Product product) {
        return toDetail(product, false);
    }

    /** Admin edits need inactive variants too, otherwise saving the form would silently drop them. */
    public ProductDetailResponse toDetail(Product product, boolean includeInactiveVariants) {
        List<ProductImageResponse> images = product.getImages().stream()
                .map(this::toImageResponse)
                .toList();
        List<ProductVariantResponse> variants = product.getVariants().stream()
                .filter(v -> includeInactiveVariants || Boolean.TRUE.equals(v.getActive()))
                .map(this::toVariantResponse)
                .toList();

        return new ProductDetailResponse(
                product.getId(),
                product.getNameKm(),
                product.getNameEn(),
                product.getSlug(),
                product.getDescriptionKm(),
                product.getDescriptionEn(),
                product.getAgeRange(),
                product.getOriginCountry(),
                Boolean.TRUE.equals(product.getFeatured()),
                Boolean.TRUE.equals(product.getActive()) && product.getDeletedAt() == null,
                toCategoryResponse(product.getCategory()),
                toBrandResponse(product.getBrand()),
                images,
                variants
        );
    }
}
