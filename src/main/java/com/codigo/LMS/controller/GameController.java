package com.codigo.LMS.controller;

import com.codigo.LMS.entity.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/gamification")
public class GameController {
    
    @GetMapping("/leaderboard")
    public String leaderboard(Model model) {
        // Create sample leaderboard data
        List<LeaderboardEntry> leaderboard = createSampleLeaderboard();
        model.addAttribute("leaderboard", leaderboard);
        return "leaderboard";
    }
    
    @GetMapping("/achievements")
    public String achievements(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        
        // Get user's achievements
        List<UserAchievement> userAchievements = new ArrayList<>(user.getAchievements());
        userAchievements.sort((a, b) -> b.getEarnedAt().compareTo(a.getEarnedAt()));
        model.addAttribute("userAchievements", userAchievements);
        
        // Get available achievements
        List<Achievement> availableAchievements = createSampleAchievements();
        List<Achievement> lockedAchievements = availableAchievements.stream()
            .filter(achievement -> user.getAchievements().stream()
                .noneMatch(ua -> ua.getAchievement().getId().equals(achievement.getId())))
            .collect(Collectors.toList());
        model.addAttribute("lockedAchievements", lockedAchievements);
        
        return "achievements";
    }
    
    @GetMapping("/badges")
    public String badges(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        
        // Get user's badges grouped by tier
        List<UserBadge> userBadges = new ArrayList<>(user.getBadges());
        userBadges.sort((a, b) -> b.getEarnedAt().compareTo(a.getEarnedAt()));
        model.addAttribute("userBadges", userBadges);
        
        // Get available badges
        List<Badge> availableBadges = createSampleBadges();
        List<Badge> lockedBadges = availableBadges.stream()
            .filter(badge -> user.getBadges().stream()
                .noneMatch(ub -> ub.getBadge().getId().equals(badge.getId())))
            .collect(Collectors.toList());
        model.addAttribute("lockedBadges", lockedBadges);
        
        return "badges";
    }
    
    @GetMapping("/progress")
    public String progress(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        
        // Calculate overall progress statistics
        List<UserProgress> progressRecords = new ArrayList<>(user.getProgressRecords());
        
        long totalLessons = progressRecords.size();
        long completedLessons = progressRecords.stream()
            .filter(p -> p.getStatus() == UserProgress.ProgressStatus.COMPLETED)
            .count();
        long inProgressLessons = progressRecords.stream()
            .filter(p -> p.getStatus() == UserProgress.ProgressStatus.IN_PROGRESS)
            .count();
        
        model.addAttribute("totalLessons", totalLessons);
        model.addAttribute("completedLessons", completedLessons);
        model.addAttribute("inProgressLessons", inProgressLessons);
        model.addAttribute("completionRate", totalLessons > 0 ? (completedLessons * 100.0 / totalLessons) : 0);
        
        // Recent progress
        List<UserProgress> recentProgress = progressRecords.stream()
            .sorted((p1, p2) -> p2.getLastAccessedAt().compareTo(p1.getLastAccessedAt()))
            .limit(10)
            .collect(Collectors.toList());
        model.addAttribute("recentProgress", recentProgress);
        
        return "progress";
    }
    
    @PostMapping("/check-achievements")
    @ResponseBody
    public List<Achievement> checkAchievements(@AuthenticationPrincipal User user) {
        List<Achievement> newAchievements = new ArrayList<>();
        
        // Check for point milestones
        if (user.getTotalPoints() >= 100 && !hasAchievement(user, "FIRST_HUNDRED")) {
            Achievement achievement = new Achievement("First Hundred", "Earned your first 100 points!", 100, Achievement.AchievementType.POINTS_MILESTONE);
            achievement.setId(1L);
            newAchievements.add(achievement);
            
            UserAchievement userAchievement = new UserAchievement(user, achievement);
            user.getAchievements().add(userAchievement);
        }
        
        // Check for course completion
        long completedCourses = user.getEnrollments().stream()
            .filter(e -> e.getStatus() == com.codigo.LMS.entity.EnrollmentStatus.COMPLETED)
            .count();
        
        if (completedCourses >= 1 && !hasAchievement(user, "COURSE_COMPLETER")) {
            Achievement achievement = new Achievement("Course Completer", "Completed your first course!", 0, Achievement.AchievementType.COURSE_COMPLETION);
            achievement.setId(2L);
            newAchievements.add(achievement);
            
            UserAchievement userAchievement = new UserAchievement(user, achievement);
            user.getAchievements().add(userAchievement);
        }
        
        return newAchievements;
    }
    
