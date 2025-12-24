package dto;

import entity.Source;

public record ArticleSourcePair(NewsArticle newsArticle, Source source) {
}
