package ru.dreader.dreadernews.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.dreader.dreadernews.dto.CategoryDto;
import ru.dreader.dreadernews.entity.Channel;
import ru.dreader.dreadernews.entity.Post;
import ru.dreader.dreadernews.entity.PublishResult;
import ru.dreader.dreadernews.enums.Platform;
import ru.dreader.dreadernews.enums.PostStatus;
import ru.dreader.dreadernews.publisher.Publisher;
import ru.dreader.dreadernews.publisher.PublisherFactory;
import ru.dreader.dreadernews.repo.ChannelRepository;
import ru.dreader.dreadernews.repo.PostRepository;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublishingServiceTest {

    @Mock
    private PublisherFactory publisherFactory;

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private PublishResultService publishResultService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private Publisher publisher;

    @InjectMocks
    private PublishingService publishingService;

    @Test
    void publishPost_shouldPublishPost_whenConditionsAreMet() {
        // Given
        CategoryDto category = new CategoryDto(1L, "Test Category", "en");
        Post post = new Post();
        post.setId(1L);
        post.setStatus(PostStatus.PENDING);
        Channel channel = new Channel();
        channel.setId(1L);
        channel.setPlatform(Platform.TELEGRAM);
        PageRequest pageRequest = PageRequest.of(0, 1, Sort.by("updatedAt").descending());

        when(postRepository.findPublishedByCategoryId(category.id(), pageRequest)).thenReturn(Collections.emptyList());
        when(postRepository.findUnpublishedByCategoryId(category.id())).thenReturn(List.of(post));
        when(channelRepository.findByCategories_Id(category.id())).thenReturn(List.of(channel));
        when(publisherFactory.getPublisher(Platform.TELEGRAM)).thenReturn(publisher);
        PublishResult publishResult = new PublishResult();
        publishResult.setSuccess(true);
        when(publisher.publish(any(Post.class), any(Channel.class))).thenReturn(publishResult);

        // When
        publishingService.publishPost(category);

        // Then
        verify(postRepository).save(post);
        verify(publishResultService).save(any(PublishResult.class));
        assert post.getStatus() == PostStatus.PUBLISHED;
    }

    @Test
    void publishPost_shouldNotPublish_whenTooEarly() {
        // Given
        CategoryDto category = new CategoryDto(1L, "Test Category", "en");
        Post latestPublishedPost = new Post();
        latestPublishedPost.setId(1L);
        latestPublishedPost.setUpdatedAt(Instant.now());
        PageRequest pageRequest = PageRequest.of(0, 1, Sort.by("updatedAt").descending());

        when(postRepository.findPublishedByCategoryId(category.id(), pageRequest)).thenReturn(List.of(latestPublishedPost));

        // When
        publishingService.publishPost(category);

        // Then
        verify(postRepository, never()).findUnpublishedByCategoryId(any());
        verify(postRepository, never()).save(any());
    }

    @Test
    void publishPost_shouldDoNothing_whenNoChannelsFound() {
        // Given
        CategoryDto category = new CategoryDto(1L, "Test Category", "en");
        Post post = new Post();
        post.setId(1L);
        post.setStatus(PostStatus.PENDING);
        PageRequest pageRequest = PageRequest.of(0, 1, Sort.by("updatedAt").descending());

        when(postRepository.findPublishedByCategoryId(category.id(), pageRequest)).thenReturn(Collections.emptyList());
        when(postRepository.findUnpublishedByCategoryId(category.id())).thenReturn(List.of(post));
        when(channelRepository.findByCategories_Id(category.id())).thenReturn(Collections.emptyList());

        // When
        publishingService.publishPost(category);

        // Then
        verify(postRepository, never()).save(any());
    }

    @Test
    void publishPost_shouldDoNothing_whenNoPostsFound() {
        // Given
        CategoryDto category = new CategoryDto(1L, "Test Category", "en");
        PageRequest pageRequest = PageRequest.of(0, 1, Sort.by("updatedAt").descending());

        when(postRepository.findPublishedByCategoryId(category.id(), pageRequest)).thenReturn(Collections.emptyList());
        when(postRepository.findUnpublishedByCategoryId(category.id())).thenReturn(Collections.emptyList());

        // When
        publishingService.publishPost(category);

        // Then
        verify(channelRepository, never()).findByCategories_Id(any());
        verify(postRepository, never()).save(any());
    }

    @Test
    void publishPost_shouldSetPartialStatus_whenOneOfTwoPublicationsFails() {
        // Given
        CategoryDto category = new CategoryDto(1L, "Test Category", "en");
        Post post = new Post();
        post.setId(1L);
        post.setStatus(PostStatus.PENDING);
        Channel channel1 = new Channel();
        channel1.setId(1L);
        channel1.setPlatform(Platform.TELEGRAM);
        Channel channel2 = new Channel();
        channel2.setId(2L);
        channel2.setPlatform(Platform.TELEGRAM);
        PageRequest pageRequest = PageRequest.of(0, 1, Sort.by("updatedAt").descending());

        when(postRepository.findPublishedByCategoryId(category.id(), pageRequest)).thenReturn(Collections.emptyList());
        when(postRepository.findUnpublishedByCategoryId(category.id())).thenReturn(List.of(post));
        when(channelRepository.findByCategories_Id(category.id())).thenReturn(List.of(channel1, channel2));
        when(publisherFactory.getPublisher(Platform.TELEGRAM)).thenReturn(publisher);
        PublishResult publishResult = new PublishResult();
        publishResult.setSuccess(true);
        when(publisher.publish(post, channel1)).thenReturn(publishResult);
        when(publisher.publish(post, channel2)).thenThrow(new RuntimeException("Test Exception"));

        // When
        publishingService.publishPost(category);

        // Then
        verify(postRepository).save(post);
        assert post.getStatus() == PostStatus.PARTIAL;
    }

    @Test
    void publishPost_shouldSetFailedStatus_whenAllPublicationsFail() {
        // Given
        CategoryDto category = new CategoryDto(1L, "Test Category", "en");
        Post post = new Post();
        post.setId(1L);
        post.setStatus(PostStatus.PENDING);
        Channel channel = new Channel();
        channel.setId(1L);
        channel.setPlatform(Platform.TELEGRAM);
        PageRequest pageRequest = PageRequest.of(0, 1, Sort.by("updatedAt").descending());

        when(postRepository.findPublishedByCategoryId(category.id(), pageRequest)).thenReturn(Collections.emptyList());
        when(postRepository.findUnpublishedByCategoryId(category.id())).thenReturn(List.of(post));
        when(channelRepository.findByCategories_Id(category.id())).thenReturn(List.of(channel));
        when(publisherFactory.getPublisher(Platform.TELEGRAM)).thenReturn(publisher);
        when(publisher.publish(any(Post.class), any(Channel.class))).thenThrow(new RuntimeException("Test Exception"));

        // When
        publishingService.publishPost(category);

        // Then
        verify(postRepository).save(post);
        assert post.getStatus() == PostStatus.FAILED;
    }
}
