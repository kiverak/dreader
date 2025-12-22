package dreadernewsparser.parser.strategy;

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
public class HabrParserStrategy implements ParserStrategy {

    private final HttpClientConfig httpClientConfig;

    @Override
    public boolean supports(Source source) {
        return source.getName().equals("Habr News");
    }

    @Override
    public List<String> findNewArticles(Source source) throws IOException {
        String url = source.getUrl();
        String rootUrl = ParserUtils.getRootUrl(url);
        Document doc = httpClientConfig.prepareRequest(url).get();

        Elements elements = doc.select("h2");

        List<String> urls = new ArrayList<>();
        for (Element el : elements) {
            String attr = el.select("a").attr("href");
            String newsUrl = rootUrl + attr;
            if (newsUrl.startsWith(url)) {
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
        String text = doc.select("span[class=value value--contrasted]").text();
        text = text.replaceAll("\\D", "");
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {}
        return 0;
    }

    @Override
    public Integer getViewsCount(Document doc) {
        String text = doc.select("span[class=tm-icon-counter__value]").attr("title");
        text = text.replaceAll("\\D", "");
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {}
        return 0;
    }

    @Override
    public String getContent(Document doc) {
        return doc.select("div[id=post-content-body]").text();
    }

    @Override
    public String getShortContent(Document doc) {
        return null;
    }

    @Override
    public String getImageUrl(Document doc) {
        return null;
    }

    @Override
    public LocalDateTime getPublicationDate(Document doc) {
        String dateTimeStr = doc.select("meta[property=aiturec:datetime]").attr("content");
        if (dateTimeStr != null && !dateTimeStr.isEmpty()) {
            return ZonedDateTime.parse(dateTimeStr).toLocalDateTime();
        }
        return LocalDateTime.now();
    }

    @Override
    public List<String> getTags(Document doc) {
        List<String> tags = new ArrayList<>();
        String tagsStr = doc.select("meta[name=keywords]").attr("content");
        String[] tagsArr = tagsStr.split(",");
        for (String t : tagsArr) {
            tags.add(t.trim());
        }
        return tags;
    }
}