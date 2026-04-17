package org.dsoft.boundary;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class NutritionProfileControllerTest {

    @Test
    void testGetProfileSettingsEndpointExists() {
        // Endpoint requires authentication, should return 401 without token
        given()
            .when()
                .get("/api/users/nutrition-profile-settings")
            .then()
                .statusCode(401);
    }

    @Test
    void testGetProfileSettingsWithoutAuthenticationReturnsForbidden() {
        given()
            .when()
                .get("/api/users/nutrition-profile-settings")
            .then()
                .statusCode(401); // Unauthorized without token
    }

    @Test
    void testUpdateProfileSettingsEndpointExists() {
        // Endpoint requires authentication, should return 401 without token
        given()
            .contentType(ContentType.JSON)
            .body("{\"allergens\": [\"PEANUTS\"], \"dietaryPreferences\": [\"VEGETARIAN\"], \"medicalConditions\": \"diabetes\"}")
            .when()
                .put("/api/users/nutrition-profile-settings")
            .then()
                .statusCode(401);
    }

    @Test
    void testUpdateProfileSettingsWithoutAuthenticationReturnsForbidden() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"allergens\": []}")
            .when()
                .put("/api/users/nutrition-profile-settings")
            .then()
                .statusCode(401); // Unauthorized without token
    }
}
