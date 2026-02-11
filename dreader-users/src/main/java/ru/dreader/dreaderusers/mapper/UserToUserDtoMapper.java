package ru.dreader.dreaderusers.mapper;

import dto.UserInfo;
import org.springframework.stereotype.Component;
import ru.dreader.dreaderusers.dto.UserDto;
import ru.dreader.dreaderusers.entity.UserEntity;

import java.time.Instant;

@Component
public class UserToUserDtoMapper {

    public UserInfo toUserInfo(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }
        UserInfo userInfo = new dto.UserInfo();
        userInfo.setId(String.valueOf(userEntity.getId()));
        userInfo.setEmail(userEntity.getEmail());
        userInfo.setUsername(userEntity.getUsername());
        userInfo.setFirstName(userEntity.getFirstName());
        userInfo.setLastName(userEntity.getLastName());
        userInfo.setTelegramAccount(userEntity.getTelegramAccount());
        userInfo.setDeleted(userEntity.isDeleted());
        return userInfo;
    }

    public UserEntity toUser(UserDto userDto) {
        if (userDto == null) {
            return null;
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(userDto.email());
        userEntity.setUsername(userDto.username());
        userEntity.setFirstName(userDto.firstName());
        userEntity.setLastName(userDto.lastName());
        userEntity.setTelegramAccount(userDto.telegramAccount());
        return userEntity;
    }

    public void updateUserFromDto(UserEntity userEntity, UserDto userDto) {
        if (userEntity == null || userDto == null) {
            return;
        }
        if (userDto.email() != null && !userDto.email().isEmpty()) {
            userEntity.setEmail(userDto.email());
        }
        if (userDto.username() != null && !userDto.username().isEmpty()) {
            userEntity.setUsername(userDto.username());
        }
        if (userDto.firstName() != null && !userDto.firstName().isEmpty()) {
            userEntity.setFirstName(userDto.firstName());
        }
        if (userDto.lastName() != null && !userDto.lastName().isEmpty()) {
            userEntity.setLastName(userDto.lastName());
        }
        if (userDto.telegramAccount() != null && !userDto.telegramAccount().isEmpty()) {
            userEntity.setTelegramAccount(userDto.telegramAccount());
        }
        if (userDto.deleted() != null) {
            userEntity.setDeleted(userDto.deleted());
        }
        userEntity.setUpdatedAt(Instant.now());
    }
}
