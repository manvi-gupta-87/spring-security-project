package com.example.demo.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        System.out.println("user:  " + encoder.encode("password"));
        System.out.println("admin: " + encoder.encode("admin123"));
        System.out.println("manager: " + encoder.encode("manager"));
    }
}