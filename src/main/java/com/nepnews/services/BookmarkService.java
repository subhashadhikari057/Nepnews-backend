package com.nepnews.services;
import com.nepnews.models.Bookmark;
import com.nepnews.models.News;
import com.nepnews.dto.BookmarkNewsDTO;
import com.nepnews.repositories.BookmarkRepository;
import com.nepnews.repositories.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
//BOOKMARK
@Service
public class BookmarkService {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private NewsRepository newsRepository;

    // Toggle bookmark: add if not exists, remove if exists
    public void toggleBookmark(String userId, String articleId) {
        Optional<Bookmark> existing = bookmarkRepository.findByUserIdAndArticleId(userId, articleId);

        if (existing.isPresent()) {
            bookmarkRepository.deleteByUserIdAndArticleId(userId, articleId);
        } else {
            Bookmark bookmark = Bookmark.builder()
                    .userId(userId)
                    .articleId(articleId)
                    .savedAt(new Date())
                    .build();
            Bookmark saved = bookmarkRepository.save(bookmark);
            if (saved.getId() == null) {
                System.err.println("❌ Failed to persist bookmark. ID is null!");
            } else {
                System.out.println("✅ Bookmark successfully saved with ID: " + saved.getId());
            }
        }
    }

    // Updated method: return full article info + savedAt
    public List<BookmarkNewsDTO> getBookmarksByUser(String userId) {
        List<Bookmark> bookmarks = bookmarkRepository.findByUserId(userId);
        List<BookmarkNewsDTO> bookmarkNewsList = new ArrayList<>();

        for (Bookmark bookmark : bookmarks) {
            Optional<News> newsOpt = newsRepository.findById(bookmark.getArticleId());
            if (newsOpt.isPresent()) {
                BookmarkNewsDTO dto = new BookmarkNewsDTO(newsOpt.get(), bookmark.getSavedAt());
                bookmarkNewsList.add(dto);
            }
        }

        return bookmarkNewsList;
    }
}
