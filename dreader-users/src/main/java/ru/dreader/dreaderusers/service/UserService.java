package ru.dreader.dreaderusers.service;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.dreader.dreaderusers.auth.DreaderOidcUser;
import ru.dreader.dreaderusers.dto.UserDto;
import ru.dreader.dreaderusers.dto.UserInfo;
import ru.dreader.dreaderusers.entity.UserEntity;
import ru.dreader.dreaderusers.mapper.UserToUserDtoMapper;
import ru.dreader.dreaderusers.repo.UserRepository;
import ru.dreader.mvc.exception.UserNotFoundException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final KeycloakUserService keycloakUserService;
    private final UserRepository userRepository;
    private final UserToUserDtoMapper userToUserDtoMapper;
    private final KeycloakUserSyncService keycloakUserSyncService;

    @Transactional(readOnly = true)
    public UserInfo getUserInfoByEmail(final String email) {
        return getUserFromRepoByEmail(email);
    }

    @Transactional(readOnly = true)
    public UserInfo getUserInfoById(String id) {
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

    @Transactional(readOnly = true)
    public UserInfo getCurrentUserInfo() {
        DreaderOidcUser oidcUser = getCurrentOidcUser();
        String email = oidcUser.getEmail();
        return getUserFromRepoByEmail(email);
    }

    private UserInfo getUserFromRepoByEmail(String email) {
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

    public DreaderOidcUser getCurrentOidcUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (userIsLogin(auth))
            return (DreaderOidcUser) auth.getPrincipal();
        throw new RuntimeException("There are no login user");
    }

    private boolean userIsLogin(Authentication auth) {
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName());
    }

    @Transactional
    public UserInfo createUser(UserDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(409), "User with this email already exists: " + userDto.getEmail());
        }

        String keycloakUserId = keycloakUserService.createUserInKeycloak(userDto, List.of("ROLE_USER"));

        UserEntity userEntity = userToUserDtoMapper.toUser(userDto);
        userEntity.setId(keycloakUserId);
        userEntity = userRepository.save(userEntity);
        return userToUserDtoMapper.toUserInfo(userEntity);
    }

    @Transactional
    public UserInfo updateUser(String id, UserDto userDto) {
        UserEntity userEntity = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        if (userEntity.isDeleted()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "User with this id deleted: " + id);
        }
        keycloakUserService.updateKeycloakUser(userDto);
        userToUserDtoMapper.updateUserFromDto(userEntity, userDto);
        userEntity = userRepository.save(userEntity);

        return userToUserDtoMapper.toUserInfo(userEntity);
    }

    @Transactional
    public void deleteUser(String id) {
        UserEntity userEntity = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        keycloakUserService.deleteKeycloakUser(id);
        userEntity.setDeleted(true);
        userEntity.setUpdatedAt(Instant.now());
    }
}
