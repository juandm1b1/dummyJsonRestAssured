package base;

import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import utils.ConfigReader;

import static io.restassured.RestAssured.enableLoggingOfRequestAndResponseIfValidationFails;


public class BaseTest {

    protected RequestSpecification spec;

    @BeforeClass
    public void setup() {
        OpenApiValidationFilter validationFilter = new OpenApiValidationFilter("src/test/resources/openapi.json");
        enableLoggingOfRequestAndResponseIfValidationFails();
        this.spec = new RequestSpecBuilder()
                .setBaseUri(ConfigReader.get("base.uri"))
                .addHeader("Content-Type", "application/json")
                .addFilter(validationFilter)
                .addFilter(new AllureRestAssured())
                .build();
    }
}
