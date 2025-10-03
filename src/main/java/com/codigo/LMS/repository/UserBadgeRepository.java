package com.codigo.LMS.repository;

import com.codigo.LMS.entity.UserBadge;
import com.codigo.LMS.entity.User;
import com.codigo.LMS.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    
    List<UserBadge> findByUser(User user);
    
    List<UserBadge> findByUserOrderByEarnedAtDesc(User user);
    
    Optional<UserBadge> findByUserAndBadge(User user, Badge badge);
    
    boolean existsByUserAndBadge(User user, Badge badge);
    
    @Query("SELECT ub FROM UserBadge ub WHERE ub.user.id = :userId ORDER BY ub.earnedAt DESC")
    List<UserBadge> findRecentBadgesByUser(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(ub) FROM UserBadge ub WHERE ub.user = :user")
    long countByUser(@Param("user") User user);
    
    @Query("SELECT ub FROM UserBadge ub WHERE ub.earnedAt BETWEEN :startDate AND :endDate")
    List<UserBadge> findByEarnedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT ub.badge.id, COUNT(ub) FROM UserBadge ub GROUP BY ub.badge.id ORDER BY COUNT(ub) DESC")
    List<Object[]> findMostEarnedBadges();
    
    @Query("SELECT COUNT(ub) FROM UserBadge ub WHERE ub.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}