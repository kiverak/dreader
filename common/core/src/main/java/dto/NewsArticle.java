package dto;

import java.time.LocalDateTime;
import java.util.List;

public record NewsArticle(
        String url,
        String title,
        Integer viewsCount,
        Integer commentsCount,
        String content,
        String shortContent,
        String imageUrl,
        LocalDateTime publicationDate,
        List<String> tags
) {}