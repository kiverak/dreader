package ru.dreader.dreadernews.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.dreader.dreadernews.dto.ArticleResponse;
import ru.dreader.dreadernews.dto.CategorizingRequest;
import ru.dreader.dreadernews.dto.CategorizingResponse;
import ru.dreader.dreadernews.dto.ParseRequest;

@Component
public class LLMParserClient {

    private final WebClient webClient;

    public LLMParserClient(WebClient.Builder webClientBuilder,
                           @Value("${urls.llm-parser}") String url) {
        this.webClient = webClientBuilder.baseUrl(url + "/api").build();
    }

    public ArticleResponse parseArticle(final ParseRequest request) {
        return webClient.post()
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

    public CategorizingResponse categorizeArticles(final CategorizingRequest request) {
        return webClient.post()
                .uri("/categorize")
                .body(Mono.just(request), CategorizingRequest.class)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("Error categorize article: " + errorBody)))
                )
                .bodyToMono(CategorizingResponse.class)
                .block();
    }
}
