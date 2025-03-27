package com.nepnews.services;

import com.nepnews.repositories.NewsRepository;
import com.nepnews.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final NewsRepository newsRepository;

    public Map<String, Long> getAdminStats() {
        Map<String, Long> stats = new HashMap<>();

        stats.put("totalUsers", userRepository.count());
        stats.put("totalNews", newsRepository.count());
        stats.put("draftNews", newsRepository.countByStatusIgnoreCase("draft"));
        stats.put("publishedNews", newsRepository.countByStatusIgnoreCase("published"));

        return stats;
    }
}
