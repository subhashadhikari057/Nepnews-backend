package com.nepnews.controllers;

import com.nepnews.models.News;
import com.nepnews.services.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @PreAuthorize("hasAnyRole('AUTHOR', 'EDITOR', 'ADMIN')")
    @PostMapping
    public News createNews(@RequestBody News news) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName(); // ✅ userId from JWT
        news.setCreatedBy(userId);      // ✅ set it here (don't trust frontend)

        return newsService.createNews(news);
    }


    // ✅ Update News
    @PreAuthorize("hasAnyRole('AUTHOR','EDITOR', 'ADMIN')")
    @PutMapping("/{id}")
    public News updateNews(@PathVariable String id, @RequestBody News news) {
        return newsService.updateNews(id, news);
    }

    @PreAuthorize("hasAnyRole('AUTHOR','EDITOR','ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteNews(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        String role = auth.getAuthorities().iterator().next().getAuthority(); // e.g., ROLE_AUTHOR

        System.out.println("🛠️ Calling delete for ID: " + id + " by userId: " + email + " with role: " + role);


        return newsService.deleteNews(id, email, role)
                ? "News deleted successfully"
                : "News not found or not authorized";
    }



    // Get News by Category
    @GetMapping("/category/{category}")
    public List<News> getNewsByCategory(@PathVariable String category) {
        return newsService.getNewsByCategory(category);
    }
    // Get News by Status (for editors)
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public List<News> getNewsByStatus(@PathVariable String status) {
        return newsService.getNewsByStatus(status);
    }
    @GetMapping("/user/{userId}")
    public List<News> getNewsByUserAndStatus(
            @PathVariable String userId,
            @RequestParam(required = false) String status
    ) {
        return newsService.getNewsByUserAndStatus(userId, status);
    }

    @PreAuthorize("hasAnyRole('AUTHOR','EDITOR','ADMIN')")
    @DeleteMapping("/slug/{slug}")
    public String deleteNewsBySlug(@PathVariable String slug) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName(); // this is userId from token
        String role = auth.getAuthorities().iterator().next().getAuthority(); // ROLE_AUTHOR

        return newsService.deleteNewsBySlug(slug, userId, role)
                ? "News deleted successfully"
                : "News not found or not authorized";
    }
    @PreAuthorize("hasAnyRole('AUTHOR','EDITOR','ADMIN')")
    @PutMapping("/slug/{slug}")
    public ResponseEntity<?> updateNewsBySlug(@PathVariable String slug, @RequestBody News updatedNews) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName(); // userId from JWT
        String role = auth.getAuthorities().iterator().next().getAuthority(); // ROLE_AUTHOR

        boolean updated = newsService.updateNewsBySlug(slug, updatedNews, userId, role);
        if (updated) {
            return ResponseEntity.ok("News updated successfully");
        } else {
            return ResponseEntity.status(403).body("Unauthorized or not found");
        }
    }

}





