package com.nepnews.controllers;

import com.nepnews.models.News;
import com.nepnews.services.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/news") // Base API route
public class NewsController {
    @Autowired
    private NewsService newsService;

    // ✅ Updated: Get All News (Now supports search)
    @GetMapping
    public List<News> getAllNews(
            @RequestParam(required = false) String search, // Search query (optional)
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int limit
    ) {
        return newsService.getAllNews(search, page, limit);
    }

    // Create News
    @PostMapping
    public News createNews(@RequestBody News news) {
        return newsService.createNews(news);
    }

    // Get News by ID
    @GetMapping("/{id}")
    public Optional<News> getNewsById(@PathVariable String id) {
        return newsService.getNewsById(id);
    }

    // Get News by Slug
    @GetMapping("/slug/{slug}")
    public ResponseEntity<News> getNewsBySlug(@PathVariable String slug) {
        return newsService.getNewsBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update News
    @PutMapping("/{id}")
    public News updateNews(@PathVariable String id, @RequestBody News news) {
        return newsService.updateNews(id, news);
    }

    // Delete News
    @DeleteMapping("/{id}")
    public String deleteNews(@PathVariable String id) {
        return newsService.deleteNews(id) ? "News deleted successfully" : "News not found";
    }

    // Get News by Category
    @GetMapping("/category/{category}")
    public List<News> getNewsByCategory(@PathVariable String category) {
        return newsService.getNewsByCategory(category);
    }
}
