package ru.dreader.dreaderusers.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.dreader.dreaderusers.entity.UserEntity;
import ru.dreader.dreaderusers.mapper.UserToUserDtoMapper;
import ru.dreader.dreaderusers.repo.UserRepository;
import ru.dreader.mvc.exception.UserNotFoundException;

@Service
@RequiredArgsConstructor
public class UserCacheService {

    private final UserRepository userRepository;
    private final UserToUserDtoMapper userToUserDtoMapper;
    private final KeycloakUserSyncService keycloakUserSyncService;

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#email")
    public dto.UserInfo getCachedUserFromRepoByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseGet(() -> keycloakUserSyncService.syncUserFromKeycloakIfExists(email, true));
        if (userEntity == null) {
            throw new UserNotFoundException("User not found with email: " + email);
        }
        if (userEntity.isDeleted()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "User with this email deleted: " + email);
        }
        return userToUserDtoMapper.toUserInfo(userEntity);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
    public dto.UserInfo getCachedUserInfoById(String id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseGet(() -> keycloakUserSyncService.syncUserFromKeycloakIfExists(id, false));
        if (userEntity == null) {
            throw new UserNotFoundException("User not found with keycloakId: " + id);
        }
        if (userEntity.isDeleted()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "User with this id deleted: " + id);
        }
        return userToUserDtoMapper.toUserInfo(userEntity);
    }

}
