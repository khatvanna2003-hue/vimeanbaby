package com.vimeanbaby.dashboard.controller;

import com.vimeanbaby.common.ApiResponse;
import com.vimeanbaby.dashboard.dto.DashboardResponse;
import com.vimeanbaby.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ApiResponse<DashboardResponse> summary() {
        return ApiResponse.ok(dashboardService.summary());
    }
}
