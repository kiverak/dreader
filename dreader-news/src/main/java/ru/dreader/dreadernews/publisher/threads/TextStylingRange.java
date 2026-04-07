package ru.dreader.dreadernews.publisher.threads;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TextStylingRange(
        int offset,
        int length,
        @JsonProperty("styling_info") List<String> stylingInfo
) {
    public TextStylingRange(int offset, int length, List<String> stylingInfo) {
        this.offset = offset;
        this.length = length;
        this.stylingInfo = stylingInfo != null ? stylingInfo : List.of();
    }
}
