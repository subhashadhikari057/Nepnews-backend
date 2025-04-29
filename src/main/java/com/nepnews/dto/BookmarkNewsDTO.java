package com.nepnews.dto;

import com.nepnews.models.News;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkNewsDTO {
    private News article;   // News article full object
    private Date savedAt;   // When it was bookmarked
}
