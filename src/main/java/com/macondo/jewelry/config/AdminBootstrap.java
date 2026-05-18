package com.macondo.jewelry.config;

import com.macondo.jewelry.user.AppUser;
import com.macondo.jewelry.user.UserRepository;
import com.macondo.jewelry.user.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${ADMIN_EMAIL:admin@macondo.local}") String adminEmail,
            @Value("${ADMIN_PASSWORD:admin12345}") String adminPassword
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail(adminEmail.toLowerCase())) {
            userRepository.save(new AppUser("Administrador Macondo", adminEmail, passwordEncoder.encode(adminPassword), UserRole.ADMIN));
        }
    }
}
