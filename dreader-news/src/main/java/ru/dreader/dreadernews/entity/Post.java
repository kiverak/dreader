package ru.dreader.dreadernews.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.dreader.dreadernews.enums.PostStatus;
import ru.dreader.mvc.entity.BaseEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "post",
        indexes = {
                @Index(name = "idx_post_status", columnList = "status"),
                @Index(name = "idx_post_scheduled_at", columnList = "scheduledAt")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseEntity {

    @Column(columnDefinition = "text", nullable = false)
    private String text;

    @Column
    private Instant scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PostStatus status = PostStatus.PENDING;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PostMedia> media = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "post_category",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories = new ArrayList<>();

    private int rate = 0;
}
