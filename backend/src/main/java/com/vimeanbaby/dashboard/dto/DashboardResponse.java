package com.vimeanbaby.dashboard.dto;

import com.vimeanbaby.catalog.dto.InventoryDtos.InventoryItemResponse;
import com.vimeanbaby.user.dto.AdminCustomerDtos.CustomerSummaryResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record DashboardResponse(
        CatalogStats catalog,
        InventoryStats inventory,
        CustomerStats customers,
        List<CategoryCount> productsByCategory,
        List<DailyCount> signups,
        List<InventoryItemResponse> lowStockItems,
        List<InventoryItemResponse> expiringItems,
        List<RecentProduct> recentProducts,
        List<CustomerSummaryResponse> recentCustomers
) {

    public record CatalogStats(long products, long published, long drafts, long categories, long brands) {
    }

    public record InventoryStats(
            long sellableVariants,
            long stockUnits,
            BigDecimal inventoryValue,
            long lowStock,
            long outOfStock,
            long expiringSoon,
            int lowStockThreshold,
            int expiryWarningDays
    ) {
    }

    public record CustomerStats(long total, long active, long newLast30Days) {
    }

    public record CategoryCount(String name, long count) {
    }

    public record DailyCount(LocalDate date, long count) {
    }

    public record RecentProduct(Long id, String nameEn, String nameKm, String categoryName, boolean active, Instant createdAt) {
    }
}
