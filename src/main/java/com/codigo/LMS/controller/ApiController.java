package com.codigo.LMS.controller;

import com.codigo.LMS.entity.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    
    @GetMapping("/user/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(@AuthenticationPrincipal User user) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("email", user.getEmail());
        profile.put("fullName", user.getFullName());
        profile.put("role", user.getRole().name());
        profile.put("totalPoints", user.getTotalPoints());
        profile.put("currentStreak", user.getCurrentStreak());
        profile.put("longestStreak", user.getLongestStreak());
        profile.put("memberSince", user.getCreatedAt());
        
        return ResponseEntity.ok(profile);
    }
    
    @PutMapping("/user/profile")
    public ResponseEntity<Map<String, String>> updateUserProfile(@AuthenticationPrincipal User user, 
                                                               @RequestBody Map<String, String> updates) {
        try {
            if (updates.containsKey("firstName")) {
                user.setFirstName(updates.get("firstName"));
            }
            if (updates.containsKey("lastName")) {
                user.setLastName(updates.get("lastName"));
            }
            if (updates.containsKey("email")) {
                user.setEmail(updates.get("email"));
            }
            
            // In a real application, save to database here
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Profile updated successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to update profile: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/courses")
    public ResponseEntity<List<Map<String, Object>>> getCourses(@RequestParam(required = false) String category,
                                                              @RequestParam(required = false) String difficulty,
                                                              @RequestParam(required = false) String search) {
        List<Course> courses = createSampleCourses();
        List<Map<String, Object>> response = new ArrayList<>();
        
        for (Course course : courses) {
            Map<String, Object> courseData = new HashMap<>();
            courseData.put("id", course.getId());
            courseData.put("title", course.getTitle());
            courseData.put("description", course.getDescription());
            courseData.put("category", course.getCategory());
            courseData.put("difficulty", course.getDifficultyLevel().name());
            courseData.put("price", course.getPrice());
            courseData.put("enrollmentCount", course.getEnrollmentCount());
            courseData.put("rating", course.getRating());
            courseData.put("thumbnailUrl", course.getThumbnailUrl());
            courseData.put("instructor", course.getInstructor().getFullName());
            courseData.put("isPublished", course.getIsPublished());
            
            response.add(courseData);
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/courses/{id}")
    public ResponseEntity<Map<String, Object>> getCourseDetails(@PathVariable Long id) {
        Course course = findCourseById(id);
        if (course == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Course not found");
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> courseData = new HashMap<>();
        courseData.put("id", course.getId());
        courseData.put("title", course.getTitle());
        courseData.put("description", course.getDescription());
        courseData.put("category", course.getCategory());
        courseData.put("difficulty", course.getDifficultyLevel().name());
        courseData.put("price", course.getPrice());
        courseData.put("enrollmentCount", course.getEnrollmentCount());
        courseData.put("rating", course.getRating());
        courseData.put("thumbnailUrl", course.getThumbnailUrl());
        courseData.put("instructor", course.getInstructor().getFullName());
        courseData.put("modules", course.getModules().size());
        courseData.put("createdAt", course.getCreatedAt());
        
        return ResponseEntity.ok(courseData);
    }
    
    @PostMapping("/courses/{id}/enroll")
    public ResponseEntity<Map<String, String>> enrollInCourse(@PathVariable Long id, 
                                                            @AuthenticationPrincipal User user) {
        Course course = findCourseById(id);
        Map<String, String> response = new HashMap<>();
        
        if (course == null) {
            response.put("status", "error");
            response.put("message", "Course not found");
            return ResponseEntity.notFound().build();
        }
        
        // Check if already enrolled
        boolean alreadyEnrolled = user.getEnrollments().stream()
            .anyMatch(enrollment -> enrollment.getCourse().getId().equals(id));
        
        if (alreadyEnrolled) {
            response.put("status", "error");
            response.put("message", "Already enrolled in this course");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Create enrollment
        Enrollment enrollment = new Enrollment(user, course);
        user.getEnrollments().add(enrollment);
        user.addPoints(10);
        user.recordActivity();
        
        response.put("status", "success");
        response.put("message", "Successfully enrolled in course");
        response.put("pointsEarned", "10");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/progress")
    public ResponseEntity<Map<String, Object>> getUserProgress(@AuthenticationPrincipal User user) {
        Map<String, Object> progressData = new HashMap<>();
        
        // Overall statistics
        progressData.put("totalPoints", user.getTotalPoints());
        progressData.put("currentStreak", user.getCurrentStreak());
        progressData.put("longestStreak", user.getLongestStreak());
        progressData.put("totalEnrollments", user.getEnrollments().size());
        progressData.put("totalBadges", user.getBadges().size());
        progressData.put("totalAchievements", user.getAchievements().size());
        
        // Enrollment progress
        List<Map<String, Object>> enrollmentProgress = new ArrayList<>();
        for (Enrollment enrollment : user.getEnrollments()) {
            Map<String, Object> enrollmentData = new HashMap<>();
            enrollmentData.put("courseId", enrollment.getCourse().getId());
            enrollmentData.put("courseTitle", enrollment.getCourse().getTitle());
            enrollmentData.put("progressPercentage", enrollment.getProgressPercentage());
            enrollmentData.put("status", enrollment.getStatus().name());
            enrollmentData.put("pointsEarned", enrollment.getTotalPoints());
            enrollmentData.put("enrolledAt", enrollment.getEnrolledAt());
            enrollmentData.put("lastAccessedAt", enrollment.getLastAccessedAt());
            
            enrollmentProgress.add(enrollmentData);
        }
        progressData.put("enrollments", enrollmentProgress);
        
        return ResponseEntity.ok(progressData);
    }
    
    @GetMapping("/user/achievements")
    public ResponseEntity<List<Map<String, Object>>> getUserAchievements(@AuthenticationPrincipal User user) {
        List<Map<String, Object>> achievements = new ArrayList<>();
        
        for (UserAchievement userAchievement : user.getAchievements()) {
            Map<String, Object> achievementData = new HashMap<>();
            Achievement achievement = userAchievement.getAchievement();
            
            achievementData.put("id", achievement.getId());
            achievementData.put("name", achievement.getName());
            achievementData.put("description", achievement.getDescription());
            achievementData.put("iconUrl", achievement.getIconUrl());
            achievementData.put("type", achievement.getAchievementType().name());
            achievementData.put("earnedAt", userAchievement.getEarnedAt());
            achievementData.put("isDisplayed", userAchievement.getIsDisplayed());
            
            achievements.add(achievementData);
        }
        
        return ResponseEntity.ok(achievements);
    }
    
    @GetMapping("/user/badges")
    public ResponseEntity<List<Map<String, Object>>> getUserBadges(@AuthenticationPrincipal User user) {
        List<Map<String, Object>> badges = new ArrayList<>();
        
        for (UserBadge userBadge : user.getBadges()) {
            Map<String, Object> badgeData = new HashMap<>();
            Badge badge = userBadge.getBadge();
            
            badgeData.put("id", badge.getId());
            badgeData.put("name", badge.getName());
            badgeData.put("description", badge.getDescription());
            badgeData.put("iconUrl", badge.getIconUrl());
            badgeData.put("colorCode", badge.getColorCode());
            badgeData.put("tier", badge.getBadgeTier().name());
            badgeData.put("category", badge.getBadgeCategory().name());
            badgeData.put("earnedAt", userBadge.getEarnedAt());
            badgeData.put("earnedReason", userBadge.getEarnedReason());
            
            badges.add(badgeData);
        }
        
        return ResponseEntity.ok(badges);
    }
    
    @PostMapping("/user/activity")
    public ResponseEntity<Map<String, String>> recordActivity(@AuthenticationPrincipal User user, 
                                                            @RequestBody Map<String, Object> activityData) {
        try {
            user.recordActivity();
            
            // Award points based on activity type
            String activityType = (String) activityData.get("type");
            int points = getPointsForActivity(activityType);
            if (points > 0) {
                user.addPoints(points);
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Activity recorded successfully");
            response.put("pointsEarned", String.valueOf(points));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to record activity: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/leaderboard")
    public ResponseEntity<List<Map<String, Object>>> getLeaderboard(@RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> leaderboard = new ArrayList<>();
        
        // Sample leaderboard data
        String[] usernames = {"alice_j", "bob_s", "carol_d", "david_w", "emma_b"};
        int[] points = {2850, 2640, 2480, 2350, 2200};
        int[] courses = {15, 12, 14, 11, 13};
        
        for (int i = 0; i < Math.min(usernames.length, limit); i++) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("rank", i + 1);
            entry.put("username", usernames[i]);
            entry.put("points", points[i]);
            entry.put("coursesCompleted", courses[i]);
            
            leaderboard.add(entry);
        }
        
        return ResponseEntity.ok(leaderboard);
    }
    
    // Helper methods
    private int getPointsForActivity(String activityType) {
        switch (activityType.toLowerCase()) {
            case "lesson_view": return 5;
            case "lesson_complete": return 25;
            case "quiz_complete": return 15;
            case "course_complete": return 100;
            case "daily_login": return 10;
            default: return 0;
        }
    }
    
    private Course findCourseById(Long id) {
        List<Course> courses = createSampleCourses();
        return courses.stream()
            .filter(course -> course.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    private List<Course> createSampleCourses() {
        List<Course> courses = new ArrayList<>();
        
        User instructor = new User("instructor", "instructor@example.com", "password", "John", "Instructor", Role.INSTRUCTOR);
        instructor.setId(2L);
        
        Course course1 = new Course("Java Fundamentals", "Learn the basics of Java programming", instructor);
        course1.setId(1L);
        course1.setPrice(new BigDecimal("99.99"));
        course1.setDifficultyLevel(Course.DifficultyLevel.BEGINNER);
        course1.setImageUrl("/images/java-course.jpg");
        course1.setDurationHours(40);
        course1.setIsPublished(true);
        course1.setEnrollmentCount(125);
        course1.setRating(4.5);
        course1.setCategory("Programming");
        courses.add(course1);
        
        Course course2 = new Course("Advanced Spring Boot", "Master Spring Boot framework development", instructor);
        course2.setId(2L);
        course2.setPrice(new BigDecimal("149.99"));
        course2.setDifficultyLevel(Course.DifficultyLevel.ADVANCED);
        course2.setImageUrl("/images/spring-course.jpg");
        course2.setDurationHours(60);
        course2.setIsPublished(true);
        course2.setEnrollmentCount(87);
        course2.setRating(4.8);
        course2.setCategory("Framework");
        courses.add(course2);
        
        Course course3 = new Course("Introduction to Programming", "Learn programming fundamentals", instructor);
        course3.setId(3L);
        course3.setPrice(BigDecimal.ZERO); // Free course
        course3.setDifficultyLevel(Course.DifficultyLevel.BEGINNER);
        course3.setImageUrl("/images/programming-course.jpg");
        course3.setDurationHours(30);
        course3.setIsPublished(true);
        course3.setEnrollmentCount(245);
        course3.setRating(4.3);
        course3.setCategory("Programming");
        courses.add(course3);
        
        Course course4 = new Course("JavaScript & Web Development", "Build modern web applications", instructor);
        course4.setId(4L);
        course4.setPrice(new BigDecimal("79.99"));
        course4.setDifficultyLevel(Course.DifficultyLevel.INTERMEDIATE);
        course4.setImageUrl("/images/javascript-course.jpg");
        course4.setDurationHours(45);
        course4.setIsPublished(true);
        course4.setEnrollmentCount(189);
        course4.setRating(4.6);
        course4.setCategory("Web Development");
        courses.add(course4);
        
        Course course5 = new Course("Cybersecurity Fundamentals", "Learn security best practices", instructor);
        course5.setId(5L);
        course5.setPrice(new BigDecimal("129.99"));
        course5.setDifficultyLevel(Course.DifficultyLevel.INTERMEDIATE);
        course5.setImageUrl("/images/security-course.jpg");
        course5.setDurationHours(35);
        course5.setIsPublished(true);
        course5.setEnrollmentCount(76);
        course5.setRating(4.7);
        course5.setCategory("Security");
        courses.add(course5);
        
        Course course6 = new Course("Mobile App Development", "Create mobile applications", instructor);
        course6.setId(6L);
        course6.setPrice(new BigDecimal("159.99"));
        course6.setDifficultyLevel(Course.DifficultyLevel.ADVANCED);
        course6.setImageUrl("/images/mobile-course.jpg");
        course6.setDurationHours(55);
        course6.setIsPublished(true);
        course6.setEnrollmentCount(98);
        course6.setRating(4.4);
        course6.setCategory("Mobile Development");
        courses.add(course6);
        
        Course course7 = new Course("UI/UX Design Principles", "Master user interface design", instructor);
        course7.setId(7L);
        course7.setPrice(BigDecimal.ZERO); // Free course
        course7.setDifficultyLevel(Course.DifficultyLevel.BEGINNER);
        course7.setImageUrl("/images/design-course.jpg");
        course7.setDurationHours(25);
        course7.setIsPublished(true);
        course7.setEnrollmentCount(156);
        course7.setRating(4.2);
        course7.setCategory("Design");
        courses.add(course7);
        
        Course course8 = new Course("Project Management", "Learn project management methodologies", instructor);
        course8.setId(8L);
        course8.setPrice(new BigDecimal("89.99"));
        course8.setDifficultyLevel(Course.DifficultyLevel.INTERMEDIATE);
        course8.setImageUrl("/images/pm-course.jpg");
        course8.setDurationHours(40);
        course8.setIsPublished(true);
        course8.setEnrollmentCount(112);
        course8.setRating(4.5);
        course8.setCategory("Management");
        courses.add(course8);
        
        return courses;
    }
    
    @PostMapping("/enroll")
    public ResponseEntity<Map<String, Object>> enrollInCourse(@AuthenticationPrincipal User user,
                                                            @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long courseId = Long.valueOf(request.get("courseId").toString());
            Course course = findCourseById(courseId);
            
            if (course == null) {
                response.put("success", false);
                response.put("message", "Course not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }
            
            // Check if course is free or requires payment
            if (course.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                // Paid course - return payment URL
                response.put("success", false);
                response.put("requiresPayment", true);
                response.put("paymentUrl", "/payment/course/" + courseId);
                response.put("price", course.getPrice());
                response.put("message", "Payment required for this course");
                return ResponseEntity.ok(response);
            }
            
            // Free course - process enrollment immediately
            // In a real application, you would save enrollment to database
            response.put("success", true);
            response.put("message", "Successfully enrolled in " + course.getTitle());
            response.put("courseId", courseId);
            response.put("courseName", course.getTitle());
            response.put("enrollmentDate", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Enrollment failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}