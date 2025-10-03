package com.codigo.LMS.service;

import com.codigo.LMS.entity.User;

public interface GameService {
    
    // Points system
    void awardEnrollmentPoints(Long userId, Long courseId);
    void awardCompletionPoints(Long userId, Long courseId);
    void awardMilestonePoints(Long userId, Long courseId, String milestone);
    void awardStreakPoints(Long userId, int streakDays);
    
    // Badge system
    void checkAndAwardBadges(Long userId);
    void awardSpecificBadge(Long userId, String badgeType);
    void awardMultipleEnrollmentBadge(Long userId, int enrollmentCount);
    void awardCompletionBadge(Long userId, Long courseId);
    
    // Achievements
    void checkAchievements(Long userId);
    void unlockAchievement(Long userId, String achievementType);
    
    // Leaderboard
    void updateLeaderboard(Long userId);
    java.util.List<User> getTopStudentsByPoints(int limit);
    java.util.List<User> getTopStudentsByEnrollments(int limit);
    
    // Streaks
    void updateLoginStreak(Long userId);
    void updateLearningStreak(Long userId);
    int getCurrentStreak(Long userId);
    
    // Game mechanics
    void processLevelUp(Long userId);
    void checkSpecialEvents(Long userId);
    void awardRandomBonus(Long userId);
    
    // Analytics
    int getTotalPointsByUser(Long userId);
    int getUserRank(Long userId);
    java.util.Map<String, Object> getUserGameStats(Long userId);
}