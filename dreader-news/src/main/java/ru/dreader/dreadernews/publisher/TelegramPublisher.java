package ru.dreader.dreadernews.publisher;

import org.springframework.stereotype.Component;
import ru.dreader.dreadernews.entity.Channel;
import ru.dreader.dreadernews.entity.Post;
import ru.dreader.dreadernews.entity.PublishResult;
import ru.dreader.dreadernews.enums.Platform;

@Component
public class TelegramPublisher implements Publisher {

    @Override
    public Platform getPlatform() {
        return Platform.TELEGRAM;
    }

    @Override
    public PublishResult publish(Post post, Channel channel) {
        String botToken = channel.getCredentials().get("botToken");
        String chatId = channel.getCredentials().get("chatId");

        // TODO: отправка через Telegram Bot API
        return new PublishResult();
    }
}
