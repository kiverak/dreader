package ru.dreader.dreadernews.dto;

import java.util.List;

public record ParseRequest(String url, String title, String body, List<String> rawTags, String language) {
}
