package com.nepnews.services;

import com.nepnews.models.News;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.jdom2.Element;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class RssService {

    public List<News> fetchNewsFromRss(String feedUrl, int limit) {
        List<News> newsList = new ArrayList<>();

        try {
            URL url = new URL(feedUrl);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(url));

            int count = 0;

            for (SyndEntry entry : feed.getEntries()) {
                if (count >= limit) break;

                News news = new News();

                news.setTitle(entry.getTitle());
                news.setContent(entry.getDescription() != null ? entry.getDescription().getValue() : "No content");
                news.setSlug(entry.getTitle().toLowerCase().replaceAll("[^a-z0-9]+", "-"));
                news.setCreatedAt(entry.getPublishedDate() != null ? entry.getPublishedDate() : new Date());
                news.setStatus("PUBLISHED");
                news.setCategory("external");
                news.setAuthorName(feed.getTitle());

                // 🖼️ Image logic
                String imageUrl = null;
                for (SyndEnclosure enclosure : entry.getEnclosures()) {
                    if (enclosure.getType() != null && enclosure.getType().startsWith("image")) {
                        imageUrl = enclosure.getUrl();
                        break;
                    }
                }

                if (imageUrl == null) {
                    List<Element> foreignMarkup = (List<Element>) entry.getForeignMarkup();
                    for (Element element : foreignMarkup) {
                        if ("thumbnail".equalsIgnoreCase(element.getName()) || "content".equalsIgnoreCase(element.getName())) {
                            imageUrl = element.getAttributeValue("url");
                            break;
                        }
                    }
                }

                news.setImageUrl(imageUrl != null ? imageUrl : "https://i.imgur.com/default-placeholder.jpg");

                newsList.add(news);
                count++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return newsList;
    }
}
