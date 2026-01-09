package ru.dreader.dreadernews.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import ru.dreader.dreadernews.enums.Platform;
import ru.dreader.mvc.entity.AuditEntity;

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

    @Column(nullable = false, length = 255)
    private String name;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, String> credentials;
}
