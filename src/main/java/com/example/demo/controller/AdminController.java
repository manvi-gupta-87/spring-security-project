package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/")
    public String getAdminRoot() {
        return "Admin API - Available endpoints: /dashboard, /stats, /ping";
    }

    @GetMapping("/dashboard")
    public String getAdminDashboard() {
        return "Hello Admin - Welcome to Admin Dashboard";
    }

    @GetMapping("/stats")
    public String getAdminStats() {
        return "Admin Statistics - Users: 150, Active Sessions: 45";
    }

    @GetMapping("/ping")
    public String pingAdmin() {
        return "Pinging the admin";
    }
}
