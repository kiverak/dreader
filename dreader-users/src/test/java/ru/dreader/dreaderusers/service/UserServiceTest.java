package ru.dreader.dreaderusers.service;

import dto.UserInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;
import ru.dreader.dreaderusers.dto.UserDto;
import ru.dreader.dreaderusers.entity.UserEntity;
import ru.dreader.dreaderusers.mapper.UserToUserDtoMapper;
import ru.dreader.dreaderusers.repo.UserRepository;
import ru.dreader.mvc.exception.UserNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private KeycloakUserService keycloakUserService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserToUserDtoMapper userToUserDtoMapper;

    @Mock
    private KeycloakUserSyncService keycloakUserSyncService;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private UserService userService;

    @Test
    void getUserInfoByEmail_shouldReturnUserInfo_whenExists() {
        // Given
        String email = "test@example.com";
        UserEntity userEntity = new UserEntity();
        userEntity.setId("user1");
        userEntity.setEmail(email);
        userEntity.setDeleted(false);

        UserInfo expectedUserInfo = new UserInfo();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));
        when(userToUserDtoMapper.toUserInfo(userEntity)).thenReturn(expectedUserInfo);

        // When
        UserInfo result = userService.getUserInfoByEmail(email);

        // Then
        assertNotNull(result);
        verify(userRepository).findByEmail(email);
        verify(userToUserDtoMapper).toUserInfo(userEntity);
    }

    @Test
    void getUserInfoByEmail_shouldThrowException_whenUserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(keycloakUserSyncService.syncUserFromKeycloakIfExists(email, true)).thenReturn(null);

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.getUserInfoByEmail(email));
    }

    @Test
    void getUserInfoById_shouldReturnUserInfo_whenExists() {
        // Given
        String userId = "user1";
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setDeleted(false);

        UserInfo expectedUserInfo = new UserInfo();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userToUserDtoMapper.toUserInfo(userEntity)).thenReturn(expectedUserInfo);

        // When
        UserInfo result = userService.getUserInfoById(userId);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(userId);
        verify(userToUserDtoMapper).toUserInfo(userEntity);
    }

    @Test
    void getUserInfoById_shouldThrowException_whenUserDeleted() {
        // Given
        String userId = "user1";
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setDeleted(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        // When & Then
        assertThrows(ResponseStatusException.class, () -> userService.getUserInfoById(userId));
    }

    @Test
    void createUser_shouldCreateNewUser() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setUsername("newuser");
        userDto.setEmail("new@example.com");
        userDto.setPassword("password123");

        UserEntity savedEntity = new UserEntity();
        savedEntity.setId("new-user-id");
        savedEntity.setEmail("new@example.com");

        UserInfo expectedUserInfo = new UserInfo();

        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.empty());
        when(keycloakUserService.createUserInKeycloak(userDto, List.of("ROLE_USER"))).thenReturn("new-user-id");
        when(userToUserDtoMapper.toUser(userDto)).thenReturn(new UserEntity());
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);
        when(userToUserDtoMapper.toUserInfo(savedEntity)).thenReturn(expectedUserInfo);

        // When
        UserInfo result = userService.createUser(userDto);

        // Then
        assertNotNull(result);
        verify(userRepository).findByEmail(userDto.getEmail());
        verify(keycloakUserService).createUserInKeycloak(userDto, List.of("ROLE_USER"));
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void createUser_shouldThrowException_whenEmailAlreadyExists() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setEmail("existing@example.com");

        UserEntity existingEntity = new UserEntity();
        existingEntity.setEmail("existing@example.com");

        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.of(existingEntity));

        // When & Then
        assertThrows(ResponseStatusException.class, () -> userService.createUser(userDto));
        verify(userRepository).findByEmail(userDto.getEmail());
        verify(keycloakUserService, never()).createUserInKeycloak(any(), any());
    }

    @Test
    void updateUser_shouldUpdateUser_whenExists() {
        // Given
        String userId = "user1";
        UserDto updateDto = new UserDto();
        updateDto.setUsername("updated");
        updateDto.setEmail("updated@example.com");

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setDeleted(false);

        UserInfo expectedUserInfo = new UserInfo();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userToUserDtoMapper.toUserInfo(userEntity)).thenReturn(expectedUserInfo);

        // When
        UserInfo result = userService.updateUser(userId, updateDto);

        // Then
        assertNotNull(result);
        verify(userRepository).findById(userId);
        verify(keycloakUserService).updateKeycloakUser(updateDto);
        verify(userToUserDtoMapper).updateUserFromDto(userEntity, updateDto);
        verify(userRepository).save(userEntity);
    }

    @Test
    void deleteUser_shouldDeleteUser_whenExists() {
        // Given
        String userId = "user1";
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository).findById(userId);
        verify(keycloakUserService).deleteKeycloakUser(userId);
        assertTrue(userEntity.isDeleted());
    }

    @Test
    void deleteUser_shouldThrowException_whenUserNotFound() {
        // Given
        String userId = "nonexistent";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));
        verify(userRepository).findById(userId);
        verify(keycloakUserService, never()).deleteKeycloakUser(any());
    }
}
