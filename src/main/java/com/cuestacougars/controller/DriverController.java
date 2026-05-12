package com.cuestacougars.controller;

import com.cuestacougars.model.Driver;
import com.cuestacougars.service.DeliveryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/driver")
public class DriverController {

    @Autowired
    private DeliveryService service;

    private String requireRole(HttpSession session) {
        if (!"driver".equals(session.getAttribute("role"))) return null;
        return (String) session.getAttribute("username");
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model,
                            @RequestParam(required = false) String msg) {
        String username = requireRole(session);
        if (username == null) return "redirect:/";

        Driver driver = service.findDriver(username);
        model.addAttribute("driver", driver);
        model.addAttribute("name", session.getAttribute("name"));
        if (msg != null) model.addAttribute("msg", msg);
        return "driver/dashboard";
    }

    @PostMapping("/progress")
    public String markInProgress(HttpSession session) {
        String username = requireRole(session);
        if (username == null) return "redirect:/";
        service.markInProgress(username);
        return "redirect:/driver/dashboard?msg=inprogress";
    }

    @PostMapping("/delivered")
    public String markDelivered(HttpSession session) {
        String username = requireRole(session);
        if (username == null) return "redirect:/";
        service.markDelivered(username);
        return "redirect:/driver/dashboard?msg=delivered";
    }
}
