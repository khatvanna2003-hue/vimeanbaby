package com.vimeanbaby.catalog.controller;

import com.vimeanbaby.catalog.dto.InventoryDtos.InventoryItemResponse;
import com.vimeanbaby.catalog.dto.InventoryDtos.StockUpdateRequest;
import com.vimeanbaby.catalog.service.InventoryService;
import com.vimeanbaby.common.ApiResponse;
import com.vimeanbaby.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
public class AdminInventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ApiResponse<PageResponse<InventoryItemResponse>> list(
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(inventoryService.list(filter, q, page, size));
    }

    @PatchMapping("/variants/{id}/stock")
    public ApiResponse<InventoryItemResponse> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request
    ) {
        return ApiResponse.ok(inventoryService.updateStock(id, request.stockQty()), "Stock updated");
    }
}
