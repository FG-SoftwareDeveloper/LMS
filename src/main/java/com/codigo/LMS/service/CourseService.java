package com.codigo.LMS.service;

import com.codigo.LMS.entity.Course;
import com.codigo.LMS.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface CourseService {
    
    Course findById(Long courseId);
    List<Course> findAll();
    List<Course> findByCategory(String category);
    List<Course> findPublishedCourses();
    List<Course> findByInstructor(Long instructorId);
    
    Course save(Course course);
    void delete(Long courseId);
    
    // Search and filtering
    List<Course> searchCourses(String searchTerm);
    List<Course> findFreeCourses();
    List<Course> findPaidCourses();
    List<Course> findByPriceRange(java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice);
    
    // Enrollment related
    List<Course> findOpenForEnrollment();
    List<Course> findCoursesAtCapacity();
    List<Course> findCoursesNearCapacity();
    
    // User management
    User getCurrentUser(UserDetails userDetails);
    User findUserByUsername(String username);
    User findUserById(Long userId);
    
    // Analytics
    List<Course> getMostPopularCourses();
    double getAverageRating();
    long getFreeCourseCount();
    long getPaidCourseCount();
    
    // Categories
    List<String> getAllCategories();
}