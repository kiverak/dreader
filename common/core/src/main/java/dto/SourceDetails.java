package dto;

import java.util.List;

public record SourceDetails(
        String name,
        String url,
        List<String> defaultTags
) { }