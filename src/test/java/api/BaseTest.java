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

import java.io.InputStream;
import java.util.Properties;
import java.util.Random;

public abstract class BaseTest {

    protected static WireMockServer wireMockServer;
    protected static RequestSpecification baseRequest;

    protected static String appUrl;
    protected static String apiKey;
    protected static int mockPort;

    @BeforeAll
    public static void globalSetup() {
        // Читаем файл properties перед запуском тестов
        loadProperties();

        // Поднимаем мок-сервер на порту из конфига
        wireMockServer = new WireMockServer(mockPort);
        wireMockServer.start();
        WireMock.configureFor("localhost", mockPort);

        // Настраиваем RestAssured с URL и ключом из конфига
        baseRequest = new RequestSpecBuilder()
                .setBaseUri(appUrl)
                .addHeader("X-Api-Key", apiKey)
                .setContentType(ContentType.URLENC)
                .setAccept(ContentType.JSON)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .addFilter(new AllureRestAssured())
                .build();
    }

    private static void loadProperties() {
        // Читаем файл application.properties из папки src/test/resources
        try (InputStream input = BaseTest.class.getClassLoader().getResourceAsStream("application.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                throw new RuntimeException("Файл application.properties не найден в папке resources");
            }
            prop.load(input);

            // Инициализируем переменные
            appUrl = prop.getProperty("base.url");
            apiKey = prop.getProperty("api.key");
            mockPort = Integer.parseInt(prop.getProperty("mock.port"));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при загрузке конфигурации: " + e.getMessage());
        }
    }

    @BeforeEach
    public void resetMocks() {
        // Сбрасываем моки перед каждым тестом
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
        Random rnd = new Random();
        while (token.length() < 32) {
            token.append(rnd.nextInt(10));
        }
        return token.toString();
    }
}