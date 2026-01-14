package ru.dreader.dreadernews.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.dreader.mvc.entity.AuditEntity;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "article")
public class Article extends AuditEntity {

    @Column(nullable = false)
    private String title;

    private Integer viewsCount;
    private Integer commentsCount;

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
            name = "article_tag",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Article article = (Article) o;
        return Objects.equals(this.getId(), article.getId()) && Objects.equals(url, article.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId(), url);
    }
}