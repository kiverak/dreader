package dreadernewsparser.parser.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dreadernewsparser.config.HttpClientConfig;
import dreadernewsparser.entity.Source;
import dreadernewsparser.utils.ParserUtils;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ThreeDNewsParserStrategy implements ParserStrategy {

    private final HttpClientConfig httpClientConfig;
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(Source source) {
        return source.getName().equals("3DNews");
    }

    @Override
    public List<String> findNewArticles(Source source) throws IOException {
        String url = source.getUrl();
        String rootUrl = ParserUtils.getRootUrl(url);
        Document doc = httpClientConfig.prepareRequest(url).get();

        Elements elements = doc.select("a.entry-header");

        List<String> urls = new ArrayList<>();
        for (Element el : elements) {
            String attr = el.attr("href");
            String newsUrl = rootUrl + attr;
            if (!newsUrl.contains(rootUrl + "http")) {
                urls.add(newsUrl);
            }
        }
        return urls;
    }

    @Override
    public String getTitle(Document doc) {
        return doc.select("h1").first().text();
    }

    @Override
    public Integer getCommentsCount(Document doc) {
        return null;
    }

    @Override
    public Integer getViewsCount(Document doc) {
        return null;
    }

    @Override
    public String getShortContent(Document doc) {
        String[] split = doc.select("div[class=js-mediator-article]").text().split("\\.");
        return split.length > 0 ? split[0] : null;
    }

    @Override
    public String getContent(Document doc) {
        return doc.select("div[id=section-content]").text();
    }

    @Override
    public String getImageUrl(Document doc) {
        return null;
    }

    @Override
    public LocalDateTime getPublicationDate(Document doc) {
        Elements scripts = doc.select("script[type=application/ld+json]");
        for (Element script : scripts) {
            try {
                JsonNode node = objectMapper.readTree(script.data());
                if (node.has("datePublished")) {
                    String date = node.get("datePublished").asText();
                    return ZonedDateTime.parse(date).toLocalDateTime();
                }
            } catch (Exception e) {
                // Игнорируем ошибки парсинга и пробуем следующий способ
            }
        }

        String dateTimeStr = doc.select("span[class=entry-date tttes]").attr("content");
        if (dateTimeStr != null && !dateTimeStr.isEmpty()) {
            return ZonedDateTime.parse(dateTimeStr).toLocalDateTime();
        }

        return LocalDateTime.now();
    }

    @Override
    public List<String> getTags(Document doc) {
        List<String> tags = new ArrayList<>();
        String tagsStr = doc.select("div[class=taglist]").text();
        tagsStr = tagsStr.substring("Теги: ".length());
        String[] tagsArr = tagsStr.split(",");
        for (String t : tagsArr) {
            tags.add(t.trim().toLowerCase());
        }
        return tags;
    }
}