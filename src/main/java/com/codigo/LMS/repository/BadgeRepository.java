package com.codigo.LMS.repository;

import com.codigo.LMS.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    
    List<Badge> findByIsActiveTrue();
    
    Optional<Badge> findByName(String name);
    
    List<Badge> findByBadgeCategory(com.codigo.LMS.entity.Badge.BadgeCategory badgeCategory);
    
    List<Badge> findByBadgeCategoryAndIsActiveTrue(com.codigo.LMS.entity.Badge.BadgeCategory badgeCategory);
    
    @Query("SELECT b FROM Badge b WHERE b.isActive = true ORDER BY b.name ASC")
    List<Badge> findAllActiveOrderByName();        @Query("SELECT DISTINCT b.badgeCategory FROM Badge b WHERE b.isActive = true ORDER BY b.badgeCategory")
    List<com.codigo.LMS.entity.Badge.BadgeCategory> findDistinctCategories();
}