package ru.dreader.dreadernews.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dreader.dreadernews.dto.ArticleResponse;
import ru.dreader.dreadernews.dto.ParseRequest;
import ru.dreader.dreadernews.entity.Article;
import ru.dreader.dreadernews.entity.Post;
import ru.dreader.dreadernews.entity.Tag;
import ru.dreader.dreadernews.mapper.ArticlePostMapper;
import ru.dreader.dreadernews.service.ArticleService;
import ru.dreader.dreadernews.service.PostService;
import ru.dreader.dreadernews.web.LLMParserClient;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class ArticlesLLMParsingProcessor {

    private final LLMParserClient llmParserClient;
    private final PostService postService;
    private final ArticleService articleService;
    private final ArticlePostMapper articlePostMapper;

    /**
     * @return true если статья обработана, false если работы не было
     */
    @Transactional
    protected boolean processOneArticle() {
        var article = articleService.getEarliestForDayReadyToPost();

        if (article == null) {
            try {
                Thread.sleep(5000); // нет работы — подождём 5 секунд
            } catch (InterruptedException ignored) {
            }
            return false;
        }

        log.info("Getting article {} for LLM parsing", article.getId());
        ParseRequest request = createParseRequest(article);
        ArticleResponse response = llmParserClient.parseArticle(request);
        log.info("Article {} LLM parsed: {}", article.getId(), response);

        Post post = articlePostMapper.map(article, response);
        postService.save(post);
        article.setLlmParsed(true);
        log.info("Fresh post saved id: {}", post.getId());

        return true;
    }

    private ParseRequest createParseRequest(Article article) {
        String url = article.getUrl();
        String title = article.getTitle();
        String body = article.getContent();
        List<String> rawTags = article.getTags().stream().map(Tag::getName).toList();
        String language = "ru";

        return new ParseRequest(url, title, body, rawTags, language);
    }

}
