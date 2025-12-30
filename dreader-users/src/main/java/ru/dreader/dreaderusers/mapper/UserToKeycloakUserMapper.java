package ru.dreader.dreaderusers.mapper;

import org.springframework.stereotype.Component;
import ru.dreader.dreaderusers.entity.UserEntity;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class UserToKeycloakUserMapper {

    public UserEntity toEntity(Map<String, Object> keycloakUser) {
        if (keycloakUser == null || keycloakUser.isEmpty()) {
            return null;
        }

        UserEntity entity = new UserEntity();
        entity.setId((String) keycloakUser.get("id"));
        entity.setEmail((String) keycloakUser.get("email"));
        entity.setUsername((String) keycloakUser.get("username"));

        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        return entity;
    }

    public Map<String, Object> toKeycloakUser(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", entity.getId());
        payload.put("email", entity.getEmail());
        payload.put("username", entity.getUsername());
        payload.put("enabled", true);

        return payload;
    }
}


