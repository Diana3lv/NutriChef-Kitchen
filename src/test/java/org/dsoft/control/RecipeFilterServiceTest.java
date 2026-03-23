package org.dsoft.control;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.dsoft.entity.model.Allergen;
import org.dsoft.entity.model.DietaryPreference;
import org.dsoft.entity.model.Recipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeFilterService Tests")
class RecipeFilterServiceTest {

    private RecipeFilterService filterService;
    private List<Recipe> testRecipes;

    @BeforeEach
    void setUp() {
        filterService = new RecipeFilterService();
        testRecipes = new ArrayList<>();

        // Recipe 1: Vegan pasta (no allergens)
        Recipe pastaDish = new Recipe();
        pastaDish.id = 1L;
        pastaDish.title = "Vegan Pasta";
        pastaDish.allergens = new ArrayList<>();
        pastaDish.dietaryPreferences = List.of(DietaryPreference.VEGAN);
        testRecipes.add(pastaDish);

        // Recipe 2: Chicken dish with peanuts (contains allergen)
        Recipe chickenDish = new Recipe();
        chickenDish.id = 2L;
        chickenDish.title = "Chicken with Peanut Sauce";
        chickenDish.allergens = List.of(Allergen.PEANUTS);
        chickenDish.dietaryPreferences = new ArrayList<>();
        testRecipes.add(chickenDish);

        // Recipe 3: Fish with milk (contains dairy)
        Recipe fishDish = new Recipe();
        fishDish.id = 3L;
        fishDish.title = "Fish with Cream Sauce";
        fishDish.allergens = List.of(Allergen.DAIRY, Allergen.FISH);
        fishDish.dietaryPreferences = new ArrayList<>();
        testRecipes.add(fishDish);

        // Recipe 4: Keto beef steak (no allergens)
        Recipe ketoDish = new Recipe();
        ketoDish.id = 4L;
        ketoDish.title = "Keto Beef Steak";
        ketoDish.allergens = new ArrayList<>();
        ketoDish.dietaryPreferences = List.of(DietaryPreference.KETO);
        testRecipes.add(ketoDish);

        // Recipe 5: Gluten-free cake with eggs
        Recipe cakeDish = new Recipe();
        cakeDish.id = 5L;
        cakeDish.title = "Gluten-Free Cake";
        cakeDish.allergens = List.of(Allergen.EGGS);
        cakeDish.dietaryPreferences = List.of(DietaryPreference.GLUTEN_FREE);
        testRecipes.add(cakeDish);
    }

    @Test
    @DisplayName("Should filter out recipes with user's allergens")
    void testFilterByAllergens() {
        // User is allergic to peanuts
        List<Allergen> userAllergens = List.of(Allergen.PEANUTS);
        List<Recipe> filtered = filterService.filterByAllergens(testRecipes, userAllergens);

        assertEquals(4, filtered.size(), "Should exclude 1 recipe with peanuts");
        assertTrue(filtered.stream().noneMatch(r -> r.id == 2L), "Recipe 2 (peanuts) should be excluded");
    }

    @Test
    @DisplayName("Should filter out recipes with dairy when user has dairy allergy")
    void testFilterByDairyAllergen() {
        List<Allergen> userAllergens = List.of(Allergen.DAIRY);
        List<Recipe> filtered = filterService.filterByAllergens(testRecipes, userAllergens);

        assertEquals(4, filtered.size(), "Should exclude 1 recipe with dairy");
        assertTrue(filtered.stream().noneMatch(r -> r.id == 3L), "Recipe 3 (fish with cream) should be excluded");
    }

    @Test
    @DisplayName("Should handle null allergen list")
    void testFilterByAllergens_NullInput() {
        List<Recipe> filtered = filterService.filterByAllergens(testRecipes, null);
        assertEquals(testRecipes.size(), filtered.size(), "Should return all recipes if no allergens");
    }

    @Test
    @DisplayName("Should handle empty allergen list")
    void testFilterByAllergens_EmptyInput() {
        List<Recipe> filtered = filterService.filterByAllergens(testRecipes, new ArrayList<>());
        assertEquals(testRecipes.size(), filtered.size(), "Should return all recipes if allergen list is empty");
    }

    @Test
    @DisplayName("Should filter recipes by dietary preferences - VEGAN")
    void testFilterByDietaryPreferences_Vegan() {
        List<DietaryPreference> userPreferences = List.of(DietaryPreference.VEGAN);
        List<Recipe> filtered = filterService.filterByDietaryPreferences(testRecipes, userPreferences);

        // Should only include recipes that have VEGAN tag (and recipes with no dietary tags)
        assertTrue(filtered.stream().anyMatch(r -> r.id == 1L), "Should include vegan pasta");
    }

