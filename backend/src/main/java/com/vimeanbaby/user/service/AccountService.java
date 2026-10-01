package com.vimeanbaby.user.service;

import com.vimeanbaby.exception.BadRequestException;
import com.vimeanbaby.exception.ConflictException;
import com.vimeanbaby.exception.ResourceNotFoundException;
import com.vimeanbaby.user.dto.AccountDtos.ChangePasswordRequest;
import com.vimeanbaby.user.dto.AccountDtos.UpdateProfileRequest;
import com.vimeanbaby.user.dto.AuthDtos.AuthResponse;
import com.vimeanbaby.user.dto.UserProfileResponse;
import com.vimeanbaby.user.entity.User;
import com.vimeanbaby.user.mapper.UserMapper;
import com.vimeanbaby.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        return userMapper.toProfile(load(userId));
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = load(userId);
        String email = AuthService.normalizeEmail(request.email());
        String phone = AuthService.requireValidPhone(request.phone());

        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, userId)) {
            throw new ConflictException("Email is already registered", "EMAIL_TAKEN");
        }
        if (userRepository.existsByPhoneAndIdNot(phone, userId)) {
            throw new ConflictException("Phone number is already registered", "PHONE_TAKEN");
        }

        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPhone(phone);
        user.setDateOfBirth(request.dateOfBirth());
        user.setGender(request.gender());
        return userMapper.toProfile(userRepository.saveAndFlush(user));
    }

    /**
     * Changes the password and bumps the token version, which signs out every other device.
     * Returns fresh tokens so the current session stays signed in.
     */
    @Transactional
    public AuthResponse changePassword(Long userId, ChangePasswordRequest request) {
        User user = load(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect", "WRONG_PASSWORD");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must differ from the current one", "SAME_PASSWORD");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        return authService.issueTokens(userRepository.saveAndFlush(user));
    }

    /** Signs out every device by invalidating all issued tokens. */
    @Transactional
    public void logoutEverywhere(Long userId) {
        User user = load(userId);
        user.setTokenVersion(user.getTokenVersion() + 1);
    }

    private User load(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
