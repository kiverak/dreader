package dreadernewsparser.dto;

import dreadernewsparser.entity.Source;

public record ArticleSourcePair(NewsArticle newsArticle, Source source) {
}
