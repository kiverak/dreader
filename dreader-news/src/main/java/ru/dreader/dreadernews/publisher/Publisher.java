package ru.dreader.dreadernews.publisher;

import ru.dreader.dreadernews.entity.Channel;
import ru.dreader.dreadernews.entity.Post;
import ru.dreader.dreadernews.entity.PublishResult;
import ru.dreader.dreadernews.enums.Platform;

public interface Publisher {
    Platform getPlatform();
    PublishResult publish(Post post, Channel channel);
}
