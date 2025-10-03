package com.codigo.LMS.repository;

import com.codigo.LMS.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    
    List<Achievement> findByIsActiveTrue();
    
    Optional<Achievement> findByName(String name);
    
    List<Achievement> findByAchievementType(Achievement.AchievementType achievementType);
    
    List<Achievement> findByAchievementTypeAndIsActiveTrue(Achievement.AchievementType achievementType);
    
    @Query("SELECT a FROM Achievement a WHERE a.pointsRequired <= :points AND a.isActive = true")
    List<Achievement> findAchievableByPoints(@Param("points") int points);
    
    @Query("SELECT a FROM Achievement a WHERE a.isActive = true ORDER BY a.pointsRequired ASC")
    List<Achievement> findAllActiveOrderByPointsRequired();
    
    @Query("SELECT DISTINCT a.achievementType FROM Achievement a WHERE a.isActive = true")
    List<Achievement.AchievementType> findDistinctAchievementTypes();
}