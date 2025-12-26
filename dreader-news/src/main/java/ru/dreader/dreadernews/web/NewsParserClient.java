package ru.dreader.dreadernews.web;

import ru.dreader.dreadernews.dto.NewsArticle;
import ru.dreader.dreadernews.dto.SourceDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class NewsParserClient {

    private final WebClient webClient;

    public NewsParserClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://dreader-news-parser").build();
    }

    public List<NewsArticle> getNews() {
        return webClient.get()
                .uri("/api/news/getNews")
                .retrieve()
                .bodyToFlux(NewsArticle.class)
                .collectList()
                .block();
    }

    public List<SourceDetails> getAllSources() {
        return webClient.get()
                .uri("/api/source")
                .retrieve()
                .bodyToFlux(SourceDetails.class)
                .collectList()
                .block();
    }

    public SourceDetails getSourceById(Long id) {
        return webClient.get()
                .uri("/api/source/{id}", id)
                .retrieve()
                .bodyToMono(SourceDetails.class)
                .block();
    }

    public SourceDetails createSource(SourceDetails sourceDetails) {
        return webClient.post()
                .uri("/api/source")
                .body(Mono.just(sourceDetails), SourceDetails.class)
                .retrieve()
                .bodyToMono(SourceDetails.class)
                .block();
    }

    public SourceDetails updateSource(Long id, SourceDetails sourceDetails) {
        return webClient.put()
                .uri("/api/source/{id}", id)
                .body(Mono.just(sourceDetails), SourceDetails.class)
                .retrieve()
                .bodyToMono(SourceDetails.class)
                .block();
    }

    public Void deleteSource(Long id) {
        return webClient.delete()
                .uri("/api/source/{id}", id)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}
