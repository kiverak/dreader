package ru.dreader.dreadernews.job;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.dreader.dreadernews.entity.Post;
import ru.dreader.dreadernews.repo.PostRepository;
import ru.dreader.dreadernews.service.PostPublishingService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ScheduledPublisher {

    private final PostRepository postRepository;
    private final PostPublishingService publishingService;

    @Scheduled(fixedDelay = 5000)
    public void processScheduledPosts() {
        List<Post> posts = postRepository.findReadyToPublish();
        posts.forEach(publishingService::publish);
    }
}
