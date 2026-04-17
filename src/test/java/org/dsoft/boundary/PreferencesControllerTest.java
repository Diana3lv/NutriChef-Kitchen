package org.dsoft.boundary;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class PreferencesControllerTest {

    @Test
    void testGetAvailableAllergensReturnsListOfOptions() {
        given()
            .when()
                .get("/api/nutrition/preferences/allergens")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0))
                .body("[0].apiValue", notNullValue())
                .body("[0].label", notNullValue());
    }

    @Test
    void testGetAvailableDietaryPreferencesReturnsListOfOptions() {
        given()
            .when()
                .get("/api/nutrition/preferences/dietary-preferences")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0))
                .body("[0].apiValue", notNullValue())
                .body("[0].label", notNullValue());
    }

    @Test
    void testAllergensContainExpectedValues() {
        given()
            .when()
                .get("/api/nutrition/preferences/allergens")
            .then()
                .statusCode(200)
                .body("apiValue", hasItems("TREE_NUTS", "PEANUTS", "SHELLFISH", "FISH", "DAIRY", "EGGS", "SOY", "WHEAT"));
    }

    @Test
    void testDietaryPreferencesContainExpectedValues() {
        given()
            .when()
                .get("/api/nutrition/preferences/dietary-preferences")
            .then()
                .statusCode(200)
                .body("apiValue", hasItems("VEGETARIAN", "VEGAN", "PESCATARIAN", "KETO", "PALEO", "GLUTEN_FREE"));
    }

    @Test
    void testAllergensResponseHasFormattedLabels() {
        given()
            .when()
                .get("/api/nutrition/preferences/allergens")
            .then()
                .statusCode(200)
                .body("find { it.apiValue == 'TREE_NUTS' }.label", equalTo("Tree Nuts"));
    }

    @Test
    void testDietaryPreferencesResponseHasFormattedLabels() {
        given()
            .when()
                .get("/api/nutrition/preferences/dietary-preferences")
            .then()
                .statusCode(200)
                .body("find { it.apiValue == 'GLUTEN_FREE' }.label", equalTo("Gluten Free"));
    }
}
