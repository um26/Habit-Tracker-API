package com.habittracker.api.config;

import com.habittracker.api.model.User;
import com.habittracker.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Updated admin credentials
    private final String ADMIN_EMAIL = "devved@gmail.com";
    private final String ADMIN_USERNAME = "admin"; // Keeping username as 'admin' for simplicity
    private final String ADMIN_PASSWORD = "6969";   // Updated password

    @Override
    public void run(String... args) throws Exception {
        // Check if the admin user already exists by email
        if (userRepository.findByEmail(ADMIN_EMAIL).isEmpty()) {
            log.info("Admin user not found, creating admin user...");

            User adminUser = new User();
            adminUser.setEmail(ADMIN_EMAIL);
            adminUser.setUsername(ADMIN_USERNAME); // Assigns the separate username field
            adminUser.setPassword(passwordEncoder.encode(ADMIN_PASSWORD)); // Hash the password!
            adminUser.setUniqueUserId(UUID.randomUUID().toString().replaceAll("-", "").substring(0, 7)); // Generate unique ID
            adminUser.setEnabled(true); // Admin is enabled by default
            adminUser.setTwoFactorEnabled(false); // Admin starts with 2FA disabled

            userRepository.save(adminUser);
            log.info("Admin user created successfully with email: {}", ADMIN_EMAIL);
        } else {
            log.info("Admin user already exists.");
        }
    }
}