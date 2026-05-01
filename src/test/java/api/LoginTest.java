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
@Feature("Авторизация (LOGIN)")
public class LoginTest extends BaseTest {

    @Test
    @DisplayName("Успешная авторизация (LOGIN)")
    public void testSuccessfulLogin() {
        String token = generateValidToken();

        stubFor(post(urlEqualTo("/auth")).willReturn(aResponse().withStatus(200)));
        Allure.step("Мок: внешний сервис /auth отвечает 200 OK");

        given()
                .spec(baseRequest)
                .formParam("token", token)
                .formParam("action", "LOGIN")
                .when()
                .post()
                .then()
                .statusCode(200)
                .body("result", equalTo("OK"));
    }
}