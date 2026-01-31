package ru.dreader.dreadernews.web;

import dto.ArticleDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import dto.SourceDetails;

import java.util.List;

@Component
public class ArticleParserClient {

    @Value("${urls.gateway}")
    private String gatewayUrl;

    private final WebClient webClient;

    public ArticleParserClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(gatewayUrl + "/parser/api").build();
    }

    public List<ArticleDto> getNews() {
        return webClient.get()
                .uri("/news")
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                    response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new RuntimeException("Error getting news: " + errorBody)))
                )
                .bodyToFlux(ArticleDto.class)
                .collectList()
                .block();
    }

    public List<SourceDetails> getAllSources() {
        return webClient.get()
                .uri("/source")
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                    response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new RuntimeException("Error getting sources: " + errorBody)))
                )
                .bodyToFlux(SourceDetails.class)
                .collectList()
                .block();
    }

    public SourceDetails createSource(SourceDetails sourceDetails) {
        return webClient.post()
                .uri("/source")
                .body(Mono.just(sourceDetails), SourceDetails.class)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                    response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new RuntimeException("Error creating source: " + errorBody)))
                )
                .bodyToMono(SourceDetails.class)
                .block();
    }

    public SourceDetails updateSource(Long id, SourceDetails sourceDetails) {
        return webClient.put()
                .uri("/source/{id}", id)
                .body(Mono.just(sourceDetails), SourceDetails.class)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                    response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new RuntimeException("Error updating source: " + errorBody)))
                )
                .bodyToMono(SourceDetails.class)
                .block();
    }

    public Void deleteSource(Long id) {
        return webClient.delete()
                .uri("/source/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                    response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new RuntimeException("Error deleting source: " + errorBody)))
                )
                .bodyToMono(Void.class)
                .block();
    }
}
