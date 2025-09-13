package com.example.demo.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message = "UserName cant be blank")
    @Size(min = 3, max = 50, message = "username must be 3 -50 chars" )
    private String userName;

    @NotBlank(message = "password can not be empty")
    @Size(min = 8, max = 100, message = "password must be at least 8 chars")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "password must contain letters and digits"
    )
    private String password;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
