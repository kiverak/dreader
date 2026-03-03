package ru.dreader.dreadernews.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.dreader.dreadernews.entity.Channel;
import ru.dreader.dreadernews.enums.Platform;

import java.util.List;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {

    List<Channel> findByCategories_Id(Long categoryId);

    List<Channel> findAllByPlatform(Platform platform);
}
