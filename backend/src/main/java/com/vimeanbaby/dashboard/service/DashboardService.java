package com.vimeanbaby.dashboard.service;

import com.vimeanbaby.catalog.repository.BrandRepository;
import com.vimeanbaby.catalog.repository.CategoryRepository;
import com.vimeanbaby.catalog.repository.ProductRepository;
import com.vimeanbaby.catalog.repository.ProductVariantRepository;
import com.vimeanbaby.catalog.service.InventoryService;
import com.vimeanbaby.dashboard.dto.DashboardResponse;
import com.vimeanbaby.dashboard.dto.DashboardResponse.CatalogStats;
import com.vimeanbaby.dashboard.dto.DashboardResponse.CategoryCount;
import com.vimeanbaby.dashboard.dto.DashboardResponse.CustomerStats;
import com.vimeanbaby.dashboard.dto.DashboardResponse.DailyCount;
import com.vimeanbaby.dashboard.dto.DashboardResponse.InventoryStats;
import com.vimeanbaby.dashboard.dto.DashboardResponse.RecentProduct;
import com.vimeanbaby.user.entity.Role;
import com.vimeanbaby.user.mapper.UserMapper;
import com.vimeanbaby.user.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    static final ZoneId STORE_ZONE = ZoneId.of("Asia/Phnom_Penh");
    static final int SIGNUP_DAYS = 14;
    private static final int LIST_SIZE = 6;

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final UserMapper userMapper;

    public DashboardResponse summary() {
        long products = productRepository.countNotDeleted();
        long published = productRepository.countPublished();
        var catalog = new CatalogStats(products, published, products - published,
                categoryRepository.countByActiveTrue(), brandRepository.countByActiveTrue());

        int threshold = inventoryService.lowStockThreshold();
        LocalDate expiryCutoff = inventoryService.expiryCutoff();
        var inventory = new InventoryStats(
                variantRepository.countSellable(),
                variantRepository.sumStockUnits(),
                variantRepository.sumInventoryValue(),
                variantRepository.countLowStock(threshold),
                variantRepository.countOutOfStock(),
                variantRepository.countExpiringBefore(expiryCutoff),
                threshold,
                inventoryService.expiryWarningDays()
        );

        Instant now = Instant.now();
        var customers = new CustomerStats(
                userRepository.countByRole(Role.CUSTOMER),
                userRepository.countByRoleAndActiveTrue(Role.CUSTOMER),
                userRepository.countByRoleAndCreatedAtAfter(Role.CUSTOMER, now.minus(30, ChronoUnit.DAYS))
        );

        List<CategoryCount> byCategory = productRepository.countByCategory().stream()
                .map(row -> new CategoryCount((String) row[0], ((Number) row[1]).longValue()))
                .toList();

        var page = PageRequest.of(0, LIST_SIZE);
        return new DashboardResponse(
                catalog,
                inventory,
                customers,
                byCategory,
                signupsPerDay(LocalDate.now(STORE_ZONE)),
                variantRepository.findLowStock(threshold, page).stream().map(InventoryService::toItem).toList(),
                variantRepository.findExpiringBefore(expiryCutoff, page).stream().map(InventoryService::toItem).toList(),
                productRepository.findRecent(page).stream()
                        .map(p -> new RecentProduct(p.getId(), p.getNameEn(), p.getNameKm(),
                                p.getCategory() != null ? p.getCategory().getNameEn() : null,
                                Boolean.TRUE.equals(p.getActive()), p.getCreatedAt()))
                        .toList(),
                userRepository.findTop5ByRoleOrderByCreatedAtDesc(Role.CUSTOMER).stream()
                        .map(userMapper::toCustomerSummary)
                        .toList()
        );
    }

    /** Customer sign-ups per store-local day for the last {@value #SIGNUP_DAYS} days, oldest first, zero-filled. */
    List<DailyCount> signupsPerDay(LocalDate today) {
        LocalDate first = today.minusDays(SIGNUP_DAYS - 1L);
        Instant since = first.atStartOfDay(STORE_ZONE).toInstant();
        Map<LocalDate, Long> counts = userRepository.findSignupTimes(Role.CUSTOMER, since).stream()
                .map(instant -> instant.atZone(STORE_ZONE).toLocalDate())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        List<DailyCount> days = new ArrayList<>(SIGNUP_DAYS);
        for (int i = 0; i < SIGNUP_DAYS; i++) {
            LocalDate day = first.plusDays(i);
            days.add(new DailyCount(day, counts.getOrDefault(day, 0L)));
        }
        return days;
    }
}
