package com.vimeanbaby.user.service;

import com.vimeanbaby.user.entity.Role;
import com.vimeanbaby.user.entity.User;
import com.vimeanbaby.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Creates the initial admin account from ADMIN_* env variables when it does not exist yet. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAccountSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String email;

    @Value("${app.admin.password}")
    private String password;

    @Value("${app.admin.full-name}")
    private String fullName;

    @Value("${app.admin.phone}")
    private String phone;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String normalizedEmail = AuthService.normalizeEmail(email);
        if (password == null || password.isBlank() || userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            return;
        }
        User admin = new User();
        admin.setFullName(fullName);
        admin.setEmail(normalizedEmail);
        admin.setPhone(PhoneNumbers.normalize(phone));
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        userRepository.save(admin);
        log.info("Seeded admin account {}", normalizedEmail);
    }
}
