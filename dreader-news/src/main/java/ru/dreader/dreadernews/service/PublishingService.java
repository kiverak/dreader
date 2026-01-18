package ru.dreader.dreadernews.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dreader.dreadernews.dto.CategoryDto;
import ru.dreader.dreadernews.entity.Channel;
import ru.dreader.dreadernews.entity.Post;
import ru.dreader.dreadernews.entity.PublishResult;
import ru.dreader.dreadernews.enums.PostStatus;
import ru.dreader.dreadernews.publisher.Publisher;
import ru.dreader.dreadernews.publisher.PublisherFactory;
import ru.dreader.dreadernews.repo.ChannelRepository;
import ru.dreader.dreadernews.repo.PostRepository;

import java.time.Instant;
import java.util.List;
import java.util.Random;

@Log4j2
@Service
@RequiredArgsConstructor
public class PublishingService {

    private final PublisherFactory publisherFactory;
    private final ChannelRepository channelRepository;
    private final PublishResultService publishResultService;
    private final PostRepository postRepository;

    @Async
    @CacheEvict(value = "publishedPosts", allEntries = true)
    @Transactional
    public void publishPost(CategoryDto category) {
        // находим стратегию для категории // TODO
        int minUpdatePeriodInMinutes = 30;
        int maxUpdatePeriodInMinutes = 60;

        // определяем дату последнего поста
        PageRequest pageRequest = PageRequest.of(0, 1, Sort.by("updatedAt").descending());
        Post latestPublishedPost = postRepository.findPublishedByCategoryId(category.id(), pageRequest).stream().findFirst().orElse(null);

        // if too early to publish then skip
        long diffMinutes = -1;
        if (latestPublishedPost != null) {
            diffMinutes = (Instant.now().toEpochMilli() - latestPublishedPost.getUpdatedAt().toEpochMilli()) / 1000 / 60;
            if (diffMinutes < minUpdatePeriodInMinutes) return;
        }

        // находим пост для публикации по рейтингу и категории
        Post post = getPostToPublish(category, maxUpdatePeriodInMinutes, diffMinutes);
        if (post == null) {
            return;
        }

        // находим каналы для категории
        List<Channel> channels = channelRepository.findByCategories_Id(category.id());

        if (channels.isEmpty()) {
            log.warn("No channels found for category {}", category.id());
            return;
        }

        // запускаем публикацию в каждый канал
        int publicationsCount = 0;
        for (Channel channel : channels) {
            try {
                publish(post, channel);
                publicationsCount++;
            } catch (Exception e) {
                log.error("Failed to publish post {} to channel {}", post.getId(), channel.getId(), e);
            }
        }

        if (publicationsCount == channels.size()) {
            post.setStatus(PostStatus.PUBLISHED);
        } else if (publicationsCount > 0) {
            post.setStatus(PostStatus.PARTIAL);
        } else {
            post.setStatus(PostStatus.FAILED);
        }
        postRepository.save(post);
    }

    private Post getPostToPublish(CategoryDto category, int maxUpdatePeriodInMinutes, long diffMinutes) {
        // TODO remake ratings with google
        // List<Post> posts = postRepository.findUnpublishedWithMaxRateByCategoryId(category.id());
        List<Post> posts = postRepository.findUnpublishedByCategoryId(category.id());

        if (posts.isEmpty()) {
            return null;
        }

        if (posts.size() == 1) {
            return posts.getFirst();
        }

        if (diffMinutes > 0 && diffMinutes < maxUpdatePeriodInMinutes) {
            return null;
        }

        Random random = new Random();
        return posts.get(random.nextInt(posts.size()));
    }

    public void publish(Post post, Channel channel) {
        Publisher publisher = publisherFactory.getPublisher(channel.getPlatform());
        PublishResult result = publisher.publish(post, channel);
        publishResultService.save(result);

        if (result.isSuccess()) {
            log.info("Post {} successfully published to channel {} (Platform: {}, External ID: {})",
                    post.getId(), channel.getName(), channel.getPlatform(), result.getExternalId());
        } else {
            log.error("Failed to publish post {} to channel {} (Platform: {}): {}",
                    post.getId(), channel.getName(), channel.getPlatform(), result.getErrorMessage());
            throw new PublishingException("Failed to publish post " + post.getId() + " to channel " + channel.getId());
        }
    }

    static class PublishingException extends RuntimeException {
        public PublishingException(String message) {
            super(message);
        }
    }
}
