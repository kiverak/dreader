package ru.dreader.dreadernews.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dreader.dreadernews.entity.Source;

import java.util.List;
import java.util.Optional;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {
    Optional<Source> findByName(String name);

    @Query("SELECT s FROM Source s WHERE s.name IN :sourceNames")
    List<Source> findAllBySourceNames(@Param("sourceNames") List<String> sourceNames);

    @Query("""
                SELECT s
                FROM Article a
                    JOIN a.source s
                WHERE a.id = :articleId
            """)
    Optional<Source> findByArticleId(Long articleId);

    @Query("""
                SELECT s
                FROM ProcessedArticle pa
                    JOIN pa.source s
                WHERE pa.id = :processedArticleId
            """)
    Optional<Source> findByProcessedArticleId(Long processedArticleId);
}
