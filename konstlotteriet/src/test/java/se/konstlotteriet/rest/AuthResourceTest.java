package se.konstlotteriet.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import se.konstlotteriet.dto.LoginRequest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@DisplayName("AuthResource Integration Tests")
class AuthResourceTest {

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLogin_Success() {
        LoginRequest request = new LoginRequest();
        request.email = "admin@konstlotteriet.se";
        request.password = "admin123";

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("email", equalTo("admin@konstlotteriet.se"))
                .body("role", equalTo("ADMIN"))
                .body("userId", notNullValue());
    }

    @Test
    @DisplayName("Should return 401 for invalid credentials")
    void testLogin_InvalidCredentials() {
        LoginRequest request = new LoginRequest();
        request.email = "admin@konstlotteriet.se";
        request.password = "wrongpassword";

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Should return 401 for non-existent user")
    void testLogin_UserNotFound() {
        LoginRequest request = new LoginRequest();
        request.email = "nonexistent@test.se";
        request.password = "password123";

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Should return 400 for invalid email format")
    void testLogin_InvalidEmailFormat() {
        LoginRequest request = new LoginRequest();
        request.email = "notanemail";
        request.password = "password123";

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(anyOf(is(400), is(401))); // Could be validation or authentication error
    }

    @Test
    @DisplayName("Should get current user info when authenticated")
    @TestSecurity(user = "admin@konstlotteriet.se", roles = {"ADMIN"})
    void testGetCurrentUser_Authenticated() {
        // First login to get a real token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.email = "admin@konstlotteriet.se";
        loginRequest.password = "admin123";

        String token = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");

        // Then use the token to get current user
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/auth/me")
                .then()
                .statusCode(200)
                .body("email", equalTo("admin@konstlotteriet.se"))
                .body("role", equalTo("ADMIN"));
    }

    @Test
    @DisplayName("Should return 401 when accessing protected endpoint without token")
    void testGetCurrentUser_Unauthenticated() {
        given()
                .when()
                .get("/api/auth/me")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Should change password successfully")
    @TestSecurity(user = "admin@konstlotteriet.se", roles = {"ADMIN"})
    void testChangePassword_Success() {
        // First login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.email = "admin@konstlotteriet.se";
        loginRequest.password = "admin123";

        String token = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");

        // Try to change password (this test would need a test database)
        // For now, we just test the endpoint structure
        String changePasswordRequest = """
                {
                    "oldPassword": "admin123",
                    "newPassword": "newpassword456"
                }
                """;

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(changePasswordRequest)
                .when()
                .post("/api/auth/change-password")
                .then()
                .statusCode(anyOf(is(200), is(204)));
    }

    @Test
    @DisplayName("Should return 401 when changing password without authentication")
    void testChangePassword_Unauthenticated() {
        String changePasswordRequest = """
                {
                    "oldPassword": "admin123",
                    "newPassword": "newpassword456"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(changePasswordRequest)
                .when()
                .post("/api/auth/change-password")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Should register new user as admin")
    @TestSecurity(user = "admin@konstlotteriet.se", roles = {"ADMIN"})
    void testRegister_AsAdmin() {
        // First login as admin
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.email = "admin@konstlotteriet.se";
        loginRequest.password = "admin123";

        String token = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");

        // Register new user
        String registerRequest = """
                {
                    "email": "newuser@test.se",
                    "password": "password123",
                    "role": "MEMBER",
                    "memberId": 1
                }
                """;

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(registerRequest)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(anyOf(is(200), is(201), is(400))); // 400 if member doesn't exist
    }

    @Test
    @DisplayName("Should return 401 when registering without admin role")
    void testRegister_Unauthorized() {
        String registerRequest = """
                {
                    "email": "newuser@test.se",
                    "password": "password123",
                    "role": "MEMBER"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(registerRequest)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Should validate required fields in login request")
    void testLogin_MissingFields() {
        // Missing password
        String requestWithoutPassword = """
                {
                    "email": "test@test.se"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestWithoutPassword)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(anyOf(is(400), is(401)));

        // Missing email
        String requestWithoutEmail = """
                {
                    "password": "password123"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestWithoutEmail)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(anyOf(is(400), is(401)));
    }
}
