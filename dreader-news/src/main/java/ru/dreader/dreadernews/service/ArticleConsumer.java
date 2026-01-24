package ru.dreader.dreadernews.service;

import dto.ArticleDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleConsumer {

    private final ArticleService articleService;

    @KafkaListener(topics = "${kafka.topics.articlesTopic}", groupId = "dreader-news-group")
    public void consume(ArticleDto article, Acknowledgment ack) {
        try {
            process(article);
            ack.acknowledge();
        } catch (Exception e) {
            throw e; // go to retry → DLQ
        }
    }

    // TODO add batching
    private void process(ArticleDto dto) {
        boolean exist = articleService.checkIfExist(dto.url());
        if (exist) return;
        articleService.save(dto);
        log.info("Article saved: {}", dto.title());
    }
}
