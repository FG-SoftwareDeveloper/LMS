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
        // Create a test user if none exists
        if (userRepository.count() == 0) {
            User testUser = new User();
            testUser.setUsername("testuser");
            testUser.setEmail("test@example.com");
            testUser.setPassword(passwordEncoder.encode("password123"));
            testUser.setFirstName("Test");
            testUser.setLastName("User");
            testUser.setRole(Role.STUDENT);
            
            userRepository.save(testUser);
            System.out.println("Test user created: username=testuser, password=password123");
        }
    }
}