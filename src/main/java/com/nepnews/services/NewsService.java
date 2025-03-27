package com.nepnews.services;

import com.nepnews.models.News;
import com.nepnews.repositories.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;



@Service
public class NewsService {
    @Autowired
    private NewsRepository newsRepository;

    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    // ✅ Updated: Fetch All News (with Search Support)
    public List<News> getAllNews(String search, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (search != null && !search.isEmpty()) {
            return newsRepository
                    .findByTitleContainingIgnoreCaseOrKeywordsContainingIgnoreCase(search, search, pageable)
                    .getContent();
        }

        return newsRepository.findAll(pageable).getContent();
    }


    // ✅ Existing Methods Remain Unchanged
    public News createNews(News news) {
        String slug = generateSlug(news.getTitle());
        news.setSlug(slug);

        if (news.getImageUrl() == null || news.getImageUrl().isEmpty()) {
            news.setImageUrl("https://i.imgur.com/default-placeholder.jpg"); // Default image
        }
        news.setCreatedAt(new Date()); // Set current timestamp

        return newsRepository.save(news);
    }

    public List<News> getAllNews() {
        return newsRepository.findAll();
    }

    public Optional<News> getNewsById(String id) {
        return newsRepository.findById(id);
    }

    public Optional<News> getNewsBySlug(String slug) {
        return newsRepository.findBySlug(slug);
    }

    public News updateNews(String id, News updatedNews) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName(); // email from JWT
        String currentRole = authentication.getAuthorities().stream()
                .findFirst().get().getAuthority(); // e.g. "ROLE_AUTHOR"

        return newsRepository.findById(id).map(existingNews -> {
            // ✅ If AUTHOR, ensure they own the draft  it is still a draft
            if ("ROLE_AUTHOR".equals(currentRole)) {
                if (!existingNews.getCreatedBy().equals(currentEmail) || !"draft".equalsIgnoreCase(existingNews.getStatus())) {
                    throw new SecurityException("Authors can only edit their own drafted news.");
                }
            }

            // ✅ Allow updates
            if (updatedNews.getTitle() != null) {
                existingNews.setTitle(updatedNews.getTitle());
                existingNews.setSlug(generateSlug(updatedNews.getTitle()));
            }
            if (updatedNews.getContent() != null) {
                existingNews.setContent(updatedNews.getContent());
            }
            if (updatedNews.getCategory() != null) {
                existingNews.setCategory(updatedNews.getCategory());
            }
            if (updatedNews.getKeywords() != null) {
                existingNews.setKeywords(updatedNews.getKeywords());
            }
            if (updatedNews.getStatus() != null) {
                existingNews.setStatus(updatedNews.getStatus());
            }
            if (updatedNews.getEditorId() != null) {
                existingNews.setEditorId(updatedNews.getEditorId());
            }

            existingNews.setUpdatedAt(new Date());
            return newsRepository.save(existingNews);
        }).orElse(null);
    }

    public boolean deleteNews(String id, String userId, String role) {
        Optional<News> newsOpt = newsRepository.findById(id);

        if (newsOpt.isEmpty()) return false;

        News news = newsOpt.get();


        // 🛡 Author can only delete their own *draft*
        if ("ROLE_AUTHOR".equals(role)) {
            if (news.getCreatedBy() == null || !news.getCreatedBy().equals(userId)) {
                return false;
            }
            if (!"draft".equalsIgnoreCase(news.getStatus())) {
                return false;
            }
        }

        // ✅ Editors and Admins can delete anything (no extra check)
        newsRepository.deleteById(id);
        return true;
    }



    private String generateSlug(String title) {
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("[^\\w-]");
        return pattern.matcher(normalized.toLowerCase().replace(" ", "-")).replaceAll("")
                + "-" + System.currentTimeMillis();
    }

    public List<News> getNewsByCategory(String category) {
        return newsRepository.findByCategoryIgnoreCase(category);
    }
    public List<News> getNewsByStatus(String status) {
        return newsRepository.findByStatusRegex(status); // ✅ case-insensitive
    }

    public List<News> getNewsByUserAndStatus(String userId, String status) {
        if (status != null && !status.isEmpty()) {
            return newsRepository.findByCreatedByAndStatus(userId, status);
        } else {
            return newsRepository.findByCreatedBy(userId);
        }
    }
    public boolean deleteNewsBySlug(String slug, String userId, String role) {
        System.out.println("🛠️ Deleting news with slug: " + slug);
        System.out.println("👤 User ID: " + userId);
        System.out.println("🔐 Role: " + role);

        Optional<News> newsOpt = newsRepository.findBySlug(slug);
        if (newsOpt.isEmpty()) {
            System.out.println("⚠️ News not found.");
            return false;
        }

        News news = newsOpt.get();

        // Block authors from deleting other people's drafts
        if (role.equalsIgnoreCase("ROLE_AUTHOR") || role.equalsIgnoreCase("AUTHOR")) {
            if (!news.getCreatedBy().equals(userId) || !"draft".equalsIgnoreCase(news.getStatus())) {
                System.out.println("⛔ Author is not allowed to delete this draft.");
                return false;
            }
        }

        // Allow EDITOR and ADMIN unconditionally
        newsRepository.delete(news);
        System.out.println("✅ News deleted successfully.");
        return true;
    }

    public boolean updateNewsBySlug(String slug, News updatedNews, String userId, String role) {
        Optional<News> newsOpt = newsRepository.findBySlug(slug);

        if (newsOpt.isEmpty()) return false;

        News existingNews = newsOpt.get();

        System.out.println("🔁 updateNewsBySlug");
        System.out.println("→ Slug: " + slug);
        System.out.println("→ currentUserId: " + userId);
        System.out.println("→ role: " + role);

        System.out.println("→ DB News createdBy: " + existingNews.getCreatedBy());
        System.out.println("→ DB News status: " + existingNews.getStatus());


        // Author can only update their own drafts
        if ("ROLE_AUTHOR".equals(role)) {
            if (!existingNews.getCreatedBy().equals(userId) ||
                    !"draft".equalsIgnoreCase(existingNews.getStatus())) {
                return false;
            }
        }

        // ✅ Apply updates
        if (updatedNews.getTitle() != null) {
            existingNews.setTitle(updatedNews.getTitle());
            existingNews.setSlug(generateSlug(updatedNews.getTitle()));
        }
        if (updatedNews.getContent() != null) {
            existingNews.setContent(updatedNews.getContent());
        }
        if (updatedNews.getImageUrl() != null) {
            existingNews.setImageUrl(updatedNews.getImageUrl());
        }
        if (updatedNews.getCategory() != null) {
            existingNews.setCategory(updatedNews.getCategory());
        }
        if (updatedNews.getKeywords() != null) {
            existingNews.setKeywords(updatedNews.getKeywords());
        }
        if (updatedNews.getStatus() != null) {
            existingNews.setStatus(updatedNews.getStatus());
        }

        existingNews.setUpdatedAt(new Date());
        newsRepository.save(existingNews);
        return true;
    }
    public List<News> getPublishedNews(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return newsRepository.findByStatus("PUBLISHED", pageable);
    }


}
