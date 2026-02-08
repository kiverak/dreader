package ru.dreader.dreadernews.service;

import org.junit.jupiter.api.Test;
import ru.dreader.dreadernews.dto.CategorizingArticleResult;
import ru.dreader.dreadernews.dto.CategorizingResponse;
import ru.dreader.dreadernews.dto.Pair;
import ru.dreader.dreadernews.entity.Article;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RatingCalculatorTest {

    private final RatingCalculator ratingCalculator = new RatingCalculator();

    @Test
    void rating_CalculatesCorrectly() {
        Article article = new Article();
        article.setCommentsCount(10);
        article.setViewsCount(2000);

        // 10 + 2000/1000 = 10 + 2 = 12
        assertEquals(12, ratingCalculator.rating(article));
    }

    @Test
    void rating_NullValues_CalculatesAsZero() {
        Article article = new Article();
        article.setCommentsCount(null);
        article.setViewsCount(null);

        assertEquals(0, ratingCalculator.rating(article));
    }

    @Test
    void getHighRatedArticlesToPublish_SingleGroup_ReturnsBestArticle() {
        // Article 1: rating 10 + 1 = 11
        Article a1 = new Article();
        a1.setId(1L);
        a1.setCommentsCount(10);
        a1.setViewsCount(1000);

        // Article 2: rating 5 + 0 = 5
        Article a2 = new Article();
        a2.setId(2L);
        a2.setCommentsCount(5);
        a2.setViewsCount(500);

        // Article 3: rating 20 + 2 = 22 (Best)
        Article a3 = new Article();
        a3.setId(3L);
        a3.setCommentsCount(20);
        a3.setViewsCount(2000);

        List<Article> articles = List.of(a1, a2, a3);

        // Response says a1 is the main one, and a2, a3 are duplicates
        CategorizingArticleResult result = new CategorizingArticleResult(1L, Collections.emptyList(), List.of(2L, 3L));
        CategorizingResponse response = new CategorizingResponse(List.of(result));

        Pair<CategorizingResponse, List<Article>> pair = new Pair<>(response, articles);

        List<Long> highRatedIds = ratingCalculator.getHighRatedArticlesToPublish(pair);

        assertEquals(1, highRatedIds.size());
        assertEquals(3L, highRatedIds.get(0)); // a3 has the highest rating
    }

    @Test
    void getHighRatedArticlesToPublish_MultipleGroups_ReturnsBestFromEach() {
        // Group 1
        Article a1 = new Article();
        a1.setId(1L);
        a1.setCommentsCount(100); // High rating

        Article a2 = new Article();
        a2.setId(2L);
        a2.setCommentsCount(10);

        // Group 2
        Article a3 = new Article();
        a3.setId(3L);
        a3.setCommentsCount(5);

        Article a4 = new Article();
        a4.setId(4L);
        a4.setCommentsCount(50); // High rating

        List<Article> articles = List.of(a1, a2, a3, a4);

        CategorizingArticleResult res1 = new CategorizingArticleResult(1L, Collections.emptyList(), List.of(2L));
        CategorizingArticleResult res2 = new CategorizingArticleResult(3L, Collections.emptyList(), List.of(4L));
        CategorizingResponse response = new CategorizingResponse(List.of(res1, res2));

        Pair<CategorizingResponse, List<Article>> pair = new Pair<>(response, articles);

        List<Long> highRatedIds = ratingCalculator.getHighRatedArticlesToPublish(pair);

        assertEquals(2, highRatedIds.size());
        assertTrue(highRatedIds.contains(1L));
        assertTrue(highRatedIds.contains(4L));
    }

    @Test
    void getHighRatedArticlesToPublish_EqualRatings_ReturnsOneRandomly() {
        Article a1 = new Article();
        a1.setId(1L);
        a1.setCommentsCount(10);

        Article a2 = new Article();
        a2.setId(2L);
        a2.setCommentsCount(10);

        List<Article> articles = List.of(a1, a2);

        CategorizingArticleResult res = new CategorizingArticleResult(1L, Collections.emptyList(), List.of(2L));
        CategorizingResponse response = new CategorizingResponse(List.of(res));

        Pair<CategorizingResponse, List<Article>> pair = new Pair<>(response, articles);

        List<Long> highRatedIds = ratingCalculator.getHighRatedArticlesToPublish(pair);

        assertEquals(1, highRatedIds.size());
        assertTrue(highRatedIds.contains(1L) || highRatedIds.contains(2L));
    }

    @Test
    void getHighRatedArticlesToPublish_ProcessedDuplicatesSkipped() {
        // If a duplicate is listed as a main article in another result, it should be skipped if already processed
        // But in this logic:
        // processed.addAll(cluster);
        // if (processed.contains(id)) continue;

        Article a1 = new Article();
        a1.setId(1L);
        Article a2 = new Article();
        a2.setId(2L);

        List<Article> articles = List.of(a1, a2);

        // Result 1: 1 is main, 2 is duplicate
        CategorizingArticleResult res1 = new CategorizingArticleResult(1L, Collections.emptyList(), List.of(2L));
        // Result 2: 2 is main (should be skipped because it was processed in group 1)
        CategorizingArticleResult res2 = new CategorizingArticleResult(2L, Collections.emptyList(), Collections.emptyList());

        CategorizingResponse response = new CategorizingResponse(List.of(res1, res2));

        Pair<CategorizingResponse, List<Article>> pair = new Pair<>(response, articles);

        List<Long> highRatedIds = ratingCalculator.getHighRatedArticlesToPublish(pair);

        assertEquals(1, highRatedIds.size());
        // Should pick one from group {1, 2}
        assertTrue(highRatedIds.contains(1L) || highRatedIds.contains(2L));
    }
}
