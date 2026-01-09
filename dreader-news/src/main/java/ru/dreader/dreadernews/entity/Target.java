package ru.dreader.dreadernews.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.dreader.dreadernews.enums.Platform;
import ru.dreader.mvc.entity.AuditEntity;

@Entity
@Getter
@Setter
@Table(name = "target")
public class Target extends AuditEntity {

    @Column(nullable = false, unique = true)
    private String name;

    private Platform targetType;

    private Long targetId;

    private String key;

    private int minUpdatePeriodInMinutes = 30;

}
