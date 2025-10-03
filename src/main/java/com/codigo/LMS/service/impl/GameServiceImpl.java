package com.codigo.LMS.service.impl;

import com.codigo.LMS.entity.User;
import com.codigo.LMS.entity.UserAchievement;
import com.codigo.LMS.entity.UserBadge;
import com.codigo.LMS.repository.UserRepository;
import com.codigo.LMS.repository.UserAchievementRepository;
import com.codigo.LMS.repository.UserBadgeRepository;
import com.codigo.LMS.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GameServiceImpl implements GameService {
    
    private static final Logger logger = LoggerFactory.getLogger(GameServiceImpl.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserAchievementRepository userAchievementRepository;
    
    @Autowired
    private UserBadgeRepository userBadgeRepository;
    
    private static final int ENROLLMENT_POINTS = 50;
    private static final int COMPLETION_POINTS = 200;
    private static final int MILESTONE_POINTS = 25;
    private static final int STREAK_BONUS_POINTS = 10;
    
    @Override
    public void awardEnrollmentPoints(Long userId, Long courseId) {
        logger.info("Awarding enrollment points to user {} for course {}", userId, courseId);
        
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setTotalPoints(user.getTotalPoints() + ENROLLMENT_POINTS);
            userRepository.save(user);
            
            checkAndAwardBadges(userId);
            checkAchievements(userId);
        }
    }
    
    @Override
    public void awardCompletionPoints(Long userId, Long courseId) {
        logger.info("Awarding completion points to user {} for course {}", userId, courseId);
        
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setTotalPoints(user.getTotalPoints() + COMPLETION_POINTS);
            user.setCoursesCompleted(user.getCoursesCompleted() + 1);
            userRepository.save(user);
            
            awardCompletionBadge(userId, courseId);
            checkAndAwardBadges(userId);
            checkAchievements(userId);
        }
    }
    
    @Override
    public void awardMilestonePoints(Long userId, Long courseId, String milestone) {
        logger.info("Awarding milestone points to user {} for course {} milestone: {}", userId, courseId, milestone);
        
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setTotalPoints(user.getTotalPoints() + MILESTONE_POINTS);
            userRepository.save(user);
        }
    }
    
    @Override
    public void awardStreakPoints(Long userId, int streakDays) {
        logger.info("Awarding streak points to user {} for {} days streak", userId, streakDays);
        
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            int bonusPoints = streakDays * STREAK_BONUS_POINTS;
            user.setTotalPoints(user.getTotalPoints() + bonusPoints);
            user.setCurrentStreak(streakDays);
            
            if (streakDays > user.getLongestStreak()) {
                user.setLongestStreak(streakDays);
            }
            
            userRepository.save(user);
            checkAndAwardBadges(userId);
        }
    }
    
    @Override
    public void checkAndAwardBadges(Long userId) {
        logger.debug("Checking badges for user {}", userId);
        
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;
        
        // Check for points-based badges
        if (user.getTotalPoints() >= 1000 && !hasBadge(userId, "POINTS_1000")) {
            awardSpecificBadge(userId, "POINTS_1000");
        }
        if (user.getTotalPoints() >= 5000 && !hasBadge(userId, "POINTS_5000")) {
            awardSpecificBadge(userId, "POINTS_5000");
        }
        
        // Check for completion badges
        if (user.getCoursesCompleted() >= 5 && !hasBadge(userId, "COURSES_5")) {
            awardSpecificBadge(userId, "COURSES_5");
        }
        if (user.getCoursesCompleted() >= 10 && !hasBadge(userId, "COURSES_10")) {
            awardSpecificBadge(userId, "COURSES_10");
        }
        
        // Check for streak badges
        if (user.getLongestStreak() >= 7 && !hasBadge(userId, "STREAK_7")) {
            awardSpecificBadge(userId, "STREAK_7");
        }
        if (user.getLongestStreak() >= 30 && !hasBadge(userId, "STREAK_30")) {
            awardSpecificBadge(userId, "STREAK_30");
        }
    }
    
    @Override
    public void awardSpecificBadge(Long userId, String badgeType) {
        logger.info("Awarding badge {} to user {}", badgeType, userId);
        
        if (!hasBadge(userId, badgeType)) {
            UserBadge userBadge = new UserBadge();
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                userBadge.setUser(user);
                // In a real implementation, you'd have a Badge entity and repository
                // userBadge.setBadge(badgeRepository.findByType(badgeType));
                userBadge.setEarnedAt(LocalDateTime.now());
                userBadgeRepository.save(userBadge);
            }
        }
    }
    
    @Override
    public void awardMultipleEnrollmentBadge(Long userId, int enrollmentCount) {
        logger.info("Checking multiple enrollment badges for user {} with {} enrollments", userId, enrollmentCount);
        
        if (enrollmentCount >= 3 && !hasBadge(userId, "ENROLLMENTS_3")) {
            awardSpecificBadge(userId, "ENROLLMENTS_3");
        }
        if (enrollmentCount >= 10 && !hasBadge(userId, "ENROLLMENTS_10")) {
            awardSpecificBadge(userId, "ENROLLMENTS_10");
        }
    }
    
    @Override
    public void awardCompletionBadge(Long userId, Long courseId) {
        logger.info("Awarding completion badge to user {} for course {}", userId, courseId);
        awardSpecificBadge(userId, "COURSE_COMPLETED_" + courseId);
    }
    
    @Override
    public void checkAchievements(Long userId) {
        logger.debug("Checking achievements for user {}", userId);
        
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;
        
        // Check for various achievements
        if (user.getTotalPoints() >= 10000 && !hasAchievement(userId, "MASTER_LEARNER")) {
            unlockAchievement(userId, "MASTER_LEARNER");
        }
        
        if (user.getCoursesCompleted() >= 25 && !hasAchievement(userId, "COURSE_MARATHON")) {
            unlockAchievement(userId, "COURSE_MARATHON");
        }
    }
    
    @Override
    public void unlockAchievement(Long userId, String achievementType) {
        logger.info("Unlocking achievement {} for user {}", achievementType, userId);
        
        if (!hasAchievement(userId, achievementType)) {
            UserAchievement userAchievement = new UserAchievement();
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                userAchievement.setUser(user);
                // In a real implementation, you'd have an Achievement entity and repository
                // userAchievement.setAchievement(achievementRepository.findByType(achievementType));
                userAchievement.setUnlockedAt(LocalDateTime.now());
                userAchievementRepository.save(userAchievement);
            }
        }
    }
    
    @Override
    public void updateLeaderboard(Long userId) {
        logger.debug("Updating leaderboard for user {}", userId);
        // In a real implementation, you might update a Redis leaderboard
        // or recalculate rankings
    }
    
    @Override
    public List<User> getTopStudentsByPoints(int limit) {
        return userRepository.findTopUsersByPoints(limit);
    }
    
    @Override
    public List<User> getTopStudentsByEnrollments(int limit) {
        // This would need a method in UserRepository to count enrollments
        return userRepository.findAll().stream()
            .sorted((u1, u2) -> Integer.compare(u2.getCoursesCompleted(), u1.getCoursesCompleted()))
            .limit(limit)
            .toList();
    }
    
    @Override
    public void updateLoginStreak(Long userId) {
        logger.debug("Updating login streak for user {}", userId);
        
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            LocalDateTime lastLogin = user.getLastLoginAt();
            LocalDateTime now = LocalDateTime.now();
            
            if (lastLogin != null && lastLogin.toLocalDate().equals(now.toLocalDate().minusDays(1))) {
                // Consecutive day login
                user.setCurrentStreak(user.getCurrentStreak() + 1);
                if (user.getCurrentStreak() > user.getLongestStreak()) {
                    user.setLongestStreak(user.getCurrentStreak());
                }
                awardStreakPoints(userId, user.getCurrentStreak());
            } else if (lastLogin == null || !lastLogin.toLocalDate().equals(now.toLocalDate())) {
                // Reset streak if not consecutive or first login today
                user.setCurrentStreak(1);
            }
            
            user.setLastLoginAt(now);
            userRepository.save(user);
        }
    }
    
    @Override
    public void updateLearningStreak(Long userId) {
        logger.debug("Updating learning streak for user {}", userId);
        // Similar to login streak but based on learning activity
        updateLoginStreak(userId); // For now, use same logic
    }
    
    @Override
    public int getCurrentStreak(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null ? user.getCurrentStreak() : 0;
    }
    
    @Override
    public void processLevelUp(Long userId) {
        logger.info("Processing level up for user {}", userId);
        
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            int newLevel = calculateLevel(user.getTotalPoints());
            if (newLevel > user.getLevel()) {
                user.setLevel(newLevel);
                userRepository.save(user);
                
                // Award level up badge or achievement
                awardSpecificBadge(userId, "LEVEL_" + newLevel);
            }
        }
    }
    
    @Override
    public void checkSpecialEvents(Long userId) {
        logger.debug("Checking special events for user {}", userId);
        // TODO: Implement special event logic
    }
    
    @Override
    public void awardRandomBonus(Long userId) {
        logger.info("Awarding random bonus to user {}", userId);
        
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            int bonusPoints = (int) (Math.random() * 50) + 10; // 10-60 points
            user.setTotalPoints(user.getTotalPoints() + bonusPoints);
            userRepository.save(user);
        }
    }
    
    @Override
    public int getTotalPointsByUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null ? user.getTotalPoints() : 0;
    }
    
    @Override
    public int getUserRank(Long userId) {
        // This would need a more sophisticated implementation
        // For now, return a placeholder
        return 1;
    }
    
    @Override
    public Map<String, Object> getUserGameStats(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        Map<String, Object> stats = new HashMap<>();
        
        if (user != null) {
            stats.put("totalPoints", user.getTotalPoints());
            stats.put("level", user.getLevel());
            stats.put("coursesCompleted", user.getCoursesCompleted());
            stats.put("currentStreak", user.getCurrentStreak());
            stats.put("longestStreak", user.getLongestStreak());
            stats.put("badgeCount", userBadgeRepository.countByUserId(userId));
            stats.put("achievementCount", userAchievementRepository.countByUserId(userId));
        }
        
        return stats;
    }
    
    // Helper methods
    private boolean hasBadge(Long userId, String badgeType) {
        // In a real implementation, check if user has specific badge
        return false; // Placeholder
    }
    
    private boolean hasAchievement(Long userId, String achievementType) {
        // In a real implementation, check if user has specific achievement
        return false; // Placeholder
    }
    
    private int calculateLevel(int totalPoints) {
        // Simple level calculation based on points
        return (totalPoints / 1000) + 1;
    }
}