package com.codigo.LMS.repository;

import com.codigo.LMS.entity.Lesson;
import com.codigo.LMS.entity.Module;
import com.codigo.LMS.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    
    List<Lesson> findByModule(Module module);
    
    List<Lesson> findByModuleOrderByOrderIndexAsc(Module module);
    
    Optional<Lesson> findByModuleAndOrderIndex(Module module, Integer orderIndex);
    
    @Query("SELECT l FROM Lesson l WHERE l.module.id = :moduleId ORDER BY l.orderIndex ASC")
    List<Lesson> findByModuleIdOrderByOrderIndex(@Param("moduleId") Long moduleId);
    
    @Query("SELECT l FROM Lesson l WHERE l.module.course = :course ORDER BY l.module.orderIndex ASC, l.orderIndex ASC")
    List<Lesson> findByCourseOrderByModuleAndLessonIndex(@Param("course") Course course);
    
    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.module = :module")
    long countByModule(@Param("module") Module module);
    
    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.module.course = :course")
    long countByCourse(@Param("course") Course course);
    
    @Query("SELECT l FROM Lesson l WHERE l.module = :module AND l.orderIndex > :orderIndex ORDER BY l.orderIndex ASC")
    List<Lesson> findNextLessons(@Param("module") Module module, @Param("orderIndex") Integer orderIndex);
    
    @Query("SELECT l FROM Lesson l WHERE l.module = :module AND l.orderIndex < :orderIndex ORDER BY l.orderIndex DESC")
    List<Lesson> findPreviousLessons(@Param("module") Module module, @Param("orderIndex") Integer orderIndex);
}