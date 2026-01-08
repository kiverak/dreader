package dreadernewsparser.dto;

import java.util.List;

public record SourceDetails(
        Long id,
        String name,
        String url,
        List<String> defaultTags
) { }