package com.example.demo.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service("customSecurity")
public class CustomSecurityService {
    // Custom method to check business logic
    public boolean isWorkingHours() {
        int hour = LocalTime.now().getHour();
        return hour >= 9 && hour <= 17; // 9 AM to 5 PM
    }

    // Check if user has premium account (mock logic)
    public boolean isPremium(String userName) {
        return userName.startsWith("premium");
    }

    // Complex ownership check
    public boolean canModify(Long documentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Add your custom logic here
        return auth.getName().equals("admin") || documentId < 100;
    }

}
