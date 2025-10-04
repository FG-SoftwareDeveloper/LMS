package com.codigo.LMS.repository;

import com.codigo.LMS.entity.User;
import com.codigo.LMS.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    List<User> findByRoleOrderByCreatedAtDesc(Role role);
    List<User> findTop10ByOrderByIdDesc();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") Role role);
    
    @Query("SELECT u FROM User u ORDER BY u.totalPoints DESC")
    List<User> findTopUsersByPoints(@Param("limit") int limit);
}