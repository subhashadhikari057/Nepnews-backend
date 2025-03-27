package com.nepnews.controllers;

import com.nepnews.models.User;
import com.nepnews.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 🔐 ADMIN only: Get all users
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // 🔐 ADMIN only: Promote user
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/role")
    public User updateUserRole(@PathVariable String id, @RequestParam String role) {
        return userService.updateUserRole(id, role);
    }

    // 🧪 Public test route
    @GetMapping("/ping")
    public String ping() {
        return "UserController is working!";
    }
}
