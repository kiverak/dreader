package dreadernewsparser.repo;

import dreadernewsparser.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findByUrl(String url);

    @Query("SELECT a FROM Article a WHERE a.pushed = false ORDER BY a.publicationDate ASC LIMIT :limit")
    List<Article> findUnpostedWithLimit(int limit);

    @Query("SELECT a.url FROM Article a WHERE a.url IN :urls")
    Set<String> findExistingUrls(List<String> urls);
}