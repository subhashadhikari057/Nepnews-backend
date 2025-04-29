package com.nepnews.models;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "bookmarks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bookmark {

    @Id
    private String id;

    private String userId;      // Refers to the User's ID
    private String articleId;   // Refers to the News article's ID

    @CreatedDate
    private Date savedAt;
}
