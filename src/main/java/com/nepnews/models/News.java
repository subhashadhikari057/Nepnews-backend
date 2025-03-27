package com.nepnews.models;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import lombok.*;

import java.util.Date;
import java.util.List;

@Document(collection = "news")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class News {
    @Id
    private String id;
    private String slug; // Human-readable URL identifier
    private String title;
    private String content;
    private String authorId;
    private String editorId;
    private String category;
    private List<String> keywords;
    private String imageUrl;

    @CreatedDate
    private Date createdAt;  // Auto-set when first created

    @LastModifiedDate
    private Date updatedAt;  // Auto-set when modified

    private String status;
    private String createdBy;  // 👈 Add this field if not already present
    private String authorName;

}
