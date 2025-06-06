package com.nepnews.controllers;

import com.nepnews.models.News;
import com.nepnews.models.User;
import com.nepnews.repositories.NewsRepository;
import com.nepnews.repositories.UserRepository;
import com.nepnews.services.NewsService;
import com.nepnews.services.RssService;
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
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RssService rssService;

    @Autowired
    private NewsRepository newsRepository;


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
        news.setCreatedBy(userId);      // ✅ Set userId (from token)

        // ✅ Get author name using userId
        Optional<User> userOpt = userRepository.findById(userId);
        userOpt.ifPresent(user -> news.setAuthorName(user.getName())); // 💡 Set authorName

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

    @DeleteMapping("/slug/{slug}")
    @PreAuthorize("hasAnyRole('AUTHOR','EDITOR','ADMIN')")
    public ResponseEntity<String> deleteNewsBySlug(@PathVariable String slug) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName(); // from JWT
        String role = auth.getAuthorities().iterator().next().getAuthority(); // ROLE_EDITOR

        boolean deleted = newsService.deleteNewsBySlug(slug, userId, role);

        if (deleted) {
            return ResponseEntity.ok("News deleted successfully");
        } else {
            return ResponseEntity.status(403).body("Not authorized or news not found");
        }
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

    @GetMapping("/published")
    public List<News> getPublishedNews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int limit,
            @RequestParam(required = false) String search
    ) {
        return newsService.getPublishedNews(search, page, limit);
    }


//for fetching from other
    @GetMapping("/rss/fetch")
    public ResponseEntity<?> fetchRssNews(@RequestParam(defaultValue = "") int limit) {
        String feedUrl = "https://www.thehindu.com/news/national/feeder/default.rss";

        List<News> fetchedNews = rssService.fetchNewsFromRss(feedUrl, limit);

        int savedCount = 0;
        for (News news : fetchedNews) {
            if (!newsRepository.findBySlug(news.getSlug()).isPresent()) {
                newsRepository.save(news);
                savedCount++;
            }
        }

        return ResponseEntity.ok("✅ Fetched and saved " + savedCount + " new articles (limit: " + limit + ")");
    }
}





