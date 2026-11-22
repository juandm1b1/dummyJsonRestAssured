package base;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import utils.ConfigReader;


public class BaseTest {

    protected RequestSpecification spec;

    @BeforeClass
    public void setup() {
        this.spec = new RequestSpecBuilder()
                .setBaseUri(ConfigReader.get("base.uri"))
                .addHeader("Content-Type", "application/json")
                .build();
    }
}
