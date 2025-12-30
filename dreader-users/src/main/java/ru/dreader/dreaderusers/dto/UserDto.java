package ru.dreader.dreaderusers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDto {
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    private String username;
    
    private String password;

    private String telegramAccount;
}
