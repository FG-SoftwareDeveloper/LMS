package com.codigo.LMS.controller;

import com.codigo.LMS.entity.*;
import com.codigo.LMS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/courses")
public class CourseController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String courseList(Model model) {
        // For now, we'll create sample courses
        List<Course> courses = createSampleCourses();
        model.addAttribute("courses", courses);
        return "courses";
    }
    
    @GetMapping("/{id}")
    public String courseDetail(@PathVariable Long id, @AuthenticationPrincipal User user, Model model) {
        Course course = findCourseById(id);
        if (course == null) {
            return "redirect:/courses";
        }
        
        model.addAttribute("course", course);
        
        // Check if user is enrolled
        boolean isEnrolled = user.getEnrollments().stream()
            .anyMatch(enrollment -> enrollment.getCourse().getId().equals(id));
        model.addAttribute("isEnrolled", isEnrolled);
        
        // Get enrollment if exists
        if (isEnrolled) {
            Enrollment enrollment = user.getEnrollments().stream()
                .filter(e -> e.getCourse().getId().equals(id))
                .findFirst()
                .orElse(null);
            model.addAttribute("enrollment", enrollment);
        }
        
        return "course-detail";
    }
    
    @PostMapping("/{id}/enroll")
    public String enrollInCourse(@PathVariable Long id, @AuthenticationPrincipal User user, 
                                RedirectAttributes redirectAttributes) {
        Course course = findCourseById(id);
        if (course == null) {
            redirectAttributes.addFlashAttribute("error", "Course not found");
            return "redirect:/courses";
        }
        
        // Check if already enrolled
        boolean alreadyEnrolled = user.getEnrollments().stream()
            .anyMatch(enrollment -> enrollment.getCourse().getId().equals(id));
        
        if (alreadyEnrolled) {
            redirectAttributes.addFlashAttribute("info", "You are already enrolled in this course");
            return "redirect:/courses/" + id;
        }
        
        // Create new enrollment
        Enrollment enrollment = new Enrollment(user, course);
        user.getEnrollments().add(enrollment);
        course.getEnrollments().add(enrollment);
        
        // Award enrollment points
        user.addPoints(10);
        user.recordActivity();
        
        // Save user (this would normally use a service)
        // userService.save(user);
        
        redirectAttributes.addFlashAttribute("success", "Successfully enrolled in " + course.getTitle());
        return "redirect:/courses/" + id;
    }
    
    @GetMapping("/{courseId}/modules/{moduleId}/lessons/{lessonId}")
    public String lessonView(@PathVariable Long courseId, @PathVariable Long moduleId, 
                           @PathVariable Long lessonId, @AuthenticationPrincipal User user, Model model) {
        Course course = findCourseById(courseId);
        if (course == null) {
            return "redirect:/courses";
        }
        
        // Check enrollment
        boolean isEnrolled = user.getEnrollments().stream()
            .anyMatch(enrollment -> enrollment.getCourse().getId().equals(courseId));
        
        if (!isEnrolled) {
            return "redirect:/courses/" + courseId;
        }
        
        // Find module and lesson (simplified for demo)
        com.codigo.LMS.entity.Module module = course.getModules().stream()
            .filter(m -> m.getId().equals(moduleId))
            .findFirst()
            .orElse(null);
        
        if (module == null) {
            return "redirect:/courses/" + courseId;
        }
        
        Lesson lesson = module.getLessons().stream()
            .filter(l -> l.getId().equals(lessonId))
            .findFirst()
            .orElse(null);
        
        if (lesson == null) {
            return "redirect:/courses/" + courseId;
        }
        
        model.addAttribute("course", course);
        model.addAttribute("module", module);
        model.addAttribute("lesson", lesson);
        
        // Get or create progress record
        UserProgress progress = user.getProgressRecords().stream()
            .filter(p -> p.getLesson().getId().equals(lessonId))
            .findFirst()
            .orElse(new UserProgress(user, lesson));
        
        model.addAttribute("progress", progress);
        
        return "lesson-view";
    }
    
    @PostMapping("/{courseId}/modules/{moduleId}/lessons/{lessonId}/complete")
    @ResponseBody
    public String markLessonComplete(@PathVariable Long courseId, @PathVariable Long moduleId,
                                   @PathVariable Long lessonId, @AuthenticationPrincipal User user) {
        // Find or create progress record
        UserProgress progress = user.getProgressRecords().stream()
            .filter(p -> p.getLesson().getId().equals(lessonId))
            .findFirst()
            .orElse(null);
        
        if (progress == null) {
            Course course = findCourseById(courseId);
            com.codigo.LMS.entity.Module module = course.getModules().stream()
                .filter(m -> m.getId().equals(moduleId))
                .findFirst()
                .orElse(null);
            Lesson lesson = module.getLessons().stream()
                .filter(l -> l.getId().equals(lessonId))
                .findFirst()
                .orElse(null);
            
            progress = new UserProgress(user, lesson);
            user.getProgressRecords().add(progress);
        }
        
        progress.markAsCompleted();
        progress.setPointsEarned(25); // Award points for completion
        user.addPoints(25);
        user.recordActivity();
        
        // Update enrollment progress
        updateEnrollmentProgress(user, courseId);
        
        return "success";
    }
    
    // Helper methods (these would normally be in services)
    private Course findCourseById(Long id) {
        List<Course> courses = createSampleCourses();
        return courses.stream()
            .filter(course -> course.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    private void updateEnrollmentProgress(User user, Long courseId) {
        Enrollment enrollment = user.getEnrollments().stream()
            .filter(e -> e.getCourse().getId().equals(courseId))
            .findFirst()
            .orElse(null);
        
        if (enrollment != null) {
            // Calculate progress based on completed lessons
            Course course = enrollment.getCourse();
            int totalLessons = course.getModules().stream()
                .mapToInt(module -> module.getLessons().size())
                .sum();
            
            long completedLessons = user.getProgressRecords().stream()
                .filter(progress -> progress.getStatus() == UserProgress.ProgressStatus.COMPLETED)
                .filter(progress -> course.getModules().stream()
                    .anyMatch(module -> module.getLessons().contains(progress.getLesson())))
                .count();
            
            double progressPercentage = totalLessons > 0 ? (completedLessons * 100.0 / totalLessons) : 0.0;
            enrollment.setProgressPercentage(progressPercentage);
            
            if (progressPercentage >= 100.0) {
                enrollment.setStatus(com.codigo.LMS.entity.EnrollmentStatus.COMPLETED);
                enrollment.setCompletedAt(LocalDateTime.now());
                // Award completion bonus
                user.addPoints(100);
            }
        }
    }
    
    private List<Course> createSampleCourses() {
        List<Course> courses = new ArrayList<>();
        
        // Create sample instructor
        User instructor = new User("instructor", "instructor@example.com", "password", "John", "Instructor", Role.INSTRUCTOR);
        instructor.setId(2L);
        
        // Course 1
        Course course1 = new Course("Java Fundamentals", "Learn the basics of Java programming", instructor);
        course1.setId(1L);
        course1.setPrice(new BigDecimal("99.99"));
        course1.setDifficultyLevel(Course.DifficultyLevel.BEGINNER);
        course1.setImageUrl("/images/java-course.jpg");
        course1.setDurationHours(40);
        course1.setIsPublished(true);
        
        // Add modules to course1
        com.codigo.LMS.entity.Module module1 = new com.codigo.LMS.entity.Module("Introduction to Java", course1, 1);
        module1.setId(1L);
        module1.setDurationMinutes(120);
        
        Lesson lesson1 = new Lesson("What is Java?", "Introduction to Java programming language", module1, 1);
        lesson1.setId(1L);
        lesson1.setDurationMinutes(30);
        lesson1.setLessonType(Lesson.LessonType.VIDEO);
        lesson1.setIsPublished(true);
        
        Lesson lesson2 = new Lesson("Setting up Java Development Environment", "Install Java JDK and IDE", module1, 2);
        lesson2.setId(2L);
        lesson2.setDurationMinutes(45);
        lesson2.setLessonType(Lesson.LessonType.VIDEO);
        lesson2.setIsPublished(true);
        
        module1.getLessons().add(lesson1);
        module1.getLessons().add(lesson2);
        course1.getModules().add(module1);
        
        courses.add(course1);
        
        // Course 2
        Course course2 = new Course("Advanced Spring Boot", "Master Spring Boot framework development", instructor);
        course2.setId(2L);
        course2.setPrice(new BigDecimal("149.99"));
        course2.setDifficultyLevel(Course.DifficultyLevel.ADVANCED);
        course2.setImageUrl("/images/spring-course.jpg");
        course2.setDurationHours(60);
        course2.setIsPublished(true);
        courses.add(course2);
        
        return courses;
    }
}