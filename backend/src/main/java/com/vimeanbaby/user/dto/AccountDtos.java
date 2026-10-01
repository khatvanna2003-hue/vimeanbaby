package com.vimeanbaby.user.dto;

import com.vimeanbaby.user.entity.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;

public final class AccountDtos {

    private AccountDtos() {
    }

    public record UpdateProfileRequest(
            @NotBlank @Size(min = 2, max = 150) String fullName,
            @NotBlank @Email @Size(max = 180) String email,
            @NotBlank @Pattern(regexp = AuthDtos.PHONE_REGEX) String phone,
            @Past LocalDate dateOfBirth,
            Gender gender
    ) {
    }

    public record ChangePasswordRequest(
            @NotBlank @Size(max = 72) String currentPassword,
            @NotBlank @Pattern(regexp = AuthDtos.PASSWORD_REGEX) String newPassword
    ) {
    }

    public record AddressRequest(
            @NotBlank @Size(max = 150) String receiverName,
            @NotBlank @Pattern(regexp = AuthDtos.PHONE_REGEX) String phone,
            @NotBlank @Size(max = 100) String province,
            @NotBlank @Size(max = 100) String district,
            @NotBlank @Size(max = 100) String commune,
            @NotBlank @Size(max = 255) String streetDetail,
            @Size(max = 500) String note,
            boolean defaultAddress
    ) {
    }

    public record AddressResponse(
            Long id,
            String receiverName,
            String phone,
            String province,
            String district,
            String commune,
            String streetDetail,
            String note,
            boolean defaultAddress,
            Instant createdAt
    ) {
    }
}
