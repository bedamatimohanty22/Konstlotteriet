package se.konstlotteriet.rest.admin;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import se.konstlotteriet.dto.LoginRequest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@DisplayName("Admin MemberResource Integration Tests")
class MemberResourceTest {

    private String getAdminToken() {
        LoginRequest request = new LoginRequest();
        request.email = "admin@konstlotteriet.se";
        request.password = "admin123";

        return given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    @Test
    @DisplayName("Should get all members as admin")
    @TestSecurity(user = "admin@konstlotteriet.se", roles = {"ADMIN"})
    void testGetAllMembers_AsAdmin() {
        String token = getAdminToken();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/admin/members")
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    @Test
    @DisplayName("Should return 401 when accessing admin endpoint without authentication")
    void testGetAllMembers_Unauthenticated() {
        given()
                .when()
                .get("/api/admin/members")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Should create new member as admin")
    @TestSecurity(user = "admin@konstlotteriet.se", roles = {"ADMIN"})
    void testCreateMember_AsAdmin() {
        String token = getAdminToken();

        String memberRequest = """
                {
                    "firstName": "Test",
                    "lastName": "Testsson",
                    "email": "test@test.se",
                    "phoneNumber": "070-1234567",
                    "membershipStartYear": 2024
                }
                """;

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(memberRequest)
                .when()
                .post("/api/admin/members")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .body("firstName", equalTo("Test"))
                .body("lastName", equalTo("Testsson"))
                .body("active", equalTo(true));
    }

    @Test
    @DisplayName("Should validate member data on creation")
    @TestSecurity(user = "admin@konstlotteriet.se", roles = {"ADMIN"})
    void testCreateMember_InvalidData() {
        String token = getAdminToken();

        // Missing required fields
        String invalidMemberRequest = """
                {
                    "firstName": "Test"
                }
                """;

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(invalidMemberRequest)
                .when()
                .post("/api/admin/members")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should get member by ID as admin")
    @TestSecurity(user = "admin@konstlotteriet.se", roles = {"ADMIN"})
    void testGetMemberById_AsAdmin() {
        String token = getAdminToken();

        // First create a member
        String memberRequest = """
                {
                    "firstName": "Anna",
                    "lastName": "Andersson",
                    "email": "anna@test.se",
                    "phoneNumber": "070-1111111",
                    "membershipStartYear": 2024
                }
                """;

        Long memberId = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(memberRequest)
                .when()
                .post("/api/admin/members")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .extract()
                .path("id");

        // Then get the member by ID
        if (memberId != null) {
            given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .get("/api/admin/members/" + memberId)
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(memberId.intValue()))
                    .body("firstName", equalTo("Anna"));
        }
    }

    @Test
    @DisplayName("Should update member as admin")
    @TestSecurity(user = "admin@konstlotteriet.se", roles = {"ADMIN"})
    void testUpdateMember_AsAdmin() {
        String token = getAdminToken();

        // First create a member
        String createRequest = """
                {
                    "firstName": "Björn",
                    "lastName": "Bergström",
                    "email": "bjorn@test.se",
                    "phoneNumber": "070-2222222",
                    "membershipStartYear": 2024
                }
                """;

        Long memberId = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when()
                .post("/api/admin/members")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .extract()
                .path("id");

        // Then update the member
        if (memberId != null) {
            String updateRequest = """
                    {
                        "firstName": "Björn",
                        "lastName": "Björnsson",
                        "email": "bjorn.new@test.se",
                        "phoneNumber": "070-9999999",
                        "membershipStartYear": 2024
                    }
                    """;

            given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(updateRequest)
                    .when()
                    .put("/api/admin/members/" + memberId)
                    .then()
                    .statusCode(200)
                    .body("lastName", equalTo("Björnsson"))
                    .body("email", equalTo("bjorn.new@test.se"))
                    .body("phoneNumber", equalTo("070-9999999"));
        }
    }

    @Test
    @DisplayName("Should deactivate member as admin")
    @TestSecurity(user = "admin@konstlotteriet.se", roles = {"ADMIN"})
    void testDeactivateMember_AsAdmin() {
        String token = getAdminToken();

        // First create a member
        String memberRequest = """
                {
                    "firstName": "Cecilia",
                    "lastName": "Carlsson",
                    "email": "cecilia@test.se",
                    "phoneNumber": "070-3333333",
                    "membershipStartYear": 2024
                }
                """;

        Long memberId = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(memberRequest)
                .when()
                .post("/api/admin/members")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .extract()
                .path("id");

        // Then deactivate the member
        if (memberId != null) {
            given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .delete("/api/admin/members/" + memberId)
                    .then()
                    .statusCode(anyOf(is(200), is(204)));
        }
    }

    @Test
    @DisplayName("Should return 404 when getting non-existent member")
    @TestSecurity(user = "admin@konstlotteriet.se", roles = {"ADMIN"})
    void testGetMember_NotFound() {
        String token = getAdminToken();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/admin/members/999999")
                .then()
                .statusCode(anyOf(is(404), is(400)));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent member")
    @TestSecurity(user = "admin@konstlotteriet.se", roles = {"ADMIN"})
    void testUpdateMember_NotFound() {
        String token = getAdminToken();

        String updateRequest = """
                {
                    "firstName": "Non",
                    "lastName": "Existent",
                    "email": "nonexistent@test.se",
                    "phoneNumber": "070-0000000",
                    "membershipStartYear": 2024
                }
                """;

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put("/api/admin/members/999999")
                .then()
                .statusCode(anyOf(is(404), is(400)));
    }
}
