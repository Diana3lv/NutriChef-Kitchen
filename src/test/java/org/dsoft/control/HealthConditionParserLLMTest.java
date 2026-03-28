package org.dsoft.control;

import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class HealthConditionParserLLMTest {

    @Inject
    HealthConditionParser healthConditionParser;

    // ============ Common Conditions Tests ============
    
    @Test
    public void testDairyIntolerance() {
        String medicalCondition = null;
        String intolerances = "dairy";

        Set<String> result = healthConditionParser.parseHealthConditions(medicalCondition, intolerances);

        System.out.println("\n=== Dairy Intolerance Test ===");
        System.out.println("Input: \"" + intolerances + "\"");
        System.out.println("Parsed Ingredients to Avoid:");
        result.forEach(ingredient -> System.out.println("  - " + ingredient));
        System.out.println("Total: " + result.size() + " ingredients");
        System.out.println("==============================\n");

        assertNotNull(result, "Result should not be null");
        assertTrue(result.size() > 0, "Should identify dairy-related ingredients");
    }

    @Test
    public void testGlutenIntolerance() {
        String medicalCondition = null;
        String intolerances = "gluten";

        Set<String> result = healthConditionParser.parseHealthConditions(medicalCondition, intolerances);

        System.out.println("\n=== Gluten Intolerance Test ===");
        System.out.println("Input: \"" + intolerances + "\"");
        System.out.println("Parsed Ingredients to Avoid:");
        result.forEach(ingredient -> System.out.println("  - " + ingredient));
        System.out.println("Total: " + result.size() + " ingredients");
        System.out.println("===============================\n");

        assertNotNull(result, "Result should not be null");
        assertTrue(result.size() > 0, "Should identify gluten-related ingredients");
    }

    @Test
    public void testDiabetesCondition() {
        String medicalCondition = "diabetes";
        String intolerances = null;

        Set<String> result = healthConditionParser.parseHealthConditions(medicalCondition, intolerances);

        System.out.println("\n=== Diabetes Test ===");
        System.out.println("Input: \"" + medicalCondition + "\"");
        System.out.println("Parsed Ingredients to Avoid:");
        result.forEach(ingredient -> System.out.println("  - " + ingredient));
        System.out.println("Total: " + result.size() + " ingredients");
        System.out.println("=====================\n");

        assertNotNull(result, "Result should not be null");
        assertTrue(result.size() > 0, "Should identify sugary and refined carbs to avoid");
    }

    @Test
    public void testHypertensionCondition() {
        String medicalCondition = "high blood pressure";
        String intolerances = null;

        Set<String> result = healthConditionParser.parseHealthConditions(medicalCondition, intolerances);

        System.out.println("\n=== Hypertension Test ===");
        System.out.println("Input: \"" + medicalCondition + "\"");
        System.out.println("Parsed Ingredients to Avoid:");
        result.forEach(ingredient -> System.out.println("  - " + ingredient));
        System.out.println("Total: " + result.size() + " ingredients");
        System.out.println("==========================\n");

        assertNotNull(result, "Result should not be null");
        assertTrue(result.size() > 0, "Should identify salt and sodium sources");
    }

    @Test
    public void testPeanutAllergy() {
        String medicalCondition = "peanut allergy";
        String intolerances = null;

        Set<String> result = healthConditionParser.parseHealthConditions(medicalCondition, intolerances);

        System.out.println("\n=== Peanut Allergy Test ===");
        System.out.println("Input: \"" + medicalCondition + "\"");
        System.out.println("Parsed Ingredients to Avoid:");
        result.forEach(ingredient -> System.out.println("  - " + ingredient));
        System.out.println("Total: " + result.size() + " ingredients");
        System.out.println("============================\n");

        assertNotNull(result, "Result should not be null");
        assertTrue(result.size() > 0, "Should identify peanut-related ingredients");
    }

    // ============ Complex Cases Tests ============

    @Test
    public void testWolframSyndromeDetection() {
        String medicalCondition = "i have wolfram syndrome";
        String intolerances = null;

        Set<String> result = healthConditionParser.parseHealthConditions(medicalCondition, intolerances);

        System.out.println("\n=== Wolfram Syndrome Parsing Test ===");
        System.out.println("Input: \"" + medicalCondition + "\"");
        System.out.println("Parsed Ingredients to Avoid:");
        result.forEach(ingredient -> System.out.println("  - " + ingredient));
        System.out.println("Total: " + result.size() + " ingredients");
        System.out.println("=====================================\n");

        // LLM test - shows advanced case handling
        assertNotNull(result, "Result should not be null");
    }

    @Test
    public void testWolframWithMultipleConditions() {
        String medicalCondition = "wolfram syndrome and hypothyroidism";
        String intolerances = "gluten";

        Set<String> result = healthConditionParser.parseHealthConditions(medicalCondition, intolerances);

        System.out.println("\n=== Wolfram Syndrome + Hypothyroidism + Gluten Test ===");
        System.out.println("Input: \"" + medicalCondition + "\" + Intolerances: \"" + intolerances + "\"");
        System.out.println("Parsed Ingredients to Avoid:");
        result.forEach(ingredient -> System.out.println("  - " + ingredient));
        System.out.println("Total: " + result.size() + " ingredients");
        System.out.println("========================================================\n");

        assertFalse(result.isEmpty(), "Should identify ingredients for multiple conditions");
    }

    @Test
    public void testMultipleIntolerances() {
        String medicalCondition = null;
        String intolerances = "dairy, gluten, nuts";

        Set<String> result = healthConditionParser.parseHealthConditions(medicalCondition, intolerances);

        System.out.println("\n=== Multiple Intolerances Test ===");
        System.out.println("Input: \"" + intolerances + "\"");
        System.out.println("Parsed Ingredients to Avoid:");
        result.forEach(ingredient -> System.out.println("  - " + ingredient));
        System.out.println("Total: " + result.size() + " ingredients");
        System.out.println("==================================\n");

        assertFalse(result.isEmpty(), "Should identify ingredients for multiple intolerances");
    }
    
    // ============ Edge Cases Tests ============

    @Test
    public void testEmptyInput() {
        Set<String> result = healthConditionParser.parseHealthConditions(null, null);
        System.out.println("\n=== Empty Input Test ===");
        System.out.println("Result size: " + result.size());
        System.out.println("=======================\n");
        assertTrue(result.isEmpty(), "Result should be empty for null inputs");
    }

    @Test
    public void testBlankInput() {
        Set<String> result = healthConditionParser.parseHealthConditions(" ", " ");
        System.out.println("\n=== Blank Input Test ===");
        System.out.println("Result size: " + result.size());
        System.out.println("========================\n");
        assertTrue(result.isEmpty(), "Result should be empty for blank inputs");
    }
}
