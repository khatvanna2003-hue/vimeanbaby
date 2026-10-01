package com.vimeanbaby.user.service;

import com.vimeanbaby.exception.BadRequestException;
import com.vimeanbaby.exception.ConflictException;
import com.vimeanbaby.exception.UnauthorizedException;
import com.vimeanbaby.security.JwtService;
import com.vimeanbaby.user.dto.AuthDtos.AuthResponse;
import com.vimeanbaby.user.dto.AuthDtos.LoginRequest;
import com.vimeanbaby.user.dto.AuthDtos.RegisterRequest;
import com.vimeanbaby.user.entity.Role;
import com.vimeanbaby.user.entity.User;
import com.vimeanbaby.user.mapper.UserMapper;
import com.vimeanbaby.user.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    /** BCrypt hash of a random value; compared against when the user does not exist to keep timing uniform. */
    private static final String DUMMY_HASH = "$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5BWX4Z7ZpXxN5bB4Q6vYI6Fq1bFxG";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        String phone = requireValidPhone(request.phone());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email is already registered", "EMAIL_TAKEN");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new ConflictException("Phone number is already registered", "PHONE_TAKEN");
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);
        user.setActive(true);
        user.setLastLoginAt(Instant.now());
        return issueTokens(userRepository.save(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Optional<User> found = findByIdentifier(request.identifier());
        String hash = found.map(User::getPasswordHash).orElse(DUMMY_HASH);
        boolean matches = passwordEncoder.matches(request.password(), hash);

        if (found.isEmpty() || !matches) {
            throw new UnauthorizedException("Invalid email/phone or password", "INVALID_CREDENTIALS");
        }
        User user = found.get();
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new UnauthorizedException("This account has been disabled", "ACCOUNT_DISABLED");
        }
        user.setLastLoginAt(Instant.now());
        return issueTokens(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(String refreshToken) {
        JwtService.TokenClaims claims;
        try {
            claims = jwtService.parse(refreshToken, JwtService.TYPE_REFRESH);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new UnauthorizedException("Session expired, please sign in again", "TOKEN_INVALID");
        }
        User user = userRepository.findById(claims.userId())
                .filter(u -> Boolean.TRUE.equals(u.getActive()))
                .filter(u -> u.getTokenVersion() == claims.tokenVersion())
                .orElseThrow(() -> new UnauthorizedException("Session expired, please sign in again", "TOKEN_INVALID"));
        return issueTokens(user);
    }

    public AuthResponse issueTokens(User user) {
        return new AuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                jwtService.getAccessExpirationMs() / 1000,
                userMapper.toProfile(user)
        );
    }

    static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    static String requireValidPhone(String raw) {
        String phone = PhoneNumbers.normalize(raw);
        if (phone == null || phone.length() < 9 || phone.length() > 10) {
            throw new BadRequestException("Phone number is invalid", "INVALID_PHONE");
        }
        return phone;
    }

    private Optional<User> findByIdentifier(String identifier) {
        String value = identifier.trim();
        if (value.contains("@")) {
            return userRepository.findByEmailIgnoreCase(normalizeEmail(value));
        }
        return userRepository.findByPhone(PhoneNumbers.normalize(value));
    }
}
