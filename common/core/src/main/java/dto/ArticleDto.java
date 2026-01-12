package dto;

import java.time.Instant;
import java.util.List;

public record ArticleDto(
        Long id,
        String url,
        String title,
        String sourceName,
        Integer viewsCount,
        Integer commentsCount,
        String content,
        String shortContent,
        String imageUrl,
        Instant publicationDate,
        List<String> tags
) {
}