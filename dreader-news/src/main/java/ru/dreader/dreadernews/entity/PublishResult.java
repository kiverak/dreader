package ru.dreader.dreadernews.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.dreader.mvc.entity.BaseEntity;

import java.time.Instant;

@Entity
@Table(
        name = "publish_result",
        indexes = {
                @Index(name = "idx_publish_result_post_id", columnList = "post_id"),
                @Index(name = "idx_publish_result_channel_id", columnList = "channel_id"),
                @Index(name = "idx_publish_result_success", columnList = "success")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PublishResult extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @Column(nullable = false)
    private boolean success;

    @Column(length = 255)
    private String externalId;

    @Column(columnDefinition = "text")
    private String errorMessage;

    @Column(nullable = false)
    private Instant publishedAt = Instant.now();
}
