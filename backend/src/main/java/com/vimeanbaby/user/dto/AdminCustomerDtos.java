package com.vimeanbaby.user.dto;

import com.vimeanbaby.user.dto.AccountDtos.AddressResponse;
import com.vimeanbaby.user.entity.Gender;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class AdminCustomerDtos {

    private AdminCustomerDtos() {
    }

    public record CustomerSummaryResponse(
            Long id,
            String fullName,
            String email,
            String phone,
            boolean active,
            Instant createdAt,
            Instant lastLoginAt
    ) {
    }

    public record CustomerDetailResponse(
            Long id,
            String fullName,
            String email,
            String phone,
            boolean active,
            LocalDate dateOfBirth,
            Gender gender,
            Instant createdAt,
            Instant lastLoginAt,
            List<AddressResponse> addresses
    ) {
    }

    public record CustomerStatusRequest(@NotNull Boolean active) {
    }
}
