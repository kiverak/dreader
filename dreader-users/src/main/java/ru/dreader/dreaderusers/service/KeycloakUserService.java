package ru.dreader.dreaderusers.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.dreader.dreaderusers.dto.UserDto;
import ru.dreader.mvc.exception.KeycloakException;
import ru.dreader.mvc.exception.UserAlreadyExistsException;
import ru.dreader.mvc.exception.UserNotFoundException;

import javax.management.relation.RoleNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class KeycloakUserService {

    private final WebClient adminWebClient;

    public Map<String, Object> findKeycloakUserByUserId(String userId) {
        log.info("Fetching Keycloak user {}", userId);
        return adminWebClient
                .get()
                .uri("/users/{id}", userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();
    }

    public List<Map<String, Object>> searchKeycloakUsersByEmail(String email) {
        log.info("Searching Keycloak users by email {}", email);
        return adminWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users")
                        .queryParam("email", email)
                        .build()
                )
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Keycloak error: " + body))
                )
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

        String userId = adminWebClient
                .post()
                .uri("/users")
                .bodyValue(payload)
                .retrieve()
                .onStatus(status -> status.value() == 409,
                        resp -> resp.bodyToMono(String.class)
                                .map(body -> new UserAlreadyExistsException(
                                        "User already exists: " + userDto.getEmail() + ". Keycloak says: " + body
                                ))
                )
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> resp.bodyToMono(String.class)
                                .map(body -> new KeycloakException("Keycloak rejected user creation: " + body))
                )
                .toBodilessEntity()
                .map(response -> {
                    URI location = response.getHeaders().getLocation();
                    if (location == null) {
                        throw new KeycloakException("Keycloak did not return Location header for created user");
                    }
                    return location.toString().substring(location.toString().lastIndexOf('/') + 1);
                })
                .block();

        log.info("User with email {} created in Keycloak with ID: {}", userDto.getEmail(), userId);

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
        List<Map<String, Object>> roleRepresentations = new ArrayList<>();
        for (String roleName : roles) {
            Map<String, Object> role = adminWebClient
                    .get()
                    .uri("/roles/{roleName}", roleName)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, resp ->
                            Mono.error(new RoleNotFoundException("Role not found: " + roleName))
                    )
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block();

            roleRepresentations.add(role);
        }

        // 2. Assign roles
        adminWebClient
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

        adminWebClient
                .put()
                .uri("/users/{id}", userId)
                .bodyValue(payload)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        resp -> resp.bodyToMono(String.class)
                                .map(body -> new KeycloakException("Failed to update user: " + body))
                )
                .toBodilessEntity()
                .block();

        // If password is provided, update it
        if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {

            Map<String, Object> passwordPayload = Map.of(
                    "type", "password",
                    "value", userDto.getPassword(),
                    "temporary", false
            );

            adminWebClient
                    .put()
                    .uri("/users/{id}/reset-password", userId)
                    .bodyValue(passwordPayload)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            resp -> resp.bodyToMono(String.class)
                                    .map(body -> new KeycloakException("Failed to reset password: " + body))
                    )
                    .toBodilessEntity()
                    .block();
        }

        log.info("Keycloak user {} updated", userDto.getEmail());
    }

    public void deleteKeycloakUser(String userId) {
        log.info("Deleting Keycloak user {}", userId);

        adminWebClient
                .delete()
                .uri("/users/{id}", userId)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        resp -> resp.bodyToMono(String.class)
                                .map(body -> new KeycloakException("Failed to delete user: " + body))
                )
                .toBodilessEntity()
                .block();

        log.info("User {} deleted from Keycloak", userId);
    }
}
