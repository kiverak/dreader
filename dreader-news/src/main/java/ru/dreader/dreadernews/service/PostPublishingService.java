package ru.dreader.dreadernews.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dreader.dreadernews.entity.Channel;
import ru.dreader.dreadernews.entity.Post;
import ru.dreader.dreadernews.entity.PublishResult;
import ru.dreader.dreadernews.publisher.Publisher;
import ru.dreader.dreadernews.publisher.PublisherFactory;
import ru.dreader.dreadernews.enums.PostStatus;
import ru.dreader.dreadernews.repo.ChannelRepository;
import ru.dreader.dreadernews.repo.PostRepository;
import ru.dreader.dreadernews.repo.PublishResultRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class PostPublishingService {

    private final PublisherFactory publisherFactory;
    private final ChannelRepository channelRepository;
    private final PublishResultRepository resultRepository;
    private final PostRepository postRepository;

    @Transactional
    public void publish(Post post) {
        Set<Long> targetChannelIds = post.getChannels().stream().map(Channel::getId).collect(Collectors.toSet());
        List<Channel> channels = channelRepository.findAllById(targetChannelIds);

        int successfulPublishes = 0;
        for (Channel channel : channels) {
            Publisher publisher = publisherFactory.getPublisher(channel.getPlatform());
            PublishResult result = publisher.publish(post, channel);
            resultRepository.save(result);

            if (result.isSuccess()) {
                successfulPublishes++;
                log.info("Post {} successfully published to channel {} (Platform: {}, External ID: {})",
                        post.getId(), channel.getName(), channel.getPlatform(), result.getExternalId());
            } else {
                log.error("Failed to publish post {} to channel {} (Platform: {}): {}",
                        post.getId(), channel.getName(), channel.getPlatform(), result.getErrorMessage());
            }
        }

        if (successfulPublishes == targetChannelIds.size()) {
            post.setStatus(PostStatus.PUBLISHED);
        } else if (successfulPublishes > 0) {
            post.setStatus(PostStatus.PARTIAL);
        } else {
            post.setStatus(PostStatus.FAILED);
        }
        postRepository.save(post);
    }
}
