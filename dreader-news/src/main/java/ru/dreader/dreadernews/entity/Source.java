package ru.dreader.dreadernews.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import ru.dreader.mvc.entity.AuditEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "source")
public class Source extends AuditEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String url;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @BatchSize(size = 20)
    @JoinTable(
        name = "source_default_tags",
        joinColumns = @JoinColumn(name = "source_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> defaultTags = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Source source)) return false;
        return this.getId() != null && this.getId().equals(source.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
