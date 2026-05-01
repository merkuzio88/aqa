package api;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        Allure.step("Отправка запроса с невалидным X-Api-Key");
        given()
                .header("X-Api-Key", "WRONG_KEY_123")
                .contentType("application/x-www-form-urlencoded")
                .formParam("token", generateValidToken())
                .formParam("action", "LOGIN")
                .when()
                .post(APP_URL)
                .then()
                .statusCode(anyOf(equalTo(401), equalTo(403)));
    }

    @Test
    @DisplayName("Ошибка валидации при длине токена меньше 32 символов")
    public void testInvalidTokenLength() {
        String shortToken = "1234567890123456789012345678901";

        given().spec(baseRequest)
                .formParam("token", shortToken)
                .formParam("action", "LOGIN")
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Обработка ошибки 500 от внешнего сервиса")
    public void testExternalServiceError() {
        String token = generateValidToken();

        stubFor(post(urlEqualTo("/auth")).willReturn(aResponse().withStatus(500)));
        Allure.step("Мок: внешний сервис /auth сломался и отвечает 500");

        given().spec(baseRequest)
                .formParam("token", token)
                .formParam("action", "LOGIN")
                .post()
                .then()
                .body("result", equalTo("ERROR"));
    }
}