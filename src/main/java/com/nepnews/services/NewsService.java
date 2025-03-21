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
        return newsRepository.findById(id).map(existingNews -> {
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

    public boolean deleteNews(String id) {
        if (newsRepository.existsById(id)) {
            newsRepository.deleteById(id);
            return true;
        }
        return false;
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
}
