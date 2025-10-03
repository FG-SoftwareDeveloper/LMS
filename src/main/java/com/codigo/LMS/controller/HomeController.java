package com.codigo.LMS.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "Home");
        model.addAttribute("pageDescription", "Experience the future of education with our innovative LMS platform");
        return "index";
    }

    @GetMapping("/api/test")
    @ResponseBody
    public String test() {
        return "API is working! Security is disabled for development.";
    }
    

    
    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "About Us");
        return "about"; // We'll create this later
    }
    
    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("pageTitle", "Contact");
        return "contact"; // We'll create this later
    }
    
    @GetMapping("/privacy")
    public String privacy(Model model) {
        model.addAttribute("pageTitle", "Privacy Policy");
        return "privacy";
    }
    
    @GetMapping("/terms")
    public String terms(Model model) {
        model.addAttribute("pageTitle", "Terms of Use");
        return "terms";
    }
    
    @GetMapping("/grading-rubric")
    public String gradingRubric(Model model) {
        model.addAttribute("pageTitle", "Grading Rubric");
        return "grading-rubric";
    }
}