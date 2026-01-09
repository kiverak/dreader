package dreadernewsparser.dto;

import dreadernewsparser.entity.Source;

public record ArticleSourcePair(ArticleDto articleDto, Source source) {
}
