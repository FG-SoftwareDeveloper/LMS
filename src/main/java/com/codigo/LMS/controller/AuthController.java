package com.codigo.LMS.controller;

import com.codigo.LMS.entity.User;
import com.codigo.LMS.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "logout", required = false) String logout,
                           Model model) {
        
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password!");
        }
        
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully!");
        }
        
        return "auth/login";
    }
    
    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/signup";
    }
    
    @PostMapping("/register")
    public String registerUser(@Valid User user, BindingResult result, Model model) {
        
        if (result.hasErrors()) {
            return "auth/signup";
        }
        
        try {
            // Check if username already exists
            if (userService.existsByUsername(user.getUsername())) {
                model.addAttribute("errorMessage", "Username already exists!");
                return "auth/signup";
            }
            
            // Check if email already exists
            if (userService.existsByEmail(user.getEmail())) {
                model.addAttribute("errorMessage", "Email already exists!");
                return "auth/signup";
            }
            
            userService.registerUser(user);
            model.addAttribute("successMessage", "Registration successful! Please login.");
            return "auth/login";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "auth/signup";
        }
    }
    

}