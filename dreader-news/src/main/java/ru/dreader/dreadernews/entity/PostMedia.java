package ru.dreader.dreadernews.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.dreader.dreadernews.enums.MediaType;
import ru.dreader.mvc.entity.BaseEntity;

@Entity
@Table(
        name = "post_media",
        indexes = {
                @Index(name = "idx_post_media_post_id", columnList = "post_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostMedia extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MediaType type;

    @Column(nullable = false, columnDefinition = "text")
    private String url;

    @Column(nullable = false)
    private Integer position;
}
