package com.codigo.LMS.controller;

import com.codigo.LMS.entity.*;
import com.codigo.LMS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String adminDashboard(@AuthenticationPrincipal User user, Model model) {
        if (user.getRole() != Role.ADMIN) {
            return "redirect:/dashboard";
        }
        
        // System statistics
        SystemStats stats = generateSystemStats();
        model.addAttribute("stats", stats);
        
        // Recent activities
        List<ActivityLog> recentActivities = generateRecentActivities();
        model.addAttribute("recentActivities", recentActivities);
        
        return "admin/dashboard";
    }
    
    @GetMapping("/users")
    public String manageUsers(@AuthenticationPrincipal User user, Model model) {
        if (user.getRole() != Role.ADMIN) {
            return "redirect:/dashboard";
        }
        
        // Create sample users for demonstration
        List<User> users = createSampleUsers();
        model.addAttribute("users", users);
        
        return "admin/users";
    }
    
    @GetMapping("/courses")
    public String manageCourses(@AuthenticationPrincipal User user, Model model) {
        if (user.getRole() != Role.ADMIN) {
            return "redirect:/dashboard";
        }
        
        // Create sample courses for demonstration
        List<Course> courses = createSampleCourses();
        model.addAttribute("courses", courses);
        
        return "admin/courses";
    }
    
    @GetMapping("/courses/new")
    public String createCourse(@AuthenticationPrincipal User user, Model model) {
        if (user.getRole() != Role.ADMIN) {
            return "redirect:/dashboard";
        }
        
        model.addAttribute("course", new Course());
        model.addAttribute("instructors", getInstructors());
        
        return "admin/course-form";
    }
    
    @PostMapping("/courses")
    public String saveCourse(@AuthenticationPrincipal User user, @ModelAttribute Course course, 
                           RedirectAttributes redirectAttributes) {
        if (user.getRole() != Role.ADMIN) {
            return "redirect:/dashboard";
        }
        
        try {
            // In a real application, this would save to database
            course.setCreatedAt(LocalDateTime.now());
            course.setUpdatedAt(LocalDateTime.now());
            
            redirectAttributes.addFlashAttribute("success", "Course created successfully!");
            return "redirect:/admin/courses";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating course: " + e.getMessage());
            return "redirect:/admin/courses/new";
        }
    }
    
    @GetMapping("/courses/{id}/edit")
    public String editCourse(@PathVariable Long id, @AuthenticationPrincipal User user, Model model) {
        if (user.getRole() != Role.ADMIN) {
            return "redirect:/dashboard";
        }
        
        Course course = findCourseById(id);
        if (course == null) {
            return "redirect:/admin/courses";
        }
        
        model.addAttribute("course", course);
        model.addAttribute("instructors", getInstructors());
        
        return "admin/course-form";
    }
    
    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, @AuthenticationPrincipal User user,
                                  RedirectAttributes redirectAttributes) {
        if (user.getRole() != Role.ADMIN) {
            return "redirect:/dashboard";
        }
        
        try {
            // In a real application, this would update the database
            redirectAttributes.addFlashAttribute("success", "User status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating user status: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
    
    @GetMapping("/analytics")
    public String analytics(@AuthenticationPrincipal User user, Model model) {
        if (user.getRole() != Role.ADMIN) {
            return "redirect:/dashboard";
        }
        
        // Generate analytics data
        AnalyticsData analytics = generateAnalyticsData();
        model.addAttribute("analytics", analytics);
        
        return "admin/analytics";
    }
    
    @GetMapping("/settings")
    public String systemSettings(@AuthenticationPrincipal User user, Model model) {
        if (user.getRole() != Role.ADMIN) {
            return "redirect:/dashboard";
        }
        
        // Load system settings
        SystemSettings settings = loadSystemSettings();
        model.addAttribute("settings", settings);
        
        return "admin/settings";
    }
    
    @PostMapping("/settings")
    public String updateSystemSettings(@AuthenticationPrincipal User user, @ModelAttribute SystemSettings settings,
                                     RedirectAttributes redirectAttributes) {
        if (user.getRole() != Role.ADMIN) {
            return "redirect:/dashboard";
        }
        
        try {
            // Save system settings
            redirectAttributes.addFlashAttribute("success", "System settings updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating settings: " + e.getMessage());
        }
        
        return "redirect:/admin/settings";
    }
    
    // Helper methods
    private SystemStats generateSystemStats() {
        return new SystemStats(
            1250, // totalUsers
            45,   // totalCourses
            8500, // totalEnrollments
            95.5  // systemUptime
        );
    }
    
    private List<ActivityLog> generateRecentActivities() {
        List<ActivityLog> activities = new ArrayList<>();
        activities.add(new ActivityLog("User Registration", "John Doe registered", LocalDateTime.now().minusMinutes(15)));
        activities.add(new ActivityLog("Course Completion", "Alice completed Java Fundamentals", LocalDateTime.now().minusMinutes(30)));
        activities.add(new ActivityLog("New Course", "Spring Boot Advanced was published", LocalDateTime.now().minusHours(2)));
        activities.add(new ActivityLog("System Update", "Database backup completed", LocalDateTime.now().minusHours(4)));
        return activities;
    }
    
    private List<User> createSampleUsers() {
        List<User> users = new ArrayList<>();
        
        User student1 = new User("john_doe", "john@example.com", "password", "John", "Doe", Role.STUDENT);
        student1.setId(1L);
        student1.setTotalPoints(450);
        users.add(student1);
        
        User instructor1 = new User("jane_smith", "jane@example.com", "password", "Jane", "Smith", Role.INSTRUCTOR);
        instructor1.setId(2L);
        instructor1.setTotalPoints(1200);
        users.add(instructor1);
        
        User admin1 = new User("admin_user", "admin@example.com", "password", "Admin", "User", Role.ADMIN);
        admin1.setId(3L);
        admin1.setTotalPoints(0);
        users.add(admin1);
        
        return users;
    }
    
    private List<Course> createSampleCourses() {
        List<Course> courses = new ArrayList<>();
        
        User instructor = new User("instructor", "instructor@example.com", "password", "John", "Instructor", Role.INSTRUCTOR);
        instructor.setId(2L);
        
        Course course1 = new Course("Java Fundamentals", "Learn the basics of Java programming", instructor);
        course1.setId(1L);
        course1.setPrice(new BigDecimal("99.99"));
        course1.setDifficultyLevel(Course.DifficultyLevel.BEGINNER);
        course1.setIsPublished(true);
        course1.setEnrollmentCount(125);
        courses.add(course1);
        
        Course course2 = new Course("Advanced Spring Boot", "Master Spring Boot framework", instructor);
        course2.setId(2L);
        course2.setPrice(new BigDecimal("149.99"));
        course2.setDifficultyLevel(Course.DifficultyLevel.ADVANCED);
        course2.setIsPublished(true);
        course2.setEnrollmentCount(87);
        courses.add(course2);
        
        return courses;
    }
    
    private List<User> getInstructors() {
        List<User> instructors = new ArrayList<>();
        User instructor1 = new User("jane_smith", "jane@example.com", "password", "Jane", "Smith", Role.INSTRUCTOR);
        instructor1.setId(2L);
        instructors.add(instructor1);
        return instructors;
    }
    
    private Course findCourseById(Long id) {
        List<Course> courses = createSampleCourses();
        return courses.stream()
            .filter(course -> course.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    private AnalyticsData generateAnalyticsData() {
        return new AnalyticsData(
            new int[]{100, 120, 150, 180, 200, 220, 250}, // weeklyEnrollments
            new int[]{50, 65, 80, 95, 110, 125, 140},     // weeklyCompletions
            new String[]{"Java", "Spring", "Python", "React", "Node.js"}, // popularCourses
            new int[]{45, 30, 15, 7, 3} // courseEnrollments
        );
    }
    
    private SystemSettings loadSystemSettings() {
        return new SystemSettings(
            "Learning Management System",
            "admin@lms.com",
            true, // emailNotifications
            true, // maintenanceMode
            30    // sessionTimeout
        );
    }
    
    // Inner classes for data transfer
    public static class SystemStats {
        private int totalUsers;
        private int totalCourses;
        private int totalEnrollments;
        private double systemUptime;
        
        public SystemStats(int totalUsers, int totalCourses, int totalEnrollments, double systemUptime) {
            this.totalUsers = totalUsers;
            this.totalCourses = totalCourses;
            this.totalEnrollments = totalEnrollments;
            this.systemUptime = systemUptime;
        }
        
        // Getters
        public int getTotalUsers() { return totalUsers; }
        public int getTotalCourses() { return totalCourses; }
        public int getTotalEnrollments() { return totalEnrollments; }
        public double getSystemUptime() { return systemUptime; }
    }
    
    public static class ActivityLog {
        private String type;
        private String description;
        private LocalDateTime timestamp;
        
        public ActivityLog(String type, String description, LocalDateTime timestamp) {
            this.type = type;
            this.description = description;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getType() { return type; }
        public String getDescription() { return description; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
    
    public static class AnalyticsData {
        private int[] weeklyEnrollments;
        private int[] weeklyCompletions;
        private String[] popularCourses;
        private int[] courseEnrollments;
        
        public AnalyticsData(int[] weeklyEnrollments, int[] weeklyCompletions, 
                           String[] popularCourses, int[] courseEnrollments) {
            this.weeklyEnrollments = weeklyEnrollments;
            this.weeklyCompletions = weeklyCompletions;
            this.popularCourses = popularCourses;
            this.courseEnrollments = courseEnrollments;
        }
        
        // Getters
        public int[] getWeeklyEnrollments() { return weeklyEnrollments; }
        public int[] getWeeklyCompletions() { return weeklyCompletions; }
        public String[] getPopularCourses() { return popularCourses; }
        public int[] getCourseEnrollments() { return courseEnrollments; }
    }
    
    public static class SystemSettings {
        private String siteName;
        private String adminEmail;
        private boolean emailNotifications;
        private boolean maintenanceMode;
        private int sessionTimeout;
        
        public SystemSettings(String siteName, String adminEmail, boolean emailNotifications, 
                            boolean maintenanceMode, int sessionTimeout) {
            this.siteName = siteName;
            this.adminEmail = adminEmail;
            this.emailNotifications = emailNotifications;
            this.maintenanceMode = maintenanceMode;
            this.sessionTimeout = sessionTimeout;
        }
        
        // Getters and Setters
        public String getSiteName() { return siteName; }
        public void setSiteName(String siteName) { this.siteName = siteName; }
        
        public String getAdminEmail() { return adminEmail; }
        public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }
        
        public boolean isEmailNotifications() { return emailNotifications; }
        public void setEmailNotifications(boolean emailNotifications) { this.emailNotifications = emailNotifications; }
        
        public boolean isMaintenanceMode() { return maintenanceMode; }
        public void setMaintenanceMode(boolean maintenanceMode) { this.maintenanceMode = maintenanceMode; }
        
        public int getSessionTimeout() { return sessionTimeout; }
        public void setSessionTimeout(int sessionTimeout) { this.sessionTimeout = sessionTimeout; }
    }
}