package dreadernewsparser.service;

import dto.ArticleDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Log4j2
@Service
@RequiredArgsConstructor
public class ArticleProducer {

    @Value("${kafka.topics.articlesTopic}")
    private String articlesTopic;

    private final KafkaTemplate<String, ArticleDto> kafkaTemplate;

    public void sendArticle(ArticleDto article) {
        int hash = Objects.hash(article.url());
        kafkaTemplate.send(articlesTopic, String.valueOf(hash), article)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.info("Failed to send article: {}", ex.getMessage());
                    } else {
                        log.info("Article {} sent to partition {}", article.title(), result.getRecordMetadata().partition());
                    }
                });
    }
}
