package dreadernewsparser.parser.strategy;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HabrParserStrategyTest {

    @Mock
    private HttpClientConfig httpClientConfig;
    @Mock
    private Connection connection;

    private HabrParserStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new HabrParserStrategy(httpClientConfig);
    }

    @Test
    void supports_CorrectSource_ReturnsTrue() {
        Source source = new Source();
        source.setName("Habr News");
        assertTrue(strategy.supports(source));
    }

    @Test
    void supports_IncorrectSource_ReturnsFalse() {
        Source source = new Source();
        source.setName("Other Source");
        assertFalse(strategy.supports(source));
    }

    @Test
    void findNewArticles_ParsesUrlsCorrectly() throws IOException {
        String html = """
                <html>
                <body>
                    <h2><a href="/news/123">News 1</a></h2>
                    <h2><a href="/news/456">News 2</a></h2>
                    <h2><a href="https://other.com/news/789">External News</a></h2>
                </body>
                </html>
                """;
        Document doc = Jsoup.parse(html);
        Source source = new Source();
        source.setUrl("https://habr.com/news/");
        source.setName("Habr News");

        when(httpClientConfig.prepareRequest(anyString())).thenReturn(connection);
        when(connection.get()).thenReturn(doc);

        List<String> urls = strategy.findNewArticles(source);

        assertEquals(2, urls.size());
        assertTrue(urls.contains("https://habr.com/news/123"));
        assertTrue(urls.contains("https://habr.com/news/456"));
    }

    @Test
    void getTitle_ExtractsTitle() {
        String html = "<html><body><h1>Article Title</h1></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Article Title", strategy.getTitle(doc));
    }

    @Test
    void getCommentsCount_ExtractsCount() {
        String html = "<html><body><span class=\"value value--contrasted\">Comments: 42</span></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals(42, strategy.getCommentsCount(doc));
    }

    @Test
    void getViewsCount_ExtractsCount() {
        String html = "<html><body><span class=\"tm-icon-counter__value\" title=\"Views: 1234\"></span></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals(1234, strategy.getViewsCount(doc));
    }

    @Test
    void getContent_ExtractsContent() {
        String html = "<html><body><div id=\"post-content-body\">Article Content</div></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Article Content", strategy.getContent(doc));
    }

    @Test
    void getPublicationDate_ExtractsDate() {
        String dateStr = "2023-10-27T10:00:00+00:00";
        String html = "<html><head><meta property=\"aiturec:datetime\" content=\"" + dateStr + "\"></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        
        Instant expected = Instant.parse(dateStr);
        assertEquals(expected, strategy.getPublicationDate(doc));
    }

    @Test
    void getTags_ExtractsTags() {
        String html = "<html><head><meta name=\"keywords\" content=\"tag1, tag2, tag3\"></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        
        List<String> tags = strategy.getTags(doc);
        assertEquals(3, tags.size());
        assertTrue(tags.contains("tag1"));
        assertTrue(tags.contains("tag2"));
        assertTrue(tags.contains("tag3"));
    }
}
