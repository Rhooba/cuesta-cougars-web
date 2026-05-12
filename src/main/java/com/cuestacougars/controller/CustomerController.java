package com.cuestacougars.controller;

import com.cuestacougars.model.*;
import com.cuestacougars.service.DeliveryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private DeliveryService service;

    private String requireRole(HttpSession session) {
        if (!"customer".equals(session.getAttribute("role"))) return null;
        return (String) session.getAttribute("username");
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model,
                            @RequestParam(required = false) String ordered,
                            @RequestParam(required = false) String rated,
                            @RequestParam(required = false) String error) {
        String username = requireRole(session);
        if (username == null) return "redirect:/";

        Customer customer = service.findCustomer(username);
        List<Order> orders = customer.getOrderHistory();

        boolean hasDelivered = orders.stream()
                .anyMatch(o -> o.getStatus() == OrderStatus.DELIVERED
                               && o.getAssignedDriver() != null);

        model.addAttribute("customer", customer);
        model.addAttribute("menu", service.getMenuItems());
        model.addAttribute("orders", orders);
        model.addAttribute("hasDelivered", hasDelivered);
        model.addAttribute("name", session.getAttribute("name"));
        if (ordered != null) model.addAttribute("orderedId", ordered);
        if (rated   != null) model.addAttribute("rated", true);
        if (error   != null) model.addAttribute("orderError", true);
        return "customer/dashboard";
    }

    @PostMapping("/order")
    public String placeOrder(@RequestParam(required = false) List<String> items,
                             HttpSession session) {
        String username = requireRole(session);
        if (username == null) return "redirect:/";
        if (items == null || items.isEmpty()) {
            return "redirect:/customer/dashboard?error=noitems";
        }
        Order order = service.placeOrder(username, items);
        return "redirect:/customer/dashboard?ordered=" + order.getOrderId();
    }

    @PostMapping("/rate")
    public String rateDriver(@RequestParam int orderId,
                             @RequestParam int rating,
                             HttpSession session) {
        String username = requireRole(session);
        if (username == null) return "redirect:/";
        service.rateDriver(username, orderId, rating);
        return "redirect:/customer/dashboard?rated=true";
    }
}
