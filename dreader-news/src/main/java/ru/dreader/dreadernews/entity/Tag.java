package ru.dreader.dreadernews.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import ru.dreader.mvc.entity.AuditEntity;

import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "tag")
public class Tag extends AuditEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Tag tag)) return false;
        return Objects.equals(this.getId(), tag.getId()) && Objects.equals(name, tag.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId(), name);
    }
}