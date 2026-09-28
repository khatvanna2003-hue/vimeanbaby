package com.vimeanbaby.catalog.controller;

import com.vimeanbaby.catalog.dto.CatalogDtos.BrandRequest;
import com.vimeanbaby.catalog.dto.CatalogDtos.BrandResponse;
import com.vimeanbaby.catalog.service.CatalogService;
import com.vimeanbaby.common.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/brands")
@RequiredArgsConstructor
public class AdminBrandController {

    private final CatalogService catalogService;

    @GetMapping
    public ApiResponse<List<BrandResponse>> list() {
        return ApiResponse.ok(catalogService.listAllBrands());
    }

    @PostMapping
    public ApiResponse<BrandResponse> create(@RequestBody BrandRequest request) {
        return ApiResponse.ok(catalogService.createBrand(request), "Brand created");
    }

    @PutMapping("/{id}")
    public ApiResponse<BrandResponse> update(@PathVariable Long id, @RequestBody BrandRequest request) {
        return ApiResponse.ok(catalogService.updateBrand(id, request), "Brand updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        catalogService.deleteBrand(id);
        return ApiResponse.ok(null, "Brand deactivated");
    }
}
