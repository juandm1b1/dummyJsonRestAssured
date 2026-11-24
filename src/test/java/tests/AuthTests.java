package tests;

import base.BaseTest;
import io.qameta.allure.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.ConfigReader;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

@Epic("Identity & Access Management (IAM)")
@Feature("JWT Authentication & Session Lifecycle")
public class AuthTests extends BaseTest {

    private final String validLoginBody = String.format(
            "{\"username\":\"%s\",\"password\":\"%s\"}",
            ConfigReader.get("username"),
            ConfigReader.get("password"));
    private Response loginResponse;
    private String accessToken;
    private String refreshToken;

    @BeforeClass(description = "Authenticate base user and extract initial tokens for the test suite")
    public void initAuthData() {
        this.loginResponse = given()
                .spec(spec)
                .body(validLoginBody)
                .when()
                .post("/auth/login");

        this.accessToken = loginResponse.path("accessToken");
        this.refreshToken = loginResponse.path("refreshToken");
    }

    // =========================================================================
    // 1. POSITIVE TEST CASES (HAPPY PATH & STATE TRANSITIONS)
    // =========================================================================

    @Test(description = "Valid generation of JWT token pair upon successful login")
    @Story("Token Generation and Format Validation")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Technique: Equivalence Partitioning (Valid Class). Verifies that POST /auth/login returns HTTP 200 and provides non-null accessToken and refreshToken matching a valid JWT structure (Header.Payload.Signature).")
    public void verifyPostValidTokensGenerationReturns200WhenValidLoginCredentials() {
        String jwtRegex = "^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$";
        loginResponse.then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("accessToken", matchesPattern(jwtRegex))
                .body("refreshToken", notNullValue())
                .body("refreshToken", matchesPattern(jwtRegex));
    }

    @Test(description = "Authorized access to protected resource using Bearer accessToken")
    @Story("Protected Resource Authorization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Technique: State Transition Testing (Authenticated -> Resource Access). Validates that GET /auth/me returns HTTP 200 along with the expected user profile matching the provided token.")
    public void verifyGetAuthorizedAccessReturns200WhenValidLoginCredentials() {
        given()
                .spec(spec)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/auth/me")
                .then()
                .statusCode(200)
                .body("firstName", equalTo("Sophia"))
                .body("email", equalTo("sophia.brown@x.dummyjson.com"));
    }

    @Test(description = "Session renewal issuing a new accessToken distinct from the original")
    @Story("Session Lifecycle and Token Refresh")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Technique: State Transition Testing (Active -> Renewed). Verifies that POST /auth/refresh accepts a valid refreshToken and generates a brand-new accessToken different from the initial one.")
    public void verifyPostSessionRenovationReturns200WhenValidRefreshToken() {
        String newAccessToken = getRefreshedAccessToken();
        assertNotNull(newAccessToken, "The new accessToken must not be null");
        assertNotEquals(newAccessToken, accessToken, "The new accessToken must differ from the original token");
    }

    @Test(description = "Access continuity to protected resources using the refreshed accessToken")
    @Story("Session Lifecycle and Token Refresh")
    @Severity(SeverityLevel.NORMAL)
    @Description("Technique: State Transition Testing. Confirms that the newly generated accessToken obtained from the refresh flow is fully functional for querying GET /auth/me with HTTP 200.")
    public void verifyGetMeReturns200WhenUsingRefreshedAccessToken() {
        String newAccessToken = getRefreshedAccessToken();

        given()
                .spec(spec)
                .header("Authorization", "Bearer " + newAccessToken)
                .when()
                .get("/auth/me")
                .then()
                .statusCode(200)
                .body("firstName", equalTo("Sophia"))
                .body("email", equalTo("sophia.brown@x.dummyjson.com"));
    }

    // =========================================================================
    // 2. NEGATIVE & SECURITY TEST CASES (OWASP API2: BROKEN AUTHENTICATION)
    // =========================================================================

