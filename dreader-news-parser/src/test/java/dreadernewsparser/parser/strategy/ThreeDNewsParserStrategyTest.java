package dreadernewsparser.parser.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import dreadernewsparser.config.HttpClientConfig;
import dreadernewsparser.entity.Source;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThreeDNewsParserStrategyTest {

    @Mock
    private HttpClientConfig httpClientConfig;
    @Mock
    private Connection connection;
    
    private ObjectMapper objectMapper = new ObjectMapper();

    private ThreeDNewsParserStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ThreeDNewsParserStrategy(httpClientConfig, objectMapper);
    }

    @Test
    void supports_CorrectSource_ReturnsTrue() {
        Source source = new Source();
        source.setName("3DNews");
        assertTrue(strategy.supports(source));
    }

    @Test
    void findNewArticles_ParsesUrlsCorrectly() throws IOException {
        String html = """
                <html>
                <body>
                    <a class="entry-header" href="/news/123">News 1</a>
                    <a class="entry-header" href="/news/456">News 2</a>
                </body>
                </html>
                """;
        Document doc = Jsoup.parse(html);
        Source source = new Source();
        source.setUrl("https://3dnews.ru/news/");
        source.setName("3DNews");

        when(httpClientConfig.prepareRequest(anyString())).thenReturn(connection);
        when(connection.get()).thenReturn(doc);

        List<String> urls = strategy.findNewArticles(source);

        assertEquals(2, urls.size());
        assertTrue(urls.contains("https://3dnews.ru/news/123"));
        assertTrue(urls.contains("https://3dnews.ru/news/456"));
    }

    @Test
    void getTitle_ExtractsTitle() {
        String html = "<html><body><h1>Article Title</h1></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Article Title", strategy.getTitle(doc));
    }

    @Test
    void getShortContent_ExtractsFirstSentence() {
        String html = "<html><body><div class=\"js-mediator-article\">First sentence. Second sentence.</div></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("First sentence", strategy.getShortContent(doc));
    }

    @Test
    void getContent_ExtractsContent() {
        String html = "<html><body><div id=\"section-content\">Article Content</div></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Article Content", strategy.getContent(doc));
    }

    @Test
    void getPublicationDate_ExtractsDateFromJsonLd() {
        String dateStr = "2023-10-27T10:00:00+00:00";
        String jsonLd = "{\"datePublished\": \"" + dateStr + "\"}";
        String html = "<html><head><script type=\"application/ld+json\">" + jsonLd + "</script></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        
        Instant expected = Instant.parse(dateStr);
        assertEquals(expected, strategy.getPublicationDate(doc));
    }

    @Test
    void getTags_ExtractsTags() {
        String html = "<html><body><div class=\"taglist\">Теги: tag1, tag2</div></body></html>";
        Document doc = Jsoup.parse(html);
        
        List<String> tags = strategy.getTags(doc);
        assertEquals(2, tags.size());
        assertTrue(tags.contains("tag1"));
        assertTrue(tags.contains("tag2"));
    }
}
