package com.codigo.LMS.repository;

import com.codigo.LMS.entity.UserProgress;
import com.codigo.LMS.entity.User;
import com.codigo.LMS.entity.Course;
import com.codigo.LMS.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    
    List<UserProgress> findByUser(User user);
    
    @Query("SELECT up FROM UserProgress up WHERE up.user = :user AND up.lesson.module.course = :course")
    List<UserProgress> findByUserAndCourse(@Param("user") User user, @Param("course") Course course);
    
    Optional<UserProgress> findByUserAndLesson(User user, Lesson lesson);
    
    @Query("SELECT up FROM UserProgress up WHERE up.user = :user AND up.lesson.module.course = :course ORDER BY up.lastAccessedAt DESC")
    List<UserProgress> findByUserAndCourseOrderByLastAccessed(@Param("user") User user, @Param("course") Course course);
    
    @Query("SELECT AVG(up.completionPercentage) FROM UserProgress up WHERE up.user = :user AND up.lesson.module.course = :course")
    Double calculateAverageProgressForUserAndCourse(@Param("user") User user, @Param("course") Course course);
    
    @Query("SELECT COUNT(up) FROM UserProgress up WHERE up.user = :user AND up.completedAt IS NOT NULL")
    long countCompletedLessonsByUser(@Param("user") User user);
    
    @Query("SELECT up FROM UserProgress up WHERE up.user = :user AND up.lastAccessedAt >= :date")
    List<UserProgress> findRecentProgressByUser(@Param("user") User user, @Param("date") LocalDateTime date);
    
    @Query("SELECT up.lesson.module.course.id, AVG(up.completionPercentage) FROM UserProgress up WHERE up.user = :user GROUP BY up.lesson.module.course.id")
    List<Object[]> findCourseProgressByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(DISTINCT up.user) FROM UserProgress up WHERE up.lesson.module.course = :course AND up.completedAt IS NOT NULL")
    long countCompletedStudentsForCourse(@Param("course") Course course);
}