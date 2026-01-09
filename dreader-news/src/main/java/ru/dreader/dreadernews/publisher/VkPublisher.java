package ru.dreader.dreadernews.publisher;

import org.springframework.stereotype.Component;
import ru.dreader.dreadernews.entity.Channel;
import ru.dreader.dreadernews.entity.Post;
import ru.dreader.dreadernews.entity.PublishResult;
import ru.dreader.dreadernews.enums.Platform;

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
