package com.codigo.LMS.repository;

import com.codigo.LMS.entity.Course;
import com.codigo.LMS.entity.EnrollmentPolicy;
import com.codigo.LMS.entity.Course.DifficultyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    List<Course> findByInstructorId(Long instructorId);
    
    List<Course> findByCategory(String category);
    
    List<Course> findByIsPublishedTrue();
    
    List<Course> findByIsPublishedTrueOrderByCreatedAtDesc();
    
    List<Course> findByIsPublishedTrueOrderByEnrollmentCountDesc();
    
    List<Course> findByIsPublishedTrueOrderByRatingDesc();
    
    @Query("SELECT c FROM Course c WHERE c.isPublished = true AND " +
           "(c.enrollmentWindowStart IS NULL OR c.enrollmentWindowStart <= :now) AND " +
           "(c.enrollmentWindowEnd IS NULL OR c.enrollmentWindowEnd >= :now)")
    List<Course> findOpenForEnrollment(@Param("now") LocalDateTime now);
    
    @Query("SELECT c FROM Course c WHERE c.isPublished = true AND c.price = :price")
    List<Course> findByPrice(@Param("price") BigDecimal price);
    
    @Query("SELECT c FROM Course c WHERE c.isPublished = true AND c.price BETWEEN :minPrice AND :maxPrice")
    List<Course> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
    
    @Query("SELECT c FROM Course c WHERE c.isPublished = true AND c.price = 0")
    List<Course> findFreeCourses();
    
    @Query("SELECT c FROM Course c WHERE c.isPublished = true AND c.price > 0")
    List<Course> findPaidCourses();
    
    List<Course> findByEnrollmentPolicy(EnrollmentPolicy enrollmentPolicy);
    
    List<Course> findByDifficultyLevel(DifficultyLevel difficultyLevel);
    
    @Query("SELECT c FROM Course c WHERE c.isPublished = true AND " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.category) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Course> searchCourses(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT c FROM Course c WHERE c.isPublished = true AND " +
           "c.capacity IS NOT NULL AND " +
           "(SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = c.id AND e.status = 'ACTIVE') >= c.capacity")
    List<Course> findCoursesAtCapacity();
    
    @Query("SELECT c FROM Course c WHERE c.isPublished = true AND " +
           "c.capacity IS NOT NULL AND " +
           "(SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = c.id AND e.status = 'ACTIVE') >= (c.capacity * 0.8)")
    List<Course> findCoursesNearCapacity();
    
    @Query("SELECT c FROM Course c WHERE c.cohortId = :cohortId")
    List<Course> findByCohortId(@Param("cohortId") String cohortId);
    
    @Query("SELECT c FROM Course c WHERE c.id IN :courseIds")
    List<Course> findByIdIn(@Param("courseIds") List<Long> courseIds);
    
    @Query("SELECT DISTINCT c.category FROM Course c WHERE c.isPublished = true ORDER BY c.category")
    List<String> findAllCategories();
    
    @Query("SELECT c FROM Course c WHERE c.instructor.id = :instructorId AND c.isPublished = true")
    List<Course> findPublishedCoursesByInstructor(@Param("instructorId") Long instructorId);
    
    @Query("SELECT c FROM Course c WHERE c.createdAt BETWEEN :start AND :end")
    List<Course> findCoursesCreatedInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT c, COUNT(e) as enrollmentCount FROM Course c " +
           "LEFT JOIN c.enrollments e WHERE e.status = 'ACTIVE' " +
           "GROUP BY c ORDER BY enrollmentCount DESC")
    List<Object[]> findMostPopularCourses();
    
    @Query("SELECT AVG(c.rating) FROM Course c WHERE c.isPublished = true")
    Double getAverageRating();
    
    @Query("SELECT COUNT(c) FROM Course c WHERE c.isPublished = true AND c.price = 0")
    long countFreeCourses();
    
    @Query("SELECT COUNT(c) FROM Course c WHERE c.isPublished = true AND c.price > 0")
    long countPaidCourses();
}