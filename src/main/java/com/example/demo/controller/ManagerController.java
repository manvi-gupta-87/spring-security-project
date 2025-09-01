package com.example.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    @GetMapping("")
    public String getManagerRootNoSlash() {
        return "Manager API Root - Available endpoints: /update";
    }

    @GetMapping("/")
    public String getManagerRoot() {
        return "Manager API - Available endpoints: /update";
    }

    @GetMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String updateData() {
        return "Data is updated";
    }
}
