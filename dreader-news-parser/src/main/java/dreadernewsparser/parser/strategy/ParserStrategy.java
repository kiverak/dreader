package dreadernewsparser.parser.strategy;

import dreadernewsparser.config.HttpClientConfig;
import dreadernewsparser.dto.NewsArticle;
import dreadernewsparser.entity.Source;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface ParserStrategy {
    boolean supports(Source source);
    List<String> findNewArticles(Source source) throws IOException;

    default NewsArticle parse(HttpClientConfig httpClientConfig, String url, Source source) throws IOException {
        Document doc = httpClientConfig.prepareRequest(url).get();

        return new NewsArticle(
                source.getId(),
                url,
                getTitle(doc),
                getViewsCount(doc),
                getCommentsCount(doc),
                getContent(doc),
                getShortContent(doc),
                getImageUrl(doc),
                getPublicationDate(doc),
                getTags(doc)
        );
    }

    String getTitle(Document doc);

    Integer getCommentsCount(Document doc);

    Integer getViewsCount(Document doc);

    String getShortContent(Document doc);

    String getContent(Document doc);

    String getImageUrl(Document doc);

    LocalDateTime getPublicationDate(Document doc);

    List<String> getTags(Document doc);
}