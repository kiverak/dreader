package ru.dreader.dreadernews.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dreader.dreadernews.entity.Channel;
import ru.dreader.dreadernews.entity.ThreadsToken;

import java.util.Optional;

public interface ThreadsTokenRepository extends JpaRepository<ThreadsToken, Long> {
    Optional<ThreadsToken> findByChannel(Channel channel);
}
