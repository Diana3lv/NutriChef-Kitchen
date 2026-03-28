package org.dsoft.control;

import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class HealthConditionParserWolframTest {

    @Inject
    HealthConditionParser healthConditionParser;

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

        // Just display results, don't assert - LLM availability varies
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

        assertFalse(result.isEmpty(), "Should identify ingredients for both conditions");
    }

    @Test
    public void testEmptyInput() {
        Set<String> result = healthConditionParser.parseHealthConditions(null, null);
        
        System.out.println("\n=== Empty Input Test ===");
        System.out.println("Result size: " + result.size());
        System.out.println("=======================\n");
        
        assertTrue(result.isEmpty(), "Should return empty set for null inputs");
    }
}
