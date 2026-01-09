package dreadernewsparser.parser;

import dreadernewsparser.config.HttpClientConfig;
import dreadernewsparser.dto.ArticleDto;
import dreadernewsparser.entity.Source;
import dreadernewsparser.parser.strategy.ParserStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParserService {

    private final List<ParserStrategy> strategies;
    private final HttpClientConfig httpClientConfig;

    public ArticleDto parse(String url, Source source) throws IOException {
        return getStrategy(source).parse(httpClientConfig, url, source);
    }

    public List<String> findNewArticles(Source source) throws IOException {
        return getStrategy(source).findNewArticles(source);
    }

    private ParserStrategy getStrategy(Source source) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(source))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No strategy found for source: " + source.getName()));
    }
}
