package com.cuestacougars.controller;

import com.cuestacougars.model.*;
import com.cuestacougars.service.DeliveryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    public String placeOrder(@RequestParam(name = "item",  required = false) List<String>  itemNames,
                             @RequestParam(name = "qty",   required = false) List<Integer> qtys,
                             HttpSession session) {
        String username = requireRole(session);
        if (username == null) return "redirect:/";

        List<String> expanded = new ArrayList<>();
        if (itemNames != null && qtys != null) {
            int n = Math.min(itemNames.size(), qtys.size());
            for (int i = 0; i < n; i++) {
                Integer q = qtys.get(i);
                if (q == null || q <= 0) continue;
                for (int j = 0; j < q; j++) {
                    expanded.add(itemNames.get(i));
                }
            }
        }
        if (expanded.isEmpty()) {
            return "redirect:/customer/dashboard?error=noitems";
        }
        Order order = service.placeOrder(username, expanded);
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
