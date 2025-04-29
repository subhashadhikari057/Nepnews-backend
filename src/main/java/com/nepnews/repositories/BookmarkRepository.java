package com.nepnews.repositories;

import com.nepnews.models.Bookmark;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends MongoRepository<Bookmark, String> {
    List<Bookmark> findByUserId(String userId);
    Optional<Bookmark> findByUserIdAndArticleId(String userId, String articleId);
    void deleteByUserIdAndArticleId(String userId, String articleId);
}
