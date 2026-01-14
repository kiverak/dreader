package ru.dreader.dreadernews.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.dreader.dreadernews.entity.Tag;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);

    List<Tag> findAllByNameIn(List<String> tags);

    @Query("""
                SELECT t
                FROM Article a
                    JOIN a.tags t
                WHERE a.id = :articleId
            """)
    List<Tag> findByArticleId(Long articleId);

    @Query("""
                SELECT t
                FROM ProcessedArticle pa
                    JOIN pa.tags t
                WHERE pa.id = :processedArticleId
            """)
    List<Tag> findByProcessedArticleId(Long processedArticleId);
}
