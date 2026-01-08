package ru.dreader.dreadernews.dto;


import ru.dreader.dreadernews.entity.Source;

public record ArticleSourcePair(ArticleDto articleDto, Source source) {
}
