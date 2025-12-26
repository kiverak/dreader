package ru.dreader.dreadernews.dto;


import ru.dreader.dreadernews.entity.Source;

public record ArticleSourcePair(NewsArticle newsArticle, Source source) {
}
