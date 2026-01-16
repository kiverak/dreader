package ru.dreader.dreadernews.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.dreader.dreadernews.entity.Post;

import java.time.Instant;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
            SELECT p FROM Post p
            JOIN p.categories c
            WHERE c.id = :categoryId
              AND (p.status = 'PUBLISHED' OR p.status = 'PARTIAL')
            """)
    List<Post> findPublishedByCategoryId(Long categoryId, Pageable pageable);

    Page<Post> findByCategories_Id(Long categoryId, Pageable pageable);

    @Query("""
            SELECT p FROM Post p
            JOIN p.categories c
            WHERE c.id = :categoryId
              AND p.status = 'PENDING'
              AND p.rate = (
                  SELECT MAX(p2.rate)
                  FROM Post p2
                  JOIN p2.categories c2
                  WHERE c2.id = :categoryId
                    AND p2.status = 'PENDING'
              )
            """)
    List<Post> findUnpublishedWithMaxRateByCategoryId(Long categoryId);

    @Query("""
            SELECT p FROM Post p
            JOIN p.categories c
            WHERE c.id = :categoryId
              AND p.status = 'PENDING'
            """)
    List<Post> findUnpublishedByCategoryId(Long categoryId);

    @Query("""
            SELECT p.id FROM Post p
            WHERE p.status = 'PENDING'
              AND p.createdAt < :now - 1 DAY
            """)
    List<Long> findOldUnpublishedIds(Instant now);
}