    @Test(description = "Reject HTTP 401 when accessing protected resource without Authorization header")
    @Story("Credential Presence and Format Validation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Technique: Equivalence Partitioning (Invalid Class) / OWASP API2. Validates that GET /auth/me rejects anonymous requests without leaking sensitive user data.")
    public void verifyGetMeReturns401WhenRequestWithoutAuthorizationHeader() {
        given()
                .spec(spec)
                .when()
                .get("/auth/me")
                .then()
                .statusCode(401)
                .contentType(ContentType.JSON)
                .body("message", equalTo("Access Token is required"))
                .body("email", nullValue())
                .body("password", nullValue());
    }

    @Test(description = "KNOWN BUG: Expected HTTP 401 rejection when JWT signature has been tampered with")
    @Story("JWT Signature Integrity")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-DUMMYJSON-01")
    @Description("Technique: Security Testing / Token Tampering (OWASP API2). Altering the last character of the signature should return HTTP 401, but DummyJSON currently throws HTTP 500 due to an unhandled JsonWebTokenError ('invalid signature').")
    public void verifyGetMeReturns401WhenTokenIsTampered() {
        String tamperedToken = accessToken.substring(0, accessToken.length() - 1) + "X";

        given()
                .spec(spec)
                .header("Authorization", "Bearer " + tamperedToken)
                .when()
                .get("/auth/me")
                .then()
                .statusCode(401)
                .body("message", equalTo("invalid signature"));
    }

    @Test(description = "KNOWN BUG: Expected HTTP 401 rejection when omitting the 'Bearer ' prefix in header")
    @Story("Credential Presence and Format Validation")
    @Severity(SeverityLevel.NORMAL)
    @Issue("BUG-DUMMYJSON-02")
    @Description("Technique: Equivalence Partitioning (Invalid Format) / RFC 6750. The server should reject raw tokens missing the 'Bearer ' authentication scheme, but DummyJSON accepts them as valid (violating OAuth2/Bearer standards).")
    public void verifyGetMeReturns401WhenBearerWordIsMissing() {
        given()
                .spec(spec)
                .header("Authorization", accessToken)
                .when()
                .get("/auth/me")
                .then()
                .statusCode(401);
    }

    @Test(description = "KNOWN BUG: Expected HTTP 401 rejection due to token confusion (using refreshToken as accessToken)")
    @Story("Token Purpose Isolation")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-DUMMYJSON-03")
    @Description("Technique: Security Testing / Token Confusion (OWASP API2). Validates that a refreshToken cannot be used to consume resource endpoints (/auth/me). DummyJSON fails by accepting both tokens interchangeably.")
    public void verifyGetMeReturns401WhenUsingRefreshTokenInsteadOfAccessToken() {
        given()
                .spec(spec)
                .header("Authorization", "Bearer " + refreshToken)
                .when()
                .get("/auth/me")
                .then()
                .statusCode(401);
    }

    @Test(description = "Reject HTTP 401/403 when attempting session renewal with a forged refreshToken")
    @Story("Session Lifecycle and Token Refresh")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Technique: Equivalence Partitioning (Invalid Class) / OWASP API2. Verifies that POST /auth/refresh rejects tokens with invalid signatures without issuing a new accessToken.")
    public void verifyPostRefreshReturns403WhenUsingFakeRefreshToken() {
        String fakeRefreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyIjoiZmFrZSJ9.invalidsignature";
        String refreshBody = String.format("{\"refreshToken\":\"%s\", \"expiresInMins\": 30}", fakeRefreshToken);

        given()
                .spec(spec)
                .body(refreshBody)
                .when()
                .post("/auth/refresh")
                .then()
                .statusCode(anyOf(is(401), is(403)))
                .body("message", equalTo("Invalid refresh token"))
                .body("accessToken", nullValue());
    }

    // =========================================================================
    // HELPER METHODS DOCUMENTED AS REPORT STEPS
    // =========================================================================

    @Step("Request accessToken renewal via POST /auth/refresh")
    private String getRefreshedAccessToken() {
        String refreshBody = String.format("{\"refreshToken\":\"%s\", \"expiresInMins\": 30}", refreshToken);

        return given()
                .spec(spec)
                .body(refreshBody)
                .when()
                .post("/auth/refresh")
                .then()
                .statusCode(200)
                .extract().path("accessToken");
    }
}