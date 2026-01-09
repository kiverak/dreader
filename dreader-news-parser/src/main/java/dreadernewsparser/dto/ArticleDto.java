package dreadernewsparser.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ArticleDto(
        String url,
        String title,
        String sourceName,
        Integer viewsCount,
        Integer commentsCount,
        String content,
        String shortContent,
        String imageUrl,
        LocalDateTime publicationDate,
        List<String> tags
) {}