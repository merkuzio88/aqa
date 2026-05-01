package api;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.Random;

public abstract class BaseTest {

    protected static WireMockServer wireMockServer;
    protected static RequestSpecification baseRequest;

    protected static final String APP_URL = "http://localhost:8080/endpoint";
    protected static final String API_KEY = "qazWSXedc";

    @BeforeAll
    public static void globalSetup() {
        wireMockServer = new WireMockServer(8888);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8888);

        baseRequest = new RequestSpecBuilder()
                .setBaseUri(APP_URL)
                .addHeader("X-Api-Key", API_KEY)
                .setContentType(ContentType.URLENC)
                .setAccept(ContentType.JSON)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .addFilter(new AllureRestAssured())
                .build();
    }

    @BeforeEach
    public void resetMocks() {
        WireMock.reset();
    }

    @AfterAll
    public static void globalTearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Step("Генерация нового случайного токена (только цифры для обхода бага валидации)")
    protected String generateValidToken() {
        StringBuilder token = new StringBuilder();
        java.util.Random rnd = new java.util.Random();
        while (token.length() < 32) {
            token.append(rnd.nextInt(10));
        }
        return token.toString();
    }
}