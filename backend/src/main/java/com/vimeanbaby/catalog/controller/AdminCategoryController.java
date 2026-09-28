package com.vimeanbaby.catalog.controller;

import com.vimeanbaby.catalog.dto.CatalogDtos.CategoryRequest;
import com.vimeanbaby.catalog.dto.CatalogDtos.CategoryResponse;
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
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CatalogService catalogService;

    @GetMapping
    public ApiResponse<List<CategoryResponse>> list() {
        return ApiResponse.ok(catalogService.listAllCategories());
    }

    @PostMapping
    public ApiResponse<CategoryResponse> create(@RequestBody CategoryRequest request) {
        return ApiResponse.ok(catalogService.createCategory(request), "Category created");
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(@PathVariable Long id, @RequestBody CategoryRequest request) {
        return ApiResponse.ok(catalogService.updateCategory(id, request), "Category updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        catalogService.deleteCategory(id);
        return ApiResponse.ok(null, "Category deactivated");
    }
}
