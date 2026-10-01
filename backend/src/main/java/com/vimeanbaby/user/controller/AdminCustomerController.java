package com.vimeanbaby.user.controller;

import com.vimeanbaby.common.ApiResponse;
import com.vimeanbaby.common.PageResponse;
import com.vimeanbaby.user.dto.AdminCustomerDtos.CustomerDetailResponse;
import com.vimeanbaby.user.dto.AdminCustomerDtos.CustomerStatusRequest;
import com.vimeanbaby.user.dto.AdminCustomerDtos.CustomerSummaryResponse;
import com.vimeanbaby.user.service.AdminCustomerService;
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
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final AdminCustomerService customerService;

    @GetMapping
    public ApiResponse<PageResponse<CustomerSummaryResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(customerService.list(q, status, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(customerService.get(id));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<CustomerDetailResponse> setStatus(
            @PathVariable Long id,
            @Valid @RequestBody CustomerStatusRequest request
    ) {
        String message = request.active() ? "Customer enabled" : "Customer disabled";
        return ApiResponse.ok(customerService.setActive(id, request.active()), message);
    }
}
