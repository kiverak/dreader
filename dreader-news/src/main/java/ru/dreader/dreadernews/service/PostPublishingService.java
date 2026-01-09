package ru.dreader.dreadernews.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dreader.dreadernews.entity.Channel;
import ru.dreader.dreadernews.entity.Post;
import ru.dreader.dreadernews.entity.PublishResult;
import ru.dreader.dreadernews.publisher.Publisher;
import ru.dreader.dreadernews.publisher.PublisherFactory;
import ru.dreader.dreadernews.repo.ChannelRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostPublishingService {

    private final PublisherFactory publisherFactory;
    private final ChannelRepository channelRepository;
    private final PublishResultRepository resultRepository;

    @Transactional
    public void publish(Post post) {

        List<Channel> channels = channelRepository.findAllById(post.getChannelIds());

        for (Channel channel : channels) {
            Publisher publisher = publisherFactory.getPublisher(channel.getPlatform());
            PublishResult result = publisher.publish(post, channel);
            resultRepository.save(result);
        }
    }
}

