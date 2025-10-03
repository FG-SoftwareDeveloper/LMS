package com.codigo.LMS.service.impl;

import com.codigo.LMS.entity.Course;
import com.codigo.LMS.entity.User;
import com.codigo.LMS.repository.CourseRepository;
import com.codigo.LMS.repository.UserRepository;
import com.codigo.LMS.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    public Course findById(Long courseId) {
        return courseRepository.findById(courseId).orElse(null);
    }

    @Override
    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    @Override
    public List<Course> findByCategory(String category) {
        return courseRepository.findByCategory(category);
    }

    @Override
    public List<Course> findPublishedCourses() {
        return courseRepository.findByIsPublishedTrue();
    }

    @Override
    public List<Course> findByInstructor(Long instructorId) {
        return courseRepository.findByInstructorId(instructorId);
    }

    @Override
    public Course save(Course course) {
        return courseRepository.save(course);
    }

    @Override
    public void delete(Long courseId) {
        courseRepository.deleteById(courseId);
    }

    @Override
    public List<Course> searchCourses(String searchTerm) {
        return courseRepository.searchCourses(searchTerm);
    }

    @Override
    public List<Course> findFreeCourses() {
        return courseRepository.findFreeCourses();
    }

    @Override
    public List<Course> findPaidCourses() {
        return courseRepository.findPaidCourses();
    }

    @Override
    public List<Course> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return courseRepository.findByPriceRange(minPrice, maxPrice);
    }

    @Override
    public List<Course> findOpenForEnrollment() {
        return courseRepository.findOpenForEnrollment(java.time.LocalDateTime.now());
    }

    @Override
    public List<Course> findCoursesAtCapacity() {
        return courseRepository.findCoursesAtCapacity();
    }

    @Override
    public List<Course> findCoursesNearCapacity() {
        return courseRepository.findCoursesNearCapacity();
    }

    @Override
    public User getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return userRepository.findByUsername(userDetails.getUsername()).orElse(null);
    }

    @Override
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public User findUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public List<Course> getMostPopularCourses() {
        List<Object[]> results = courseRepository.findMostPopularCourses();
        return results.stream()
                .map(result -> (Course) result[0])
                .limit(10)
                .collect(Collectors.toList());
    }

    @Override
    public double getAverageRating() {
        Double avgRating = courseRepository.getAverageRating();
        return avgRating != null ? avgRating : 0.0;
    }

    @Override
    public long getFreeCourseCount() {
        return courseRepository.countFreeCourses();
    }

    @Override
    public long getPaidCourseCount() {
        return courseRepository.countPaidCourses();
    }

    @Override
    public List<String> getAllCategories() {
        return courseRepository.findAllCategories();
    }
}