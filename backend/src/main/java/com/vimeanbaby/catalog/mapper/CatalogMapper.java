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
                category.getSortOrder()
        );
    }

    public BrandResponse toBrandResponse(Brand brand) {
        if (brand == null) {
            return null;
        }
        return new BrandResponse(brand.getId(), brand.getName(), brand.getSlug(), brand.getLogoUrl());
    }

    public ProductImageResponse toImageResponse(ProductImage image) {
        return new ProductImageResponse(
                image.getId(),
                image.getUrl(),
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
                variant.getExpiryDate()
        );
    }

    public ProductSummaryResponse toSummary(Product product) {
        ProductVariant cheapest = product.getVariants().stream()
                .filter(v -> Boolean.TRUE.equals(v.getActive()))
                .min(Comparator.comparing(ProductVariant::getPrice))
                .orElse(null);

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
                imageUrl,
                price,
                compare,
                stock,
                Boolean.TRUE.equals(product.getFeatured()),
                product.getAgeRange()
        );
    }

    public ProductDetailResponse toDetail(Product product) {
        List<ProductImageResponse> images = product.getImages().stream()
                .map(this::toImageResponse)
                .toList();
        List<ProductVariantResponse> variants = product.getVariants().stream()
                .filter(v -> Boolean.TRUE.equals(v.getActive()))
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
                toCategoryResponse(product.getCategory()),
                toBrandResponse(product.getBrand()),
                images,
                variants
        );
    }
}
