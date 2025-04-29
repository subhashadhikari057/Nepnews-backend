package com.nepnews.controllers;

import com.nepnews.dto.BookmarkNewsDTO;
import com.nepnews.services.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
@CrossOrigin(origins = "*") // Adjust origin based on your frontend domain in production
public class BookmarkController {

    @Autowired
    private BookmarkService bookmarkService;

    @PostMapping("/toggle")
    public ResponseEntity<?> toggleBookmark(@RequestParam String userId, @RequestParam String articleId) {
        bookmarkService.toggleBookmark(userId, articleId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<BookmarkNewsDTO>> getUserBookmarks(@PathVariable String userId) {
        return ResponseEntity.ok(bookmarkService.getBookmarksByUser(userId));
    }
}
