package com.codigo.LMS.repository;

import com.codigo.LMS.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByActorIdOrderByTimestampDesc(Long actorId);
    
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId);
    
    List<AuditLog> findByActionOrderByTimestampDesc(String action);
    
    Page<AuditLog> findByActorId(Long actorId, Pageable pageable);
    
    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :start AND :end ORDER BY a.timestamp DESC")
    List<AuditLog> findByTimestampRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT a FROM AuditLog a WHERE a.targetUserId = :userId ORDER BY a.timestamp DESC")
    List<AuditLog> findByTargetUserId(@Param("userId") Long userId);
    
    @Query("SELECT a FROM AuditLog a WHERE a.actorId = :actorId AND a.action = :action AND a.timestamp > :since")
    List<AuditLog> findRecentActionsByActor(@Param("actorId") Long actorId, 
                                           @Param("action") String action, 
                                           @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.entityType = :entityType AND a.action = :action AND a.timestamp BETWEEN :start AND :end")
    long countActionsByTypeInPeriod(@Param("entityType") String entityType, 
                                   @Param("action") String action, 
                                   @Param("start") LocalDateTime start, 
                                   @Param("end") LocalDateTime end);
    
    @Query("SELECT a FROM AuditLog a WHERE a.success = false ORDER BY a.timestamp DESC")
    List<AuditLog> findFailedActions();
    
    @Query("SELECT DISTINCT a.actorId FROM AuditLog a WHERE a.action = :action AND a.timestamp > :since")
    List<Long> findActorsWhoPerformedActionSince(@Param("action") String action, @Param("since") LocalDateTime since);
}