    @PostMapping("/award-badge")
    @ResponseBody
    public String awardBadge(@AuthenticationPrincipal User user, @RequestParam String badgeType) {
        Badge badge = createBadgeByType(badgeType);
        if (badge != null && !hasBadge(user, badgeType)) {
            UserBadge userBadge = new UserBadge(user, badge, "Automatically awarded");
            user.getBadges().add(userBadge);
            return "Badge awarded successfully!";
        }
        return "Badge not awarded";
    }
    
    // Helper methods
    private boolean hasAchievement(User user, String achievementName) {
        return user.getAchievements().stream()
            .anyMatch(ua -> ua.getAchievement().getName().equals(achievementName));
    }
    
    private boolean hasBadge(User user, String badgeName) {
        return user.getBadges().stream()
            .anyMatch(ub -> ub.getBadge().getName().equals(badgeName));
    }
    
    private Badge createBadgeByType(String badgeType) {
        switch (badgeType.toUpperCase()) {
            case "EARLY_BIRD":
                Badge badge = new Badge("Early Bird", "Completed a lesson before 9 AM", Badge.BadgeTier.BRONZE, Badge.BadgeCategory.PARTICIPATION);
                badge.setId(1L);
                badge.setColorCode("#CD7F32");
                return badge;
            case "NIGHT_OWL":
                Badge badge2 = new Badge("Night Owl", "Completed a lesson after 10 PM", Badge.BadgeTier.BRONZE, Badge.BadgeCategory.PARTICIPATION);
                badge2.setId(2L);
                badge2.setColorCode("#CD7F32");
                return badge2;
            default:
                return null;
        }
    }
    
    private List<LeaderboardEntry> createSampleLeaderboard() {
        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        leaderboard.add(new LeaderboardEntry(1, "Alice Johnson", 2850, 15, 12));
        leaderboard.add(new LeaderboardEntry(2, "Bob Smith", 2640, 12, 10));
        leaderboard.add(new LeaderboardEntry(3, "Carol Davis", 2480, 14, 8));
        leaderboard.add(new LeaderboardEntry(4, "David Wilson", 2350, 11, 9));
        leaderboard.add(new LeaderboardEntry(5, "Emma Brown", 2200, 13, 7));
        return leaderboard;
    }
    
    private List<Achievement> createSampleAchievements() {
        List<Achievement> achievements = new ArrayList<>();
        
        Achievement achievement1 = new Achievement("First Hundred", "Earned your first 100 points!", 100, Achievement.AchievementType.POINTS_MILESTONE);
        achievement1.setId(1L);
        achievement1.setIconUrl("/images/achievements/first-hundred.png");
        achievements.add(achievement1);
        
        Achievement achievement2 = new Achievement("Course Completer", "Completed your first course!", 0, Achievement.AchievementType.COURSE_COMPLETION);
        achievement2.setId(2L);
        achievement2.setIconUrl("/images/achievements/course-completer.png");
        achievements.add(achievement2);
        
        Achievement achievement3 = new Achievement("Streak Master", "Maintained a 7-day learning streak!", 0, Achievement.AchievementType.STREAK);
        achievement3.setId(3L);
        achievement3.setIconUrl("/images/achievements/streak-master.png");
        achievements.add(achievement3);
        
        return achievements;
    }
    
    private List<Badge> createSampleBadges() {
        List<Badge> badges = new ArrayList<>();
        
        Badge badge1 = new Badge("Early Bird", "Completed a lesson before 9 AM", Badge.BadgeTier.BRONZE, Badge.BadgeCategory.PARTICIPATION);
        badge1.setId(1L);
        badge1.setColorCode("#CD7F32");
        badges.add(badge1);
        
        Badge badge2 = new Badge("Night Owl", "Completed a lesson after 10 PM", Badge.BadgeTier.BRONZE, Badge.BadgeCategory.PARTICIPATION);
        badge2.setId(2L);
        badge2.setColorCode("#CD7F32");
        badges.add(badge2);
        
        Badge badge3 = new Badge("Java Master", "Completed all Java courses", Badge.BadgeTier.GOLD, Badge.BadgeCategory.LEARNING);
        badge3.setId(3L);
        badge3.setColorCode("#FFD700");
        badges.add(badge3);
        
        return badges;
    }
    
    // Inner class for leaderboard entries
    public static class LeaderboardEntry {
        private int rank;
        private String username;
        private int points;
        private int coursesCompleted;
        private int badges;
        
        public LeaderboardEntry(int rank, String username, int points, int coursesCompleted, int badges) {
            this.rank = rank;
            this.username = username;
            this.points = points;
            this.coursesCompleted = coursesCompleted;
            this.badges = badges;
        }
        
        // Getters
        public int getRank() { return rank; }
        public String getUsername() { return username; }
        public int getPoints() { return points; }
        public int getCoursesCompleted() { return coursesCompleted; }
        public int getBadges() { return badges; }
    }
}