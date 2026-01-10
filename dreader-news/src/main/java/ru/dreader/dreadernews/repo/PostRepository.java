package ru.dreader.dreadernews.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.dreader.dreadernews.entity.Post;
import ru.dreader.dreadernews.entity.PublishResult;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p WHERE p.status = 'PENDING' " +
            "AND (p.scheduledAt IS NULL OR p.scheduledAt <= CURRENT_TIMESTAMP) " +
            "AND NOT EXISTS (SELECT pr FROM PublishResult pr WHERE pr.post = p AND pr.success = true AND pr.channel IN ELEMENTS(p.channels))")
    List<Post> findReadyToPublish();
}
