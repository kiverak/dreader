package ru.dreader.dreadernews.publisher;

import org.springframework.stereotype.Component;
import ru.dreader.dreadernews.enums.Platform;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PublisherFactory {

    private final Map<Platform, Publisher> publishers;

    public PublisherFactory(List<Publisher> publisherList) {
        this.publishers = publisherList.stream()
                .collect(Collectors.toMap(Publisher::getPlatform, p -> p));
    }

    public Publisher getPublisher(Platform platform) {
        return publishers.get(platform);
    }
}
