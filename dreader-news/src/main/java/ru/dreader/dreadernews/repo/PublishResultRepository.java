package ru.dreader.dreadernews.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dreader.dreadernews.entity.Channel;
import ru.dreader.dreadernews.entity.PublishResult;

public interface PublishResultRepository extends JpaRepository<PublishResult, Long> {

}
