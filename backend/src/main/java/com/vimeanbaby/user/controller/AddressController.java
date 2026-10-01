package com.vimeanbaby.user.controller;

import com.vimeanbaby.common.ApiResponse;
import com.vimeanbaby.security.AuthUser;
import com.vimeanbaby.user.dto.AccountDtos.AddressRequest;
import com.vimeanbaby.user.dto.AccountDtos.AddressResponse;
import com.vimeanbaby.user.service.AddressService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ApiResponse<List<AddressResponse>> list(@AuthenticationPrincipal AuthUser user) {
        return ApiResponse.ok(addressService.list(user.id()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AddressResponse> create(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody AddressRequest request
    ) {
        return ApiResponse.ok(addressService.create(user.id(), request), "Address saved");
    }

    @PutMapping("/{id}")
    public ApiResponse<AddressResponse> update(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request
    ) {
        return ApiResponse.ok(addressService.update(user.id(), id, request), "Address updated");
    }

    @PatchMapping("/{id}/default")
    public ApiResponse<AddressResponse> setDefault(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return ApiResponse.ok(addressService.setDefault(user.id(), id), "Default address updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        addressService.delete(user.id(), id);
        return ApiResponse.ok(null, "Address deleted");
    }
}
