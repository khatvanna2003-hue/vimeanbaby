package com.vimeanbaby.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    public static final String PHONE_REGEX = "^\\+?[0-9 ()-]{8,20}$";
    public static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$";

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Size(min = 2, max = 150) String fullName,
            @NotBlank @Email @Size(max = 180) String email,
            @NotBlank @Pattern(regexp = PHONE_REGEX) String phone,
            @NotBlank @Pattern(regexp = PASSWORD_REGEX) String password
    ) {
    }

    /** {@code identifier} accepts either an email address or a phone number. */
    public record LoginRequest(
            @NotBlank @Size(max = 180) String identifier,
            @NotBlank @Size(max = 72) String password
    ) {
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            long expiresIn,
            UserProfileResponse user
    ) {
    }
}
