package ru.dreader.dreadernews.web;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.dreader.dreadernews.dto.ArticleResponse;
import ru.dreader.dreadernews.dto.ParseRequest;
import ru.dreader.dreadernews.entity.Article;

@Component
public class LLMParserClient {

    private final WebClient defaultWebClient;

    public LLMParserClient(WebClient.Builder webClientBuilder) {
        this.defaultWebClient = webClientBuilder.baseUrl("http://localhost:18080/api").build();
    }

    public ArticleResponse parseArticle(final ParseRequest request) {
        return defaultWebClient.post()
                .uri("/parse")
                .body(Mono.just(request), ParseRequest.class)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("Error parsing article: " + errorBody)))
                )
                .bodyToMono(ArticleResponse.class)
                .block();
    }
}
