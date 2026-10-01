package com.vimeanbaby.user.controller;

import com.vimeanbaby.common.ApiResponse;
import com.vimeanbaby.security.AuthUser;
import com.vimeanbaby.user.dto.AccountDtos.ChangePasswordRequest;
import com.vimeanbaby.user.dto.AccountDtos.UpdateProfileRequest;
import com.vimeanbaby.user.dto.AuthDtos.AuthResponse;
import com.vimeanbaby.user.dto.UserProfileResponse;
import com.vimeanbaby.user.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ApiResponse<UserProfileResponse> me(@AuthenticationPrincipal AuthUser user) {
        return ApiResponse.ok(accountService.getProfile(user.id()));
    }

    @PutMapping
    public ApiResponse<UserProfileResponse> update(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ApiResponse.ok(accountService.updateProfile(user.id(), request), "Profile updated");
    }

    @PutMapping("/password")
    public ApiResponse<AuthResponse> changePassword(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        return ApiResponse.ok(accountService.changePassword(user.id(), request), "Password changed");
    }

    @PostMapping("/logout-all")
    public ApiResponse<Void> logoutAll(@AuthenticationPrincipal AuthUser user) {
        accountService.logoutEverywhere(user.id());
        return ApiResponse.ok(null, "Signed out of all devices");
    }
}
