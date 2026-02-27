package ru.dreader.dreadernews.publisher.vk;

import org.springframework.stereotype.Component;
import ru.dreader.dreadernews.entity.Channel;
import ru.dreader.dreadernews.entity.Post;
import ru.dreader.dreadernews.entity.PublishResult;
import ru.dreader.dreadernews.enums.Platform;
import ru.dreader.dreadernews.publisher.Publisher;

@Component
public class VkPublisher implements Publisher {

    @Override
    public Platform getPlatform() {
        return Platform.VK;
    }

    @Override
    public PublishResult publish(Post post, Channel channel) {
        String accessToken = channel.getCredentials().get("accessToken");
        String groupId = channel.getCredentials().get("groupId");

        return new PublishResult();
    }
}
