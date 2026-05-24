package org.dsoft.control;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.dsoft.control.parser.HealthConditionParser;

@DisplayName("HealthConditionParser Tests")
class HealthConditionParserTest {

    private HealthConditionParser parser;

    @BeforeEach
    void setUp() {
        parser = new HealthConditionParser();
    }

    @Test
    @DisplayName("Should parse lactose intolerance")
    void testParseLactoseIntolerance() {
        Set<String> result = parser.parseHealthConditions("lactose intolerant", null);
        
        assertTrue(result.contains("milk"), "Should exclude milk");
        assertTrue(result.contains("cheese"), "Should exclude cheese");
        assertTrue(result.contains("butter"), "Should exclude butter");
        assertTrue(result.contains("cream"), "Should exclude cream");
    }

    @Test
    @DisplayName("Should parse dairy allergy")
    void testParseDairyAllergy() {
        Set<String> result = parser.parseHealthConditions("dairy allergy", null);
        
        assertTrue(result.contains("milk"), "Should exclude milk");
        assertTrue(result.contains("whey"), "Should exclude whey");
    }

    @Test
    @DisplayName("Should parse celiac disease")
    void testParseCeliac() {
        Set<String> result = parser.parseHealthConditions("celiac disease", null);
        
        assertTrue(result.contains("wheat"), "Should exclude wheat");
        assertTrue(result.contains("gluten"), "Should exclude gluten");
        assertTrue(result.contains("barley"), "Should exclude barley");
    }

    @Test
    @DisplayName("Should parse shellfish allergy")
    void testParseShellfish() {
        Set<String> result = parser.parseHealthConditions(null, "shellfish");
        
        assertTrue(result.contains("shrimp"), "Should exclude shrimp");
        assertTrue(result.contains("crab"), "Should exclude crab");
        assertTrue(result.contains("lobster"), "Should exclude lobster");
    }

    @Test
    @DisplayName("Should parse peanut allergy")
    void testParsePeanutAllergy() {
        Set<String> result = parser.parseHealthConditions("peanut allergy", null);
        
        assertTrue(result.contains("peanuts"), "Should exclude peanuts");
        assertTrue(result.contains("peanut oil"), "Should exclude peanut oil");
    }

    @Test
    @DisplayName("Should parse tree nut allergies")
    void testParseTreeNuts() {
        Set<String> result = parser.parseHealthConditions("tree nut allergy", null);
        
        assertTrue(result.contains("nuts"), "Should exclude nuts");
        assertTrue(result.contains("almond"), "Should exclude almond");
        assertTrue(result.contains("walnut"), "Should exclude walnut");
    }

    @Test
    @DisplayName("Should parse egg intolerance")
    void testParseEggIntolerance() {
        Set<String> result = parser.parseHealthConditions("egg intolerance", null);
        
        assertTrue(result.contains("eggs"), "Should exclude eggs");
        assertTrue(result.contains("mayonnaise"), "Should exclude mayonnaise");
    }

    @Test
    @DisplayName("Should parse soy allergy")
    void testParseSoyAllergy() {
        Set<String> result = parser.parseHealthConditions(null, "soy");
        
        assertTrue(result.contains("soy"), "Should exclude soy");
        assertTrue(result.contains("tofu"), "Should exclude tofu");
    }

    @Test
    @DisplayName("Should parse diabetes condition")
    void testParseDiabetes() {
        Set<String> result = parser.parseHealthConditions("diabetic", null);
        
        assertTrue(result.contains("sugar"), "Should exclude sugar");
        assertTrue(result.contains("honey"), "Should exclude honey");
    }

    @Test
    @DisplayName("Should parse hypertension condition")
    void testParseHypertension() {
        Set<String> result = parser.parseHealthConditions("high blood pressure", null);
        
        assertTrue(result.contains("salt"), "Should exclude salt");
        assertTrue(result.contains("sodium"), "Should exclude sodium");
    }

    @Test
    @DisplayName("Should handle multiple conditions")
    void testParseMultipleConditions() {
        Set<String> result = parser.parseHealthConditions(
            "lactose intolerant, diabetic",
            "shellfish, peanuts"
        );
        
        // Dairy
        assertTrue(result.contains("milk"), "Should exclude milk");
        // Sugar
        assertTrue(result.contains("sugar"), "Should exclude sugar");
        // Shellfish
        assertTrue(result.contains("shrimp"), "Should exclude shrimp");
        // Peanuts
        assertTrue(result.contains("peanuts"), "Should exclude peanuts");
    }

    @Test
    @DisplayName("Should handle case-insensitive input")
    void testCaseInsensitive() {
        Set<String> result1 = parser.parseHealthConditions("LACTOSE INTOLERANT", null);
        Set<String> result2 = parser.parseHealthConditions("lactose intolerant", null);
        
        assertEquals(result1, result2, "Should parse regardless of case");
        assertTrue(result1.contains("milk"), "Should correctly identify despite capitalization");
    }

    @Test
    @DisplayName("Should handle null inputs gracefully")
    void testNullInputs() {
        Set<String> result = parser.parseHealthConditions(null, null);
        
        assertNotNull(result, "Should return non-null set");
        assertTrue(result.isEmpty(), "Should return empty set for null inputs");
    }

    @Test
    @DisplayName("Should handle empty string inputs")
    void testEmptyStringInputs() {
        Set<String> result = parser.parseHealthConditions("", "");
        
        assertNotNull(result, "Should return non-null set");
        assertTrue(result.isEmpty(), "Should return empty set for empty inputs");
    }

    @Test
    @DisplayName("Should not include unrelated ingredients")
    void testNoFalsePositives() {
        Set<String> result = parser.parseHealthConditions("I like peas", null);
        
        assertFalse(result.contains("peas"), "Should not exclude unrelated ingredients");
        assertFalse(result.contains("peanuts"), "Should not trigger on similar words");
    }
}
