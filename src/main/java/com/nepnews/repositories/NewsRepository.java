package com.nepnews.repositories;

import com.nepnews.models.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface NewsRepository extends MongoRepository<News, String> {

    // Find news by category
    List<News> findByCategory(String category);

    // Find news by keywords (case insensitive)
    List<News> findByKeywordsContainingIgnoreCase(String keyword);

    // Find news by slug (for frontend-friendly URLs)
    Optional<News> findBySlug(String slug);

    // Find news by category (case insensitive)
    List<News> findByCategoryIgnoreCase(String category);

    // ✅ NEW: Search news by title OR keywords (pagination support)
    Page<News> findByTitleContainingIgnoreCaseOrKeywordsContainingIgnoreCase(String title, String keyword, Pageable pageable);

    List<News> findByStatus(String status);
    List<News> findByCreatedBy(String createdBy);
    List<News> findByCreatedByAndStatus(String createdBy, String status);

    List<News> findByStatus(String status, Pageable pageable);


}
