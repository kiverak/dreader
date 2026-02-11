package ru.dreader.dreaderusers.service;

import dto.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.dreader.dreaderusers.dto.UserDto;
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
    private final UserCacheService userCacheService;

    @Transactional(readOnly = true)
    public dto.UserInfo getUserInfoByEmail(final String email) {
        return userCacheService.getCachedUserFromRepoByEmail(email);
    }

    @Transactional(readOnly = true)
    public dto.UserInfo getUserInfoById(String id) {
        return userCacheService.getCachedUserInfoById(id);
    }

    @Transactional(readOnly = true)
    public dto.UserInfo getCurrentUserInfo() {
        Jwt jwt = getCurrentJwt();
        String email = jwt.getClaimAsString("email");
        return userCacheService.getCachedUserFromRepoByEmail(email);
    }

    @Transactional(readOnly = true)
    public Page<UserInfo> getUsers(Integer size, Integer page, String sort, String order) {
        Sort sortBy = Sort.by(Sort.Direction.fromString(order != null ? order : "DESC"), sort);
        Pageable pageable = PageRequest.of(page, size, sortBy);
        Page<UserEntity> userPage = userRepository.findAll(pageable);

        return userPage.map(userToUserDtoMapper::toUserInfo);
    }

    public Jwt getCurrentJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (userIsLogin(auth)) {
            Object principal = auth.getPrincipal();
            if (principal instanceof Jwt) {
                return (Jwt) principal;
            }
            throw new ClassCastException("Principal is not a Jwt: " + principal.getClass());
        }
        throw new RuntimeException("There are no login user");
    }

    private boolean userIsLogin(Authentication auth) {
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName());
    }

    @Transactional
    public dto.UserInfo createUser(UserDto userDto) {
        if (userRepository.findByEmail(userDto.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(409), "User with this email already exists: " + userDto.email());
        }

        String keycloakUserId = keycloakUserService.createUserInKeycloak(userDto, List.of("ROLE_USER"));

        UserEntity userEntity = userToUserDtoMapper.toUser(userDto);
        userEntity.setId(keycloakUserId);
        userEntity = userRepository.save(userEntity);
        return userToUserDtoMapper.toUserInfo(userEntity);
    }

    @Transactional
    @CacheEvict(cacheNames = "user", key = "#id")
    public dto.UserInfo updateUser(String id, UserDto userDto) {
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
    @CacheEvict(cacheNames = "user", key = "#id")
    public void deleteUser(String id) {
        UserEntity userEntity = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        keycloakUserService.deleteKeycloakUser(id);
        userEntity.setDeleted(true);
        userEntity.setUpdatedAt(Instant.now());
    }
}
