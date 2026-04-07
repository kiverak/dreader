package ru.dreader.dreadernews.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.dreader.dreadernews.entity.ProcessedArticle;

import java.time.Instant;
import java.util.List;

@Repository
public interface ProcessedArticleRepository extends JpaRepository<ProcessedArticle, Long> {
    Page<ProcessedArticle> findByTags_Id(Long tagId, Pageable pageable);

    @Query("""
            SELECT pa FROM ProcessedArticle pa
            LEFT JOIN FETCH pa.tags
            LEFT JOIN FETCH pa.source
            WHERE pa.publicationDate >= CURRENT_TIMESTAMP - 1 DAY
                        AND pa.llmParsed = false
            ORDER BY pa.publicationDate ASC
            """)
    List<ProcessedArticle> findEarliestFor24HoursNotParsed(Pageable pageable);

    @Query("""
            SELECT pa.id FROM ProcessedArticle pa
            WHERE pa.createdAt < :now - 1 DAY
            """)
    List<Long> findOldProcessedArticleIds(Instant now);
}
