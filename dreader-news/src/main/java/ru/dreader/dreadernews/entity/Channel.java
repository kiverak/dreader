package ru.dreader.dreadernews.entity;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import ru.dreader.dreadernews.enums.Platform;
import ru.dreader.mvc.entity.AuditEntity;

import java.util.List;
import java.util.Map;

@Entity
@Table(
        name = "channel",
        indexes = {
                @Index(name = "idx_channel_platform", columnList = "platform"),
                @Index(name = "idx_channel_name", columnList = "name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Channel extends AuditEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Platform platform;

    @Column(nullable = false)
    private String name;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, String> credentials;

    @Column(nullable = false)
    private int minUpdatePeriodInMinutes = 30;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "category_tag",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Category> category;
}
