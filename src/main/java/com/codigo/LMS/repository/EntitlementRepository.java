package com.codigo.LMS.repository;

import com.codigo.LMS.entity.Entitlement;
import com.codigo.LMS.entity.EntitlementStatus;
import com.codigo.LMS.entity.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EntitlementRepository extends JpaRepository<Entitlement, Long> {
    
    List<Entitlement> findByUserIdAndStatus(Long userId, EntitlementStatus status);
    
    List<Entitlement> findByUserIdAndResourceType(Long userId, ResourceType resourceType);
    
    Optional<Entitlement> findByUserIdAndResourceTypeAndResourceId(Long userId, ResourceType resourceType, Long resourceId);
    
    List<Entitlement> findByGrantedByEnrollmentId(Long enrollmentId);
    
    @Query("SELECT e FROM Entitlement e WHERE e.user.id = :userId AND e.resourceType = :resourceType AND " +
           "e.resourceId = :resourceId AND e.status = 'ACTIVE' AND " +
           "(e.expiresAt IS NULL OR e.expiresAt > :now)")
    Optional<Entitlement> findActiveEntitlement(@Param("userId") Long userId, 
                                               @Param("resourceType") ResourceType resourceType, 
                                               @Param("resourceId") Long resourceId,
                                               @Param("now") LocalDateTime now);
    
    @Query("SELECT e FROM Entitlement e WHERE e.user.id = :userId AND e.status = 'ACTIVE' AND " +
           "(e.expiresAt IS NULL OR e.expiresAt > :now)")
    List<Entitlement> findActiveEntitlementsByUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    @Query("SELECT e FROM Entitlement e WHERE e.expiresAt < :now AND e.status = 'ACTIVE'")
    List<Entitlement> findExpiredActiveEntitlements(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(e) FROM Entitlement e WHERE e.user.id = :userId AND e.resourceType = :resourceType AND e.status = 'ACTIVE'")
    long countActiveEntitlementsByUserAndType(@Param("userId") Long userId, @Param("resourceType") ResourceType resourceType);
    
    @Query("SELECT DISTINCT e.resourceId FROM Entitlement e WHERE e.user.id = :userId AND e.resourceType = :resourceType AND " +
           "e.status = 'ACTIVE' AND (e.expiresAt IS NULL OR e.expiresAt > :now)")
    List<Long> findAccessibleResourceIds(@Param("userId") Long userId, 
                                       @Param("resourceType") ResourceType resourceType,
                                       @Param("now") LocalDateTime now);
}