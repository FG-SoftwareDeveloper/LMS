package com.codigo.LMS.repository;

import com.codigo.LMS.entity.UserAchievement;
import com.codigo.LMS.entity.User;
import com.codigo.LMS.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    
    List<UserAchievement> findByUser(User user);
    
    List<UserAchievement> findByUserOrderByEarnedAtDesc(User user);
    
    Optional<UserAchievement> findByUserAndAchievement(User user, Achievement achievement);
    
    boolean existsByUserAndAchievement(User user, Achievement achievement);
    
    @Query("SELECT ua FROM UserAchievement ua WHERE ua.user.id = :userId ORDER BY ua.earnedAt DESC")
    List<UserAchievement> findRecentAchievementsByUser(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(ua) FROM UserAchievement ua WHERE ua.user = :user")
    long countByUser(@Param("user") User user);
    
    @Query("SELECT ua FROM UserAchievement ua WHERE ua.earnedAt BETWEEN :startDate AND :endDate")
    List<UserAchievement> findByEarnedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT ua.achievement.id, COUNT(ua) FROM UserAchievement ua GROUP BY ua.achievement.id ORDER BY COUNT(ua) DESC")
    List<Object[]> findMostEarnedAchievements();
    
    @Query("SELECT COUNT(ua) FROM UserAchievement ua WHERE ua.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}