package api;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.restassured.AllureRestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.anyOf;

@Epic("Тестовое задание QA")
@Feature("Негативные сценарии и валидация")
public class NegativeTest extends BaseTest {

    @Test
    @DisplayName("Ошибка доступа при неверном X-Api-Key")
    public void testInvalidApiKey() {
        Allure.step("Отправка запроса с невалидным X-Api-Key", () -> {
            given()
                    .baseUri(appUrl)
                    .header("X-Api-Key", "WRONG_KEY_123")
                    .contentType("application/x-www-form-urlencoded")
                    .filter(new AllureRestAssured())
                    .formParam("token", generateValidToken())
                    .formParam("action", "LOGIN")
                    .when()
                    .post()
                    .then()
                    .statusCode(anyOf(equalTo(401), equalTo(403)));
        });
    }

    @ParameterizedTest(name = "Ошибка валидации при отправке токена: \"{0}\"")
    @ValueSource(strings = {
            "1234567890123456789012345678901",  // 31 символ (меньше)
            "123456789012345678901234567890123", // 33 символа (больше)
            "",                                  // Пустой токен
            "USER1234567890123456789012345678"   // Токен с буквами
    })
    @DisplayName("Валидация форматов токена")
    public void testInvalidTokenFormats(String invalidToken) {
        Allure.step("Попытка авторизации с токеном: " + invalidToken, () -> {
            given().spec(baseRequest)
                    .formParam("token", invalidToken)
                    .formParam("action", "LOGIN")
                    .post()
                    .then()
                    .statusCode(400);
        });
    }

    @Test
    @DisplayName("Обработка ошибки 500 от внешнего сервиса")
    public void testExternalServiceError() {
        String token = generateValidToken();

        stubFor(post(urlEqualTo("/auth")).willReturn(aResponse().withStatus(500)));
        Allure.step("Мок: внешний сервис /auth сломался и отвечает 500");

        Allure.step("Попытка авторизации при недоступном внешнем сервисе", () -> {
            given().spec(baseRequest)
                    .formParam("token", token)
                    .formParam("action", "LOGIN")
                    .post()
                    .then()
                    .body("result", equalTo("ERROR"));
        });
    }
}