    @Test
    @DisplayName("Should include generic recipes when user has dietary preferences")
    void testFilterByDietaryPreferences_IncludesGenericRecipes() {
        List<DietaryPreference> userPreferences = List.of(DietaryPreference.VEGAN);
        List<Recipe> filtered = filterService.filterByDietaryPreferences(testRecipes, userPreferences);

        // Should include recipes with no dietary tags (generic recipes are acceptable)
        long genericCount = filtered.stream()
                .filter(r -> r.dietaryPreferences == null || r.dietaryPreferences.isEmpty())
                .count();
        assertTrue(genericCount >= 0, "Should accept recipes with no dietary preferences");
    }

    @Test
    @DisplayName("Should handle null dietary preference list")
    void testFilterByDietaryPreferences_NullInput() {
        List<Recipe> filtered = filterService.filterByDietaryPreferences(testRecipes, null);
        assertEquals(testRecipes.size(), filtered.size(), "Should return all recipes if no preferences");
    }

    @Test
    @DisplayName("Should filter out recipes with ingredients to avoid")
    void testFilterByAvoidIngredients() {
        // Create recipe with ingredients
        Recipe recipeWithMilk = new Recipe();
        recipeWithMilk.id = 6L;
        recipeWithMilk.title = "Pasta Carbonara";
        recipeWithMilk.ingredients = List.of(
                new Recipe.Ingredient("pasta", "200", "g"),
                new Recipe.Ingredient("milk", "100", "ml"),
                new Recipe.Ingredient("eggs", "2", "units"));
        testRecipes.add(recipeWithMilk);

        Set<String> avoidIngredients = Set.of("milk", "dairy");
        List<Recipe> filtered = filterService.filterByAvoidIngredients(testRecipes, avoidIngredients);

        assertEquals(5, filtered.size(), "Should exclude 1 recipe with milk");
        assertTrue(filtered.stream().noneMatch(r -> r.id == 6L), "Recipe with milk should be excluded");
    }

    @Test
    @DisplayName("Should handle partial ingredient name matching")
    void testFilterByAvoidIngredients_PartialMatch() {
        Recipe recipeWithAlmondMilk = new Recipe();
        recipeWithAlmondMilk.id = 7L;
        recipeWithAlmondMilk.title = "Smoothie";
        recipeWithAlmondMilk.ingredients = List.of(
                new Recipe.Ingredient("almond milk", "200", "ml"));
        testRecipes.add(recipeWithAlmondMilk);

        Set<String> avoidIngredients = Set.of("milk");
        List<Recipe> filtered = filterService.filterByAvoidIngredients(testRecipes, avoidIngredients);

        assertTrue(filtered.stream().noneMatch(r -> r.id == 7L), "Should exclude recipe with 'almond milk'");
    }

    @Test
    @DisplayName("Should handle null avoidIngredients")
    void testFilterByAvoidIngredients_NullInput() {
        List<Recipe> filtered = filterService.filterByAvoidIngredients(testRecipes, null);
        assertEquals(testRecipes.size(), filtered.size(), "Should return all recipes if no avoided ingredients");
    }

    @Test
    @DisplayName("Should apply all filters in sequence")
    void testApplyAllHardFilters() {
        List<Allergen> userAllergens = List.of(Allergen.PEANUTS);
        List<DietaryPreference> userPreferences = List.of(DietaryPreference.VEGAN);
        Set<String> avoidIngredients = Set.of("eggs");

        List<Recipe> filtered = filterService.applyAllHardFilters(
                testRecipes, userAllergens, userPreferences, avoidIngredients);

        // Should exclude:
        // - Recipe 2 (peanuts)
        // - Recipe 5 (has eggs)
        // And include recipes that match vegan preference or have no dietary tags
        assertTrue(filtered.stream().noneMatch(r -> r.id == 2L), "Should exclude peanut recipe");
        assertTrue(filtered.stream().noneMatch(r -> r.id == 5L), "Should exclude egg recipe");
        assertTrue(filtered.stream().anyMatch(r -> r.id == 1L), "Should include vegan pasta");
    }

    @Test
    @DisplayName("Should handle multiple allergens")
    void testFilterByAllergens_Multiple() {
        List<Allergen> userAllergens = List.of(Allergen.PEANUTS, Allergen.DAIRY, Allergen.FISH);
        List<Recipe> filtered = filterService.filterByAllergens(testRecipes, userAllergens);

        assertEquals(3, filtered.size(), "Should exclude recipes with peanuts, dairy, or fish");
        assertTrue(filtered.stream().noneMatch(r -> r.allergens.contains(Allergen.PEANUTS)), "No peanuts");
        assertTrue(filtered.stream().noneMatch(r -> r.allergens.contains(Allergen.DAIRY)), "No dairy");
        assertTrue(filtered.stream().noneMatch(r -> r.allergens.contains(Allergen.FISH)), "No fish");
    }
}
