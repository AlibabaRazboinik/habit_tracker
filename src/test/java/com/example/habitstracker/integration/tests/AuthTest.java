package com.example.habitstracker.integration.tests;

import com.example.habitstracker.integration.utils.TestUserBuilder;
import com.example.habitstracker.constants.ApiConstants;
import com.example.habitstracker.integration.utils.dsl.TokenHolder;
import com.example.habitstracker.exceptions.UserExistException;
import com.example.habitstracker.exceptions.auth.IncorrectCredentialsException;
import com.example.habitstracker.mappers.UserMapper;
import com.example.habitstracker.models.UserEntity;
import com.example.openapi.dto.ErrorResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;

/**
 * Тесты на механизм авторизации
 */
class AuthTest extends AbstractIntegrationTest {
    @Autowired
    private ObjectMapper objectMapper;
    private UserEntity user;

    @BeforeEach
    public void setup() {
        super.setup();
        user = new TestUserBuilder().build();
    }

    /**
     * Проверка механизма регистрации и логина
     */
    @Test
    void test_registrationAndLogin() throws JsonProcessingException {
        authDSL.register(user);
        authDSL.login(user);

        Assertions.assertNotNull(TokenHolder.token);
        Assertions.assertFalse(TokenHolder.token.isEmpty());
    }

    /**
     * Пробуем зарегистрировать одного и того же пользователя дважды
     */
    @Test
    void test_registerUserTwice() throws JsonProcessingException {
        authDSL.register(user);

        var exception = new UserExistException(user.getUsername());
        var expected = new ErrorResponseDTO()
                .codeError(1)
                .message(exception.getMessage());

        var dto = UserMapper.toDTO(user);

        // @formatter:off
        var response = given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(dto))
            .when()
                .post("/auth/registration")
                .getBody()
                .asString();
        // @formatter:on

        var result = objectMapper.readValue(response, ErrorResponseDTO.class);
        Assertions.assertEquals(expected, result);
    }

    /**
     * Пробуем войти используя неверные данные для входа
     */
    @Test
    void test_incorrectPassword() throws JsonProcessingException, CloneNotSupportedException {
        authDSL.register(user);

        UserEntity newUser = (UserEntity) user.clone();
        newUser.setPassword("X");

        var exception = new IncorrectCredentialsException();
        var expected = new ErrorResponseDTO()
                .codeError(7)
                .message(exception.getMessage());

        var dto = UserMapper.toLoginPassword(newUser);
        // @formatter:off
        var response = given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(dto))
             .when()
                .post(ApiConstants.LOGIN)
                .getBody()
                .asString();
        // @formatter:on

        var result = objectMapper.readValue(response, ErrorResponseDTO.class);
        Assertions.assertEquals(expected, result);
    }
}