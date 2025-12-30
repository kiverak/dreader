package ru.dreader.dreaderusers.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.dreader.dreaderusers.entity.UserEntity;
import ru.dreader.dreaderusers.mapper.UserToKeycloakUserMapper;
import ru.dreader.dreaderusers.repo.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeycloakUserSyncService {

    private final KeycloakUserService keycloakUserService;
    private final UserRepository userRepository;
    private final UserToKeycloakUserMapper mapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserEntity syncUserFromKeycloakIfExists(String emailOrId, boolean searchByEmail) {
        Map<String, Object> kcUser;
        if (searchByEmail) {
            List<Map<String, Object>> users = keycloakUserService.searchKeycloakUsersByEmail(emailOrId);
            if (users.isEmpty()) {
                return null;
            }
            kcUser = users.get(0);
        } else {
            kcUser = keycloakUserService.findKeycloakUserByUserId(emailOrId);
            if (kcUser == null || kcUser.isEmpty()) {
                return null;
            }
        }

        UserEntity entity = mapper.toEntity(kcUser);

        return userRepository.save(entity);
    }
}
