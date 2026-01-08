package ru.dreader.dreadernews.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.dreader.dreadernews.entity.Article;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    Page<Article> findByTags_Id(Long tagId, Pageable pageable);
}
