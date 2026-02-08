package dreadernewsparser.parser;

import dreadernewsparser.config.HttpClientConfig;
import dreadernewsparser.entity.Source;
import dreadernewsparser.parser.strategy.ParserStrategy;
import dto.ArticleDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParserServiceTest {

    @Mock
    private ParserStrategy strategy1;
    @Mock
    private ParserStrategy strategy2;
    @Mock
    private HttpClientConfig httpClientConfig;

    private ParserService parserService;

    @BeforeEach
    void setUp() {
        parserService = new ParserService(List.of(strategy1, strategy2), httpClientConfig);
    }

    @Test
    void parse_SupportedStrategyFound_CallsStrategyParse() throws IOException {
        Source source = new Source();
        source.setName("Source1");
        String url = "http://example.com/article";
        ArticleDto expectedDto = new ArticleDto(null, url, "Title", "Source1", 0, 0, "Content", "Short", null, null, List.of());

        when(strategy1.supports(source)).thenReturn(true);
        when(strategy1.parse(httpClientConfig, url, source)).thenReturn(expectedDto);

        ArticleDto result = parserService.parse(url, source);

        assertEquals(expectedDto, result);
        verify(strategy1).parse(httpClientConfig, url, source);
        verify(strategy2, never()).parse(any(), any(), any());
    }

    @Test
    void parse_NoSupportedStrategy_ThrowsException() {
        Source source = new Source();
        source.setName("Unknown");
        String url = "http://example.com/article";

        when(strategy1.supports(source)).thenReturn(false);
        when(strategy2.supports(source)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> parserService.parse(url, source));
    }

    @Test
    void findNewArticles_SupportedStrategyFound_CallsStrategyFindNewArticles() throws IOException {
        Source source = new Source();
        source.setName("Source2");
        List<String> expectedUrls = List.of("url1", "url2");

        when(strategy1.supports(source)).thenReturn(false);
        when(strategy2.supports(source)).thenReturn(true);
        when(strategy2.findNewArticles(source)).thenReturn(expectedUrls);

        List<String> result = parserService.findNewArticles(source);

        assertEquals(expectedUrls, result);
        verify(strategy2).findNewArticles(source);
    }
}
