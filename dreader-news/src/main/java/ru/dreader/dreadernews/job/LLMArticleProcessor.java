package ru.dreader.dreadernews.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dreader.dreadernews.dto.*;
import ru.dreader.dreadernews.entity.*;
import ru.dreader.dreadernews.mapper.ProcessedArticleMapper;
import ru.dreader.dreadernews.mapper.ProcessedArticlePostMapper;
import ru.dreader.dreadernews.repo.ArticleRepository;
import ru.dreader.dreadernews.repo.ProcessedArticleRepository;
import ru.dreader.dreadernews.service.ArticleService;
import ru.dreader.dreadernews.service.CategoryService;
import ru.dreader.dreadernews.service.PostService;
import ru.dreader.dreadernews.service.ProcessedArticleService;
import ru.dreader.dreadernews.web.LLMParserClient;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class LLMArticleProcessor {

    private final int BUNCH_SIZE = 50;

    private final LLMParserClient llmParserClient;
    private final PostService postService;
    private final ArticleService articleService;
    private final ProcessedArticleService processedArticleService;
    private final ArticleRepository articleRepository;
    private final ProcessedArticleRepository processedArticleRepository;
    private final ProcessedArticlePostMapper processedArticlePostMapper;
    private final CategoryService categoryService;
    private final ProcessedArticleMapper processedArticleMapper;

    /**
     * @return true если статья обработана, false если работы не было
     */
    @Transactional
    protected boolean parseOneArticle() {
        var processedArticle = processedArticleService.getEarliestForDayReadyToPost();

        if (processedArticle == null) {
            try {
                Thread.sleep(5000); // нет работы — подождём 5 секунд
            } catch (InterruptedException ignored) {
            }
            return false;
        }

        log.info("Getting processedArticle {} for LLM parsing", processedArticle.getId());
        ParseRequest request = createParseRequest(processedArticle);
        ArticleResponse response = llmParserClient.parseArticle(request);
        log.info("ProcessedArticle {} LLM parsed: {}", processedArticle.getId(), response);

        Post post = processedArticlePostMapper.map(processedArticle, response);
        postService.save(post);
        processedArticle.setLlmParsed(true);
        log.info("Fresh post saved id: {}", post.getId());

        return true;
    }

    private ParseRequest createParseRequest(ProcessedArticle article) {
        String url = article.getUrl();
        String title = article.getTitle();
        String body = article.getContent();
        List<String> rawTags = article.getTags().stream().map(Tag::getName).toList();
        String language = "ru";

        return new ParseRequest(url, title, body, rawTags, language);
    }

    @Transactional(readOnly = true)
    public Pair<CategorizingResponse, List<Article>> categorizeArticleBunch() {

        List<Article> articles = articleService.getEarliestForDayReadyToPostBunch(BUNCH_SIZE);

        if (articles.isEmpty()) {
            try {
                Thread.sleep(5000); // нет работы — подождём 5 секунд
            } catch (InterruptedException ignored) {
            }
            return null;
        }

        log.info("Getting articles for LLM categorization: {}", articles.size());
        CategorizingRequest request = createCategorizeRequest(articles);
        CategorizingResponse response = llmParserClient.categorizeArticles(request);
        log.info("Articles LLM categorized: {}", articles.size());

        return new Pair<>(response, articles);
    }

    private CategorizingRequest createCategorizeRequest(List<Article> articles) {
        List<CategorizingArticleRequest> articlesRequest = articles.stream()
            .map(article -> new CategorizingArticleRequest(
                    article.getId(),
                    article.getTitle(),
                    article.getTags().stream().map(Tag::getName).toList()
            ))
            .toList();

        List<CategorizingCategoryRequest> categoriesRequest = categoryService.getAll().stream()
                .map(category -> new CategorizingCategoryRequest(
                        category.id(),
                        category.name()
                ))
                .toList();

        return new CategorizingRequest(articlesRequest, categoriesRequest);
    }

    public void deleteArticlesWithLowRatingAndDuplicates(List<Article> articles, List<Long> articlesToPublishIds) {
        List<Long> idsToDelete = new ArrayList<>();
        for (Article article : articles) {
            if (!articlesToPublishIds.contains(article.getId())) {
                idsToDelete.add(article.getId());
            }
        }

        articleService.delete(idsToDelete);
    }

    @Transactional
    public void createProcessedArticlesToPublish(List<Long> articlesToPublishIds, List<CategorizingArticleResult> results) {
        List<ProcessedArticle> processedArticles = new ArrayList<>();
        for (Long id : articlesToPublishIds) {
            Article article = articleRepository.findById(id).get();

            CategorizingArticleResult res = results.stream()
                    .filter(r -> r.id().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new LLMException("LLM exception"));
            List<Category> categoriesByIds = categoryService.getCategoriesByIds(res.matchedCategoryIds());

            ProcessedArticle processedArticle = processedArticleMapper.map(article);
            processedArticle.setCategories(categoriesByIds);

            processedArticles.add(processedArticle);
        }

        processedArticleRepository.saveAll(processedArticles);
        articleService.delete(articlesToPublishIds);
    }

    static class LLMException extends RuntimeException {
        public LLMException(String message) {
            super(message);
        }
    }
}
