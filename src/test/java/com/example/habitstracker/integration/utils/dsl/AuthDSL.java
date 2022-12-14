package com.example.habitstracker.integration.utils.dsl;

import static io.restassured.RestAssured.given;

import java.util.HashMap;

import com.example.habitstracker.integration.utils.CleanerService;
import com.example.habitstracker.constants.ApiConstants;
import com.example.habitstracker.models.UserEntity;
import com.example.habitstracker.services.MapperService;
import com.example.openapi.dto.UserDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * Инструменты для взаимодействия с api авторизации
 */
@Component
public class AuthDSL {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserDSL userDSL;
    @Autowired
    private MapperService mapper;

    /**
     * Зарегистрировать нового пользователя
     */
    public void register(UserEntity user) throws JsonProcessingException {
        var dto = new UserDTO();
        mapper.transform(user, dto);

        // @formatter:off
        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(dto))
            .when()
                .post(ApiConstants.Auth.REGISTRATION)
            .then()
                .statusCode(200);
        // @formatter:on

        CleanerService.addTask(() -> {
            DSLHelper.selectUsername(user.getUsername());
            if (DSLHelper.getToken() == null) {
                try {
                    login(user);
                } catch (JsonProcessingException e) {
                    Assertions.fail("Can't login. Casued by: " + e.getMessage());
                }
            }

            if (DSLHelper.getToken() != null) {
                userDSL.deleteUser();
            }
        });
    }

    public void registerWithoutCleaner(UserEntity user) throws JsonProcessingException {
        var dto = new UserDTO();
        mapper.transform(user, dto);

        // @formatter:off
        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(dto))
                .when()
                .post(ApiConstants.Auth.REGISTRATION)
                .then()
                .statusCode(200);
        // @formatter:on
    }

    /**
     * Отправить запрос на регистрацию пользователя и получить ответ
     *
     * @return Ответ на запрос
     */
    public String sendRegistrationRequest(UserEntity user) throws JsonProcessingException {
        var dto = new UserDTO();
        mapper.transform(user, dto);

        // @formatter:off
        var result = given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(dto))
            .when()
                .post(ApiConstants.Auth.REGISTRATION);
        // @formatter:on

        if (result.getStatusCode() == 200) {
            CleanerService.addTask(() -> {
                DSLHelper.selectUsername(user.getUsername());
                if (DSLHelper.getToken() == null) {
                    try {
                        login(user);
                    } catch (JsonProcessingException e) {
                        Assertions.fail("Can't login. Casued by: " + e.getMessage());
                    }
                }

                if (DSLHelper.getToken() != null) {
                    userDSL.deleteUser();
                }
            });
        }

        return result.getBody().asString();
    }

    /**
     * Залогинится в системе
     *
     * @param user Пользователь, под котором логинимся
     */
    public void login(UserEntity user) throws JsonProcessingException {
        var values = new HashMap<String, String>();
        values.put("username", user.getUsername());
        values.put("password", user.getPassword());
        var json = objectMapper.writeValueAsString(values);

        // @formatter:off
        var response = given()
                .contentType(ContentType.JSON)
                .body(json)
            .when()
                .post(ApiConstants.Auth.LOGIN)
            .then()
                .statusCode(200);
        // @formatter:on

        DSLHelper.selectUsername(user.getUsername());
        DSLHelper.setToken(response.extract().header(HttpHeaders.AUTHORIZATION).split(" ")[1]);
    }
}
