package tests;

import base.BaseTest;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.ConfigReader;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;


public class AuthTests extends BaseTest {

    private final String validLoginBody = String.format(
            "{\"username\":\"%s\",\"password\":\"%s\"}",
            ConfigReader.get("username"),
            ConfigReader.get("password"));
    private final String loginPath = "/auth/login";
    private final String jwtRegex = "^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$";
    private Response loginResponse;
    private String accessToken;
    private String refreshToken;


    @BeforeClass
    public void initAuthData() {
        this.loginResponse = given()
                .log().all()
                .spec(spec)
                .body(validLoginBody)
        .when()
                .post(loginPath);
        this.accessToken = loginResponse.path("accessToken");
        this.refreshToken = loginResponse.path("refreshToken");
    }

    //    1. Casos Positivos (Flujo normal)
    //    • Generación válida: Hacer POST /auth/login con credenciales correctas y verificar 200 OK,
    //    comprobando que accessToken y refreshToken no sean nulos y tengan el formato JWT (tres segmentos separados por puntos).
    @Test
    public void verifyPostValidTokensGenerationReturns200WhenValidLoginCredentials() {
        loginResponse.then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("accessToken", matchesPattern(jwtRegex))
                .body("refreshToken", notNullValue())
                .body("refreshToken", matchesPattern(jwtRegex));
    }

    //    • Acceso autorizado: Consultar un recurso protegido (GET /auth/me) enviando el accessToken en el encabezado Authorization:
    //    Bearer <token> y validar 200 OK junto con los datos del usuario.
    @Test
    public void verifyGetAuthorizedAccessReturns200WhenValidLoginCredentials() {
        String token = loginResponse.path("accessToken");
        given()
                .spec(spec)
                .header("Authorization", "Bearer " + token)
        .when()
                .get("/auth/me")
        .then()
                .statusCode(200)
                .body("firstName", equalTo("Sophia"))
                .body("email", equalTo("sophia.brown@x.dummyjson.com"));
    }

    // Private helper method to reuse the refresh logic without coupling the tests
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


    //    • Renovación de sesión: Enviar el refreshToken a POST /auth/refresh y verificar que responda 200 OK
    //    entregando un nuevo accessToken distinto al original.
    @Test
    public void verifyPostSessionRenovationReturns200WhenValidRefreshToken() {
        String newAccessToken = getRefreshedAccessToken();
        assertNotNull(newAccessToken);
        assertNotEquals(newAccessToken, accessToken);
    }

    //    • Continuidad con el nuevo token: Realizar una petición a GET /auth/me
    //    utilizando el accessToken recién renovado y confirmar que sigue dando 200 OK.
    @Test
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


//  ---------------------------------------------------------------------------------------------------------------
//    2. Casos Negativos y de Seguridad (Diferenciadores en entrevistas)
//
//    • Petición sin token: Llamar a GET /auth/me sin el encabezado Authorization y validar que la API rechace con 401 Unauthorized.
//    • Token alterado o malformado: Modificar un solo carácter del accessToken (rompiendo la firma) y verificar que el servidor devuelva 401 Unauthorized o 403 Forbidden.
//    • Prefijo incorrecto: Enviar el token válido pero omitiendo la palabra Bearer (ej. Authorization: <token>) para confirmar que la API exige el estándar correcto.
//    • Confusión de tokens (Seguridad): Intentar acceder a GET /auth/me usando el refreshToken en lugar del accessToken (debe fallar con 401).
//    • Refresh con token inválido: Enviar un refreshToken inventado o caducado a POST /auth/refresh y validar que devuelva 401 Unauthorized o 403 Forbidden.


}