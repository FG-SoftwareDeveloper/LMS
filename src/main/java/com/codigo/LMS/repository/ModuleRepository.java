package com.codigo.LMS.repository;

import com.codigo.LMS.entity.Module;
import com.codigo.LMS.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    
    List<Module> findByCourse(Course course);
    
    List<Module> findByCourseOrderByOrderIndexAsc(Course course);
    
    Optional<Module> findByCourseAndOrderIndex(Course course, Integer orderIndex);
    
    @Query("SELECT m FROM Module m WHERE m.course.id = :courseId ORDER BY m.orderIndex ASC")
    List<Module> findByCourseIdOrderByOrderIndex(@Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(m) FROM Module m WHERE m.course = :course")
    long countByCourse(@Param("course") Course course);
    
    @Query("SELECT m FROM Module m WHERE m.course = :course AND m.orderIndex > :orderIndex ORDER BY m.orderIndex ASC")
    List<Module> findNextModules(@Param("course") Course course, @Param("orderIndex") Integer orderIndex);
    
    @Query("SELECT m FROM Module m WHERE m.course = :course AND m.orderIndex < :orderIndex ORDER BY m.orderIndex DESC")
    List<Module> findPreviousModules(@Param("course") Course course, @Param("orderIndex") Integer orderIndex);
}