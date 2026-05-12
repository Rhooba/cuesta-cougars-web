package com.cuestacougars.controller;

import com.cuestacougars.service.DeliveryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private DeliveryService service;

    private String requireRole(HttpSession session) {
        if (!"admin".equals(session.getAttribute("role"))) return null;
        return (String) session.getAttribute("username");
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model,
                            @RequestParam(required = false) String msg) {
        String username = requireRole(session);
        if (username == null) return "redirect:/";

        model.addAttribute("menu", service.getMenuItems());
        model.addAttribute("orders", service.getAllOrders());
        model.addAttribute("name", session.getAttribute("name"));
        if (msg != null) model.addAttribute("msg", msg);
        return "admin/dashboard";
    }

    @PostMapping("/menu/add")
    public String addItem(@RequestParam String itemName,
                          @RequestParam double price,
                          HttpSession session) {
        if (requireRole(session) == null) return "redirect:/";
        service.addMenuItem(itemName.trim(), price);
        return "redirect:/admin/dashboard?msg=added";
    }

    @PostMapping("/menu/remove")
    public String removeItem(@RequestParam String itemName,
                             HttpSession session) {
        if (requireRole(session) == null) return "redirect:/";
        service.removeMenuItem(itemName);
        return "redirect:/admin/dashboard?msg=removed";
    }

    @PostMapping("/menu/update")
    public String updateItem(@RequestParam String itemName,
                             @RequestParam double price,
                             HttpSession session) {
        if (requireRole(session) == null) return "redirect:/";
        service.updateMenuItemPrice(itemName, price);
        return "redirect:/admin/dashboard?msg=updated";
    }
}
