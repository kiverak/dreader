package ru.dreader.dreaderusers.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "users")
@Setter
@Getter
public class UserEntity {

    @Id
    private String id; // Keycloak userId

    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    @Column(name = "telegram_account")
    private String telegramAccount;

    @Column
    private boolean deleted = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity userEntity = (UserEntity) o;
        return id.equals(userEntity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return username;
    }
}
