package ru.dreader.dreaderusers.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.dreader.dreaderusers.dto.UserDto;
import ru.dreader.mvc.exception.UserNotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class KeycloakUserService {

    private final WebClient keycloakWebClient;

    public Map<String, Object> findKeycloakUserByUserId(String userId) {
        log.info("Fetching Keycloak user {}", userId);
        return keycloakWebClient
                .get()
                .uri("/users/{id}", userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();
    }

    public List<Map<String, Object>> searchKeycloakUsersByEmail(String email) {
        log.info("Searching Keycloak users by email {}", email);
        return keycloakWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users")
                        .queryParam("email", email)
                        .build()
                )
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                })
                .block();
    }

    public String createUserInKeycloak(UserDto userDto, List<String> roles) {
        log.info("Creating user in Keycloak: {}", userDto.getEmail());
        Map<String, Object> payload = Map.of(
                "username", userDto.getUsername(),
                "email", userDto.getEmail(),
                "enabled", true,
                "credentials", List.of(
                        Map.of(
                                "type", "password",
                                "value", userDto.getPassword(),
                                "temporary", false
                        )
                )
        );

        String userId = keycloakWebClient
                .post()
                .uri("/users")
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    String location = response.getHeaders().getLocation().toString();
                    return location.substring(location.lastIndexOf('/') + 1);
                })
                .block();

        log.info("User created in Keycloak with ID: {} and email: {}", userId, userDto.getEmail());

        if (roles != null && !roles.isEmpty()) {
            assignRolesToUser(userId, roles);
        }

        return userId;
    }
//    В Keycloak роль должна существовать заранее:
//    либо вручную в админке
//    либо через миграцию Keycloak
//    либо через Admin API при старте сервиса
//    Если роли нет — Keycloak вернёт 404

    public void assignRolesToUser(String userId, List<String> roles) {
        log.info("Assigning roles {} to user {}", roles, userId);

        // 1. Get all roles by names
        List<Map<String, Object>> roleRepresentations = roles.stream()
                .map(roleName ->
                        keycloakWebClient
                                .get()
                                .uri("/roles/{roleName}", roleName)
                                .retrieve()
                                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                                })
                                .block()
                )
                .toList();

        // 2. Assign roles
        keycloakWebClient
                .post()
                .uri("/users/{id}/role-mappings/realm", userId)
                .bodyValue(roleRepresentations)
                .retrieve()
                .toBodilessEntity()
                .block();

        log.info("Roles {} assigned to user {}", roles, userId);
    }

    public void updateKeycloakUser(UserDto userDto) {
        log.info("Updating Keycloak user {}", userDto.getEmail());
        List<Map<String, Object>> users = searchKeycloakUsersByEmail(userDto.getEmail());

        if (users.isEmpty()) {
            throw new UserNotFoundException("User not found in Keycloak with email: " + userDto.getEmail());
        }

        String userId = (String) users.getFirst().get("id");

        // Update profile
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", userDto.getEmail());
        payload.put("username", userDto.getUsername());
        payload.put("enabled", true);

        keycloakWebClient
                .put()
                .uri("/users/{id}", userId)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .block();

        // If password is provided, update it
        if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {

            Map<String, Object> passwordPayload = Map.of(
                    "type", "password",
                    "value", userDto.getPassword(),
                    "temporary", false
            );

            keycloakWebClient
                    .put()
                    .uri("/users/{id}/reset-password", userId)
                    .bodyValue(passwordPayload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        }

        log.info("Keycloak user {} updated", userDto.getEmail());
    }

    public void deleteKeycloakUser(String userId) {
        log.info("Deleting Keycloak user {}", userId);
        keycloakWebClient
                .delete()
                .uri("/users/{id}", userId)
                .retrieve()
                .toBodilessEntity()
                .block();

        log.info("User {} deleted from Keycloak", userId);
    }
}
