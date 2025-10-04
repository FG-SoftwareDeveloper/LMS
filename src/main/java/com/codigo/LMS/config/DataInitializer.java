package com.codigo.LMS.config;

import com.codigo.LMS.entity.User;
import com.codigo.LMS.entity.Role;
import com.codigo.LMS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Ensure a known demo user exists for login testing
        final String demoUsername = "demo";
        final String demoEmail = "demo@example.com";
        final String demoPassword = "password123"; // dev-only

        boolean demoExists = userRepository.existsByUsername(demoUsername) || userRepository.existsByEmail(demoEmail);
        if (!demoExists) {
            User demoUser = new User();
            demoUser.setUsername(demoUsername);
            demoUser.setEmail(demoEmail);
            demoUser.setPassword(passwordEncoder.encode(demoPassword));
            demoUser.setFirstName("Demo");
            demoUser.setLastName("User");
            demoUser.setRole(Role.STUDENT);

            userRepository.save(demoUser);
            System.out.println("Seeded demo user -> username=" + demoUsername + ", email=" + demoEmail + ", password=" + demoPassword);
        } else {
            System.out.println("Demo user already present (" + demoUsername + ", " + demoEmail + ")");
        }
    }
}