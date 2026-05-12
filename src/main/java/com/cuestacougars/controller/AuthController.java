package com.cuestacougars.controller;

import com.cuestacougars.model.*;
import com.cuestacougars.service.DeliveryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private DeliveryService service;

    @GetMapping("/")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("username") != null) {
            String role = (String) session.getAttribute("role");
            return "redirect:/" + role + "/dashboard";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        User user = service.login(username, password);
        if (user == null) {
            model.addAttribute("error", "Invalid username or password. Please try again.");
            return "login";
        }
        session.setAttribute("username", username);
        session.setAttribute("name", user.getName());
        String role = user.getClass().getSimpleName().toLowerCase();
        session.setAttribute("role", role);
        return "redirect:/" + role + "/dashboard";
    }

    @GetMapping("/register")
    public String registerPage(HttpSession session) {
        if (session.getAttribute("username") != null) return "redirect:/";
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String type,
                           @RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String name,
                           @RequestParam(required = false) String address,
                           @RequestParam(required = false) String location,
                           Model model) {
        if (!service.isPasswordValid(password)) {
            model.addAttribute("error", "Password must be at least 6 characters.");
            return "register";
        }

        boolean success;
        switch (type) {
            case "customer":
                success = service.createCustomer(username, password, name,
                                                 address != null ? address : "") != null;
                break;
            case "driver":
                success = service.createDriver(username, password, name,
                                               location != null ? location : "") != null;
                break;
            default:
                success = service.createAdmin(username, password, name) != null;
        }

        if (!success) {
            model.addAttribute("error", "That username is already taken. Please choose another.");
            return "register";
        }
        model.addAttribute("success", "Account created! Please sign in.");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
