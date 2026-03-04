package ru.dreader.dreadernews.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.dreader.dreadernews.entity.Channel;
import ru.dreader.dreadernews.enums.Platform;
import ru.dreader.dreadernews.service.ChannelService;
import ru.dreader.dreadernews.service.ThreadsTokenService;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class ThreadsAuthRefresher {

    private final ThreadsTokenService tokenService;
    private final ChannelService channelService;

    @Scheduled(fixedDelay = 24 * 60 * 60 * 1000) // once a day
    public void scheduledRefresh() {
        refresh();
    }

    private void refresh() {
        List<Channel> channels = channelService.getAllByPlatform(Platform.THREADS);
        for (Channel ch : channels) {
            try {
                tokenService.refreshIfNeeded(ch);
                log.info("Successfully checked/refreshed Threads token for channel: {}", ch.getName());
            } catch (Exception e) {
                log.error("Failed to refresh Threads token for channel: {}", ch.getId(), e);
            }
        }
    }

}
