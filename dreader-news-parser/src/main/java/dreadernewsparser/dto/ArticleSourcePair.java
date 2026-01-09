package dreadernewsparser.dto;

import dreadernewsparser.entity.Source;
import dto.ArticleDto;

public record ArticleSourcePair(ArticleDto articleDto, Source source) {
}
