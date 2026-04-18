package org.dsoft.control.parser;

import org.dsoft.entity.model.Allergen;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Handles allergen-to-ingredient mapping.
 * Allergens are health-related but distinct from intolerances as they cause immune reactions.
 */
@ApplicationScoped
public class AllergenParser {

    /**
     * Get ingredient names that contain a specific allergen
     */
    public Set<String> getAllergenIngredients(Allergen allergen) {
        return switch (allergen) {
            case PEANUTS -> Set.of("peanuts", "peanut butter", "peanut oil", "ground peanuts");
            case TREE_NUTS -> Set.of("almonds", "walnuts", "pecans", "cashews", "macadamia nuts", "brazil nuts", "pine nuts", "hazelnuts", "pistachios");
            case DAIRY -> Set.of("milk", "cream", "butter", "cheese", "yogurt", "whey", "casein", "lactose", "ghee", "ice cream");
            case EGGS -> Set.of("eggs", "egg white", "egg yolk", "mayonnaise");
            case FISH -> Set.of("salmon", "tuna", "bass", "cod", "halibut", "trout", "sardines", "anchovies", "tilapia", "pollock");
            case SHELLFISH -> Set.of("shrimp", "crab", "lobster", "oysters", "mussels", "clams", "scallops", "prawns");
            case SOY -> Set.of("soy", "soybean", "tofu", "tempeh", "soy sauce", "miso", "edamame", "soy milk");
            case WHEAT -> Set.of("wheat", "bread", "pasta", "flour", "cereals", "couscous", "bulgur", "semolina");
            case GLUTEN -> Set.of("gluten", "wheat", "barley", "rye", "bread", "pasta", "flour", "oats", "cereals");
            case SESAME -> Set.of("sesame", "sesame oil", "tahini", "sesame seeds");
            case MUSTARD -> Set.of("mustard", "mustard powder", "mustard seed");
            case SULFITES -> Set.of("dried fruits", "wine", "vinegar", "processed meats", "canned foods", "bottled lemon juice");
            case CELERY -> Set.of("celery", "celery seed", "celeriac", "celery salt");
            case LUPIN -> Set.of("lupin", "lupin flour", "lupini beans");
            case MOLLUSKS -> Set.of("squid", "octopus", "snail", "abalone");
        };
    }

    /**
     * Get all ingredient names for multiple allergens
     */
    public Set<String> getAllAllergenIngredients(List<Allergen> allergens) {
        if (allergens == null || allergens.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> allergenIngredients = new java.util.HashSet<>();
        for (Allergen allergen : allergens) {
            allergenIngredients.addAll(getAllergenIngredients(allergen));
        }
        return allergenIngredients;
    }
}
