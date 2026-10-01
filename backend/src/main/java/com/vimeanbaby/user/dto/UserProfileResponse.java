package com.vimeanbaby.user.dto;

import com.vimeanbaby.user.entity.Gender;
import com.vimeanbaby.user.entity.Role;
import java.time.Instant;
import java.time.LocalDate;

public record UserProfileResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        Role role,
        LocalDate dateOfBirth,
        Gender gender,
        Instant createdAt,
        Instant lastLoginAt
) {
}
