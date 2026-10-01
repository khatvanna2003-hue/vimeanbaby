package com.vimeanbaby.catalog.controller;

import com.vimeanbaby.catalog.dto.CatalogDtos.ProductDetailResponse;
import com.vimeanbaby.catalog.dto.CatalogDtos.ProductRequest;
import com.vimeanbaby.catalog.dto.CatalogDtos.ProductSummaryResponse;
import com.vimeanbaby.catalog.service.CatalogService;
import com.vimeanbaby.common.ApiResponse;
import com.vimeanbaby.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final CatalogService catalogService;

    @GetMapping
    public ApiResponse<PageResponse<ProductSummaryResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(catalogService.listAdminProducts(q, categoryId, brandId, status, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(catalogService.getAdminProduct(id));
    }

    @PostMapping
    public ApiResponse<ProductDetailResponse> create(@RequestBody ProductRequest request) {
        return ApiResponse.ok(catalogService.createProduct(request), "Product created");
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductDetailResponse> update(@PathVariable Long id, @RequestBody ProductRequest request) {
        return ApiResponse.ok(catalogService.updateProduct(id, request), "Product updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        catalogService.deleteProduct(id);
        return ApiResponse.ok(null, "Product deactivated");
    }
}
