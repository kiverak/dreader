package ru.dreader.dreadernews.service;

import org.springframework.stereotype.Component;
import ru.dreader.dreadernews.dto.CategorizingArticleResult;
import ru.dreader.dreadernews.dto.CategorizingResponse;
import ru.dreader.dreadernews.dto.Pair;
import ru.dreader.dreadernews.entity.Article;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class RatingCalculator {

    public List<Long> getHighRatedArticlesToPublish(Pair<CategorizingResponse, List<Article>> categorizingPair) {
        CategorizingResponse categorizingResponse = categorizingPair.first();
        List<Article> articles = categorizingPair.second();

        Map<Long, Article> articleById = articles.stream()
                .collect(Collectors.toMap(Article::getId, a -> a));

        Set<Long> processed = new HashSet<>();
        List<Long> result = new ArrayList<>();

        for (CategorizingArticleResult res : categorizingResponse.results()) {

            Long id = res.id();

            if (processed.contains(id)) {
                continue;
            }

            Set<Long> cluster = new HashSet<>(res.duplicateIds());
            cluster.add(id);

            processed.addAll(cluster);

            List<Article> group = cluster.stream()
                    .map(articleById::get)
                    .filter(Objects::nonNull)
                    .toList();

            Long bestId = findWithHighestRating(group);
            if (bestId != null) {
                result.add(bestId);
            }
        }

        return result;
    }

    private Long findWithHighestRating(List<Article> articles) {
        if (articles == null || articles.isEmpty()) {
            return null;
        }

        // Find max rating
        int maxRating = articles.stream()
                .mapToInt(this::rating)
                .max()
                .orElse(Integer.MIN_VALUE);

        // Articles with max rating
        List<Article> topRated = articles.stream()
                .filter(a -> rating(a) == maxRating)
                .toList();

        if (topRated.size() == 1) {
            return topRated.get(0).getId();
        }

        // Get random
        Article random = topRated.get(new Random().nextInt(topRated.size()));
        return random.getId();
    }

    public int rating(Article a) {
        return safe(a.getCommentsCount()) + safe(a.getViewsCount()) / 1000;
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }

}
