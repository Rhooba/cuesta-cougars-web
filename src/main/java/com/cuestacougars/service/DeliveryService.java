package com.cuestacougars.service;

import com.cuestacougars.model.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Spring service that owns the singleton GlobalData + FileManager and exposes
 * web-friendly methods for the controllers to call.
 */
@Service
public class DeliveryService {

    private GlobalData globalData;
    private FileManager fileManager;
    private AccountCreation accountCreation;

    private static final String[] SEED_FILES = {
        "menu.txt", "customers.txt", "drivers.txt", "admins.txt", "orders.txt"
    };

    @PostConstruct
    public void init() {
        ensureDataFiles();
        fileManager = new FileManager();
        globalData  = new GlobalData();
        globalData.setFileManager(fileManager);
        accountCreation = new AccountCreation(globalData, fileManager);
        fileManager.loadAll(globalData);
    }

    /**
     * On startup, make sure a local "data/" directory exists with the seed files.
     * Needed when running from a JAR (e.g. on Railway) where only the packaged
     * classpath resources are available — FileManager reads/writes relative paths
     * like "data/menu.txt", so we extract seeds there on first boot.
     */
    private void ensureDataFiles() {
        try {
            Path dataDir = Paths.get("data");
            Files.createDirectories(dataDir);
            for (String name : SEED_FILES) {
                Path target = dataDir.resolve(name);
                if (Files.exists(target)) continue;
                ClassPathResource seed = new ClassPathResource("seed/" + name);
                if (!seed.exists()) {
                    Files.createFile(target);
                    continue;
                }
                try (InputStream in = seed.getInputStream()) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to bootstrap data directory: " + e.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        fileManager.saveAll(globalData);
    }

    // --- Auth ---

    public User login(String username, String password) {
        return globalData.login(username, password);
    }

    public Customer createCustomer(String username, String password, String name, String address) {
        if (!accountCreation.isValidPassword(password)) return null;
        return accountCreation.createCustomer(username, password, name, address);
    }

    public Driver createDriver(String username, String password, String name, String location) {
        if (!accountCreation.isValidPassword(password)) return null;
        return accountCreation.createDriver(username, password, name, location);
    }

    public Admin createAdmin(String username, String password, String name) {
        if (!accountCreation.isValidPassword(password)) return null;
        return accountCreation.createAdmin(username, password, name);
    }

    public boolean isPasswordValid(String password) {
        return accountCreation.isValidPassword(password);
    }

    // --- Lookups ---

    public Customer findCustomer(String username) {
        for (Customer c : globalData.getCustomers()) {
            if (c.getUsername().equals(username)) return c;
        }
        return null;
    }

    public Driver findDriver(String username) {
        for (Driver d : globalData.getDrivers()) {
            if (d.getUsername().equals(username)) return d;
        }
        return null;
    }

    public Admin findAdmin(String username) {
        for (Admin a : globalData.getAdmins()) {
            if (a.getUsername().equals(username)) return a;
        }
        return null;
    }

    // --- Menu ---

    public Collection<MenuItem> getMenuItems() {
        return globalData.getMenu().values();
    }

    public Map<String, MenuItem> getMenu() {
        return globalData.getMenu();
    }

    public void addMenuItem(String name, double price) {
        if (globalData.getMenuItem(name) == null) {
            globalData.addMenuItem(new MenuItem(name, price));
            fileManager.saveMenu(globalData);
        }
    }

    public void removeMenuItem(String name) {
        globalData.removeMenuItem(name);
        fileManager.saveMenu(globalData);
    }

    public void updateMenuItemPrice(String name, double price) {
        MenuItem item = globalData.getMenuItem(name);
        if (item != null) {
            item.setPrice(price);
            fileManager.saveMenu(globalData);
        }
    }

    // --- Orders ---

    /**
     * Places a new order for the given customer with the selected item names.
     * Handles enqueueing, driver assignment, and file persistence.
     */
    public Order placeOrder(String customerUsername, List<String> itemNames) {
        Customer customer = findCustomer(customerUsername);
        List<MenuItem> items = new ArrayList<>();
        for (String name : itemNames) {
            MenuItem item = globalData.getMenuItem(name);
            if (item != null) items.add(item);
        }
        Order order = new Order(customer, items, customer.getDeliveryAddress());
        customer.addOrderToHistory(order);
        globalData.enqueueOrder(order);
        globalData.processNextOrder();
        fileManager.appendOrder(order);
        return order;
    }

    public List<Order> getAllOrders() {
        return globalData.getAllOrders();
    }

    // --- Driver actions ---

    public void markInProgress(String driverUsername) {
        Driver driver = findDriver(driverUsername);
        if (driver != null) driver.markInProgress();
    }

    public void markDelivered(String driverUsername) {
        Driver driver = findDriver(driverUsername);
        if (driver != null) {
            driver.markDelivered();
            fileManager.saveDrivers(globalData);
        }
    }

    // --- Customer actions ---

    public void rateDriver(String customerUsername, int orderId, int rating) {
        Customer customer = findCustomer(customerUsername);
        if (customer == null) return;
        for (Order o : customer.getOrderHistory()) {
            if (o.getOrderId() == orderId && o.getAssignedDriver() != null) {
                customer.rateDriver(o.getAssignedDriver(), rating);
                fileManager.saveDrivers(globalData);
                break;
            }
        }
    }
}
