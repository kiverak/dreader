package ru.dreader.dreadernews.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dreader.dreadernews.entity.Channel;

public interface ChannelRepository extends JpaRepository<Channel, Long> {

}
