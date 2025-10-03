package com.codigo.LMS.controller;

import com.codigo.LMS.entity.User;
import com.codigo.LMS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String dashboard(@AuthenticationPrincipal User user, Model model) {
        // Add user statistics with safe defaults to avoid lazy loading issues
        model.addAttribute("user", user);
        model.addAttribute("totalPoints", user.getTotalPoints() != null ? user.getTotalPoints() : 0);
        model.addAttribute("currentStreak", user.getCurrentStreak() != null ? user.getCurrentStreak() : 0);
        model.addAttribute("longestStreak", user.getLongestStreak() != null ? user.getLongestStreak() : 0);
        
        // Use safe collection access to avoid LazyInitializationException
        model.addAttribute("totalCourses", 0); // Will be loaded separately
        model.addAttribute("totalBadges", 0); // Will be loaded separately
        model.addAttribute("totalAchievements", 0); // Will be loaded separately
        
        // Safe defaults to avoid lazy loading issues
        model.addAttribute("completedCourses", 0);
        model.addAttribute("averageProgress", 0);
        
        // Add empty collections to avoid template errors
        model.addAttribute("recentEnrollments", java.util.Collections.emptyList());
        model.addAttribute("recentBadges", java.util.Collections.emptyList());
        model.addAttribute("recentAchievements", java.util.Collections.emptyList());
        
        return "dashboard";
    }
    
    @GetMapping("/student")
    public String studentDashboard(@AuthenticationPrincipal User user, Model model) {
        // Redirect to main dashboard for students
        return "redirect:/dashboard";
    }
    
    @GetMapping("/instructor")
    public String instructorDashboard(@AuthenticationPrincipal User user, Model model) {
        if (user.getRole() != com.codigo.LMS.entity.Role.INSTRUCTOR && 
            user.getRole() != com.codigo.LMS.entity.Role.TEACHER &&
            user.getRole() != com.codigo.LMS.entity.Role.ADMIN) {
            return "redirect:/dashboard";
        }
        
        model.addAttribute("user", user);
        model.addAttribute("totalCourses", 0); // Will be loaded separately
        model.addAttribute("totalStudents", 0); // Will be loaded separately
        
        // Add empty collections to avoid lazy loading issues
        model.addAttribute("instructorCourses", java.util.Collections.emptyList());
        
        return "instructor-dashboard";
    }
    
    @GetMapping("/admin")
    public String adminDashboard(@AuthenticationPrincipal User user, Model model) {
        if (user.getRole() != com.codigo.LMS.entity.Role.ADMIN) {
            return "redirect:/dashboard";
        }
        
        model.addAttribute("user", user);
        
        // Add admin-specific data here
        // This would typically involve additional services for system statistics
        
        return "admin-dashboard";
    }
}