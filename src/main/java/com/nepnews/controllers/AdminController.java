package com.nepnews.controllers;

import com.nepnews.models.News;
import com.nepnews.models.User;
import com.nepnews.models.enums.Role;
import com.nepnews.repositories.NewsRepository;
import com.nepnews.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor // ✅ Lombok will generate constructor for final fields
public class AdminController {

    private final UserRepository userRepository;
    private final NewsRepository newsRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        List<User> users = userRepository.findAll();
        List<News> newsList = newsRepository.findAll();

        Map<String, Long> stats = new HashMap<>();

        // ✅ Count roles
        stats.put("readers", users.stream().filter(u -> u.getRole() == Role.READER).count());
        stats.put("authors", users.stream().filter(u -> u.getRole() == Role.AUTHOR).count());
        stats.put("editors", users.stream().filter(u -> u.getRole() == Role.EDITOR).count());
        stats.put("admins", users.stream().filter(u -> u.getRole() == Role.ADMIN).count());

        // ✅ Count news
        stats.put("drafts", newsList.stream().filter(n -> "DRAFT".equals(n.getStatus())).count());
        stats.put("published", newsList.stream().filter(n -> "PUBLISHED".equals(n.getStatus())).count());

        return stats;
    }
}
