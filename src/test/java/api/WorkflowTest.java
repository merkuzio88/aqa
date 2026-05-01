package api;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Epic("Тестовое задание QA")
@Feature("Бизнес-логика сессий (Workflow)")
public class WorkflowTest extends BaseTest {

    @Test
    @DisplayName("Успешное выполнение ACTION после успешного LOGIN")
    public void testActionAfterSuccessfulLogin() {
        String token = generateValidToken();

        stubFor(post(urlEqualTo("/auth")).willReturn(aResponse().withStatus(200)));
        stubFor(post(urlEqualTo("/doAction")).willReturn(aResponse().withStatus(200)));

        Allure.step("Шаг 1: Успешный LOGIN", () -> {
            given().spec(baseRequest)
                    .formParam("token", token)
                    .formParam("action", "LOGIN")
                    .post()
                    .then().statusCode(200).body("result", equalTo("OK"));
        });

        Allure.step("Шаг 2: Выполнение ACTION с тем же токеном", () -> {
            given().spec(baseRequest)
                    .formParam("token", token)
                    .formParam("action", "ACTION")
                    .post()
                    .then().statusCode(200).body("result", equalTo("OK"));
        });
    }

    @Test
    @DisplayName("Успешный LOGOUT убивает сессию (ACTION не работает)")
    public void testLogoutKillsSession() {
        String token = generateValidToken();

        stubFor(post(urlEqualTo("/auth")).willReturn(aResponse().withStatus(200)));

        Allure.step("Шаг 1: Успешный LOGIN", () -> {
            given().spec(baseRequest).formParam("token", token).formParam("action", "LOGIN")
                    .post().then().statusCode(200);
        });

        Allure.step("Шаг 2: Успешный LOGOUT", () -> {
            given().spec(baseRequest).formParam("token", token).formParam("action", "LOGOUT")
                    .post().then().statusCode(200).body("result", equalTo("OK"));
        });

        Allure.step("Шаг 3: Попытка сделать ACTION после LOGOUT (должна быть ошибка)", () -> {
            given().spec(baseRequest).formParam("token", token).formParam("action", "ACTION")
                    .post().then()
                    .body("result", equalTo("ERROR"));
        });
    }

    @Test
    @DisplayName("Отказ в ACTION без предварительного LOGIN")
    public void testActionWithoutLogin() {
        String token = generateValidToken();
        stubFor(post(urlEqualTo("/doAction")).willReturn(aResponse().withStatus(200)));

        given().spec(baseRequest)
                .formParam("token", token)
                .formParam("action", "ACTION")
                .post()
                .then()
                .body("result", equalTo("ERROR"));
    }
}