package ru.dreader.dreaderusers.controller;

import lombok.extern.log4j.Log4j2;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.dreader.dreaderusers.dto.UserDto;
import ru.dreader.dreaderusers.keycloak.KeycloakUtils;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("admin/user") // базовый URI
public class AdminController {

    public static final int CONFLICT = 409;
    public static final String USER_ROLE_NAME = "user";

    private final KeycloakUtils keycloakUtils;

    public AdminController(KeycloakUtils keycloakUtils) {
        this.keycloakUtils = keycloakUtils;
    }

    @PostMapping("/add")
    public ResponseEntity add(@RequestBody UserDto userDto) {

//        if (userDto.getId() != null && userDto.getId() != 0) {
//            return new ResponseEntity("redundant param: id MUST be null", HttpStatus.NOT_ACCEPTABLE);
//        }

        if (userDto.getEmail() == null || userDto.getEmail().trim().length() == 0) {
            return new ResponseEntity("missed param: email", HttpStatus.NOT_ACCEPTABLE);
        }

        if (userDto.getPassword() == null || userDto.getPassword().trim().length() == 0) {
            return new ResponseEntity("missed param: password", HttpStatus.NOT_ACCEPTABLE);
        }

        if (userDto.getUsername() == null || userDto.getUsername().trim().length() == 0) {
            return new ResponseEntity("missed param: username", HttpStatus.NOT_ACCEPTABLE);
        }

        Response response = keycloakUtils.createKeycloakUser(userDto);

        if (response.getStatus() == CONFLICT) {
            return new ResponseEntity("user or email already exists: " + userDto.getEmail(), HttpStatus.CONFLICT);
        }

        String userId = CreatedResponseUtil.getCreatedId(response);
        log.info("User created with userId: {}", userId);

        List<String> defaultRoles = new ArrayList<>();
        defaultRoles.add(USER_ROLE_NAME);
        keycloakUtils.addRoles(userId, defaultRoles);

        return ResponseEntity.status(response.getStatus()).build();
    }

    @PutMapping("/update")
    public ResponseEntity<UserRepresentation> update(@RequestBody UserDto userDto) {
        if (userDto == null || userDto.getId().isBlank()) {
            return new ResponseEntity("missed param: id", HttpStatus.NOT_ACCEPTABLE);
        }

        keycloakUtils.updateKeycloakUser(userDto);

        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/deletebyid")
    public ResponseEntity deleteByUserId(@RequestBody String userId) {
        keycloakUtils.deleteKeycloakUser(userId);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/id")
    public ResponseEntity<UserRepresentation> findById(@RequestBody String userId) {
        return ResponseEntity.ok(keycloakUtils.findKeycloakUserByUserId(userId));
    }

    @PostMapping("/searchByEmail")
    public ResponseEntity<List<UserRepresentation>> searchByEmail(@RequestBody String email) {

        return ResponseEntity.ok(keycloakUtils.searchKeycloakUsersByEmail(email));
    }

}