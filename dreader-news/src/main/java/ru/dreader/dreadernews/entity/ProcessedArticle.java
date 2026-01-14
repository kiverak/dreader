package ru.dreader.dreadernews.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.dreader.mvc.entity.AuditEntity;
import ru.dreader.mvc.entity.BaseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "processed_article")
public class ProcessedArticle extends AuditEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String shortContent;

    @Column(nullable = false, unique = true)
    private String url;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private Source source;

    private Instant publicationDate;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "rated_tag",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;

    private boolean llmParsed = false;

    private int rate = 0;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProcessedArticle article = (ProcessedArticle) o;
        return Objects.equals(this.getId(), article.getId()) && Objects.equals(url, article.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId(), url);
    }
}