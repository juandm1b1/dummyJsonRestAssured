package base;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.report.LevelResolver;
import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import utils.ConfigReader;

import static io.restassured.RestAssured.enableLoggingOfRequestAndResponseIfValidationFails;


public class BaseTest {

    protected RequestSpecification spec;
    protected OpenApiInteractionValidator validator;

    @BeforeClass
    public void setup() {
        enableLoggingOfRequestAndResponseIfValidationFails();

        // 1. Configure the resolver to ignore `additionalProperties` (because there are errors in the dummyJSON OpenAPI contract)
        LevelResolver levelResolver = LevelResolver.create()
                .withLevel("validation.response.body.schema.additionalProperties", ValidationReport.Level.WARN)
                // 2. For security testing (malformed tokens), we allow the request to pass through,
                // but we log it as a WARN so that there is a record in the logs.
                .withLevel("validation.request.security.invalid", ValidationReport.Level.WARN)
                .withLevel("validation.request.security.missing", ValidationReport.Level.WARN)
                .build();

        // 2. Build the validator using the resolver and the contract path
        this.validator = OpenApiInteractionValidator
                .createForSpecificationUrl("openapi.json") // It can also be the contract's URL: "https://dummyjson.com/openapi.json"
                .withLevelResolver(levelResolver)
                .build();

        // 3. Create filter
        OpenApiValidationFilter validationFilter = new OpenApiValidationFilter(validator);

        // 4. spec WITH validation (but ignoring additionalProperties)
        this.spec = new RequestSpecBuilder()
                .setBaseUri(ConfigReader.get("base.uri"))
                .addHeader("Content-Type", "application/json")
                .addFilter(validationFilter)
                .addFilter(new AllureRestAssured())
                .build();
    }
}