package com.vimeanbaby.catalog.controller;

import com.vimeanbaby.catalog.dto.CatalogDtos.BrandResponse;
import com.vimeanbaby.catalog.dto.CatalogDtos.CategoryResponse;
import com.vimeanbaby.catalog.dto.CatalogDtos.ProductDetailResponse;
import com.vimeanbaby.catalog.dto.CatalogDtos.ProductSummaryResponse;
import com.vimeanbaby.catalog.service.CatalogService;
import com.vimeanbaby.common.ApiResponse;
import com.vimeanbaby.common.PageResponse;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicCatalogController {

    private final CatalogService catalogService;

    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> categories() {
        return ApiResponse.ok(catalogService.listPublicCategories());
    }

    @GetMapping("/brands")
    public ApiResponse<List<BrandResponse>> brands() {
        return ApiResponse.ok(catalogService.listPublicBrands());
    }

    @GetMapping("/products")
    public ApiResponse<PageResponse<ProductSummaryResponse>> products(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String age,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return ApiResponse.ok(catalogService.searchProducts(
                category, brand, q, minPrice, maxPrice, age, sort, page, size
        ));
    }

    @GetMapping("/products/{slug}")
    public ApiResponse<ProductDetailResponse> product(@PathVariable String slug) {
        return ApiResponse.ok(catalogService.getPublicProduct(slug));
    }
}
