package ru.dreader.dreadernews.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.dreader.dreadernews.entity.Article;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    Page<Article> findByTags_Id(Long tagId, Pageable pageable);

    @Query("""
            SELECT a FROM Article a
            LEFT JOIN FETCH a.tags
            WHERE a.publicationDate >= CURRENT_TIMESTAMP - 1 DAY
                        AND a.llmParsed = false
            ORDER BY a.publicationDate ASC
            """)
    List<Article> findEarliestFor24HoursNotParsed(Pageable pageable);

}
