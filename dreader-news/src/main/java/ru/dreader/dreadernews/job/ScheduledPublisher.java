package ru.dreader.dreadernews.job;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.dreader.dreadernews.dto.CategoryDto;
import ru.dreader.dreadernews.service.CategoryService;
import ru.dreader.dreadernews.service.PublishingService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ScheduledPublisher {

    private final PublishingService publishingService;
    private final CategoryService categoryService;

    @Scheduled(fixedDelay = 60000)
    public void processScheduledPosts() {
        List<CategoryDto> categories = categoryService.getAll();

        for (CategoryDto category : categories) {
            publishingService.publishPost(category);
        }
    }

}
