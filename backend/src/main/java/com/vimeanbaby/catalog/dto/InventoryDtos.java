package com.vimeanbaby.catalog.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class InventoryDtos {

    private InventoryDtos() {
    }

    public record InventoryItemResponse(
            Long variantId,
            Long productId,
            String productNameEn,
            String productNameKm,
            String imageUrl,
            String sku,
            String optionName,
            BigDecimal price,
            Integer stockQty,
            LocalDate expiryDate,
            boolean active
    ) {
    }

    public record StockUpdateRequest(@NotNull @Min(0) @Max(1_000_000) Integer stockQty) {
    }
}
