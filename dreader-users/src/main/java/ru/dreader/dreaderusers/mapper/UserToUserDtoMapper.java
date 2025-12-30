package ru.dreader.dreaderusers.mapper;

import org.springframework.stereotype.Component;
import ru.dreader.dreaderusers.dto.UserDto;
import ru.dreader.dreaderusers.dto.UserInfo;
import ru.dreader.dreaderusers.entity.UserEntity;

@Component
public class UserToUserDtoMapper {

    public UserInfo toUserInfo(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setId(String.valueOf(userEntity.getId()));
        userInfo.setEmail(userEntity.getEmail());
        userInfo.setUsername(userEntity.getUsername());
        userInfo.setTelegramAccount(userEntity.getTelegramAccount());
        return userInfo;
    }

    public UserEntity toUser(UserDto userDto) {
        if (userDto == null) {
            return null;
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(userDto.getEmail());
        userEntity.setUsername(userDto.getUsername());
        userEntity.setTelegramAccount(userDto.getTelegramAccount());
        return userEntity;
    }

    public void updateUserFromDto(UserEntity userEntity, UserDto userDto) {
        if (userEntity == null || userDto == null) {
            return;
        }
        if (userDto.getEmail() != null && !userDto.getEmail().isEmpty()) {
            userEntity.setEmail(userDto.getEmail());
        }
        if (userDto.getUsername() != null && !userDto.getUsername().isEmpty()) {
            userEntity.setUsername(userDto.getUsername());
        }
        if (userDto.getTelegramAccount() != null && !userDto.getTelegramAccount().isEmpty()) {
            userEntity.setTelegramAccount(userDto.getTelegramAccount());
        }
        userEntity.setUpdatedAt(userEntity.getUpdatedAt());
    }
}
