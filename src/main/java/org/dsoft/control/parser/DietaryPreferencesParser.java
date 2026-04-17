package org.dsoft.control.parser;

import org.dsoft.entity.model.DietaryPreference;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.Set;

/**
 * Parses dietary preferences and returns incompatible food categories.
 * Separated from health conditions as dietary choices differ from medical needs.
 */
@ApplicationScoped
public class DietaryPreferencesParser {

    /**
     * Get foods that conflict with a dietary preference
     */
    public Set<String> getIncompatibleFoods(DietaryPreference preference) {
        return switch (preference) {
            case VEGETARIAN -> Set.of("beef", "pork", "lamb", "chicken", "turkey", "fish", "seafood", "meat", "lard", "gelatin", "anchovies", "bacon", "ham");
            
            case VEGAN -> Set.of("beef", "pork", "lamb", "chicken", "turkey", "fish", "seafood", "meat", 
                           "milk", "cheese", "butter", "cream", "yogurt", "whey", "casein", "lactose",
                           "eggs", "egg white", "mayonnaise", "honey", "gelatin", "lard", "anchovies");
            
            case PESCATARIAN -> Set.of("beef", "pork", "lamb", "chicken", "turkey", "meat", "lard", "gelatin", "bacon", "ham");
            
            case KETO -> Set.of("rice", "bread", "pasta", "sugar", "flour", "potato", "corn", "oats", "cereals", 
                          "fruit", "fruit juice", "soda", "beans", "lentils", "legumes", "honey", "maple syrup");
            
            case PALEO -> Set.of("grains", "wheat", "oats", "rice", "bread", "pasta", "cereals", "flour",
                           "legumes", "beans", "lentils", "peanuts", "peas",
                           "dairy", "milk", "cheese", "butter", "yogurt", "whey", "casein",
                           "processed foods", "refined sugar", "vegetable oil");
            
            case GLUTEN_FREE -> Set.of("wheat", "barley", "rye", "bread", "pasta", "flour", "cereals", "oats", "gluten");
            
            case DAIRY_FREE -> Set.of("milk", "cheese", "butter", "cream", "yogurt", "whey", "casein", "lactose", "ghee", "ice cream");
            
            case LOW_CARB -> Set.of("rice", "bread", "pasta", "sugar", "flour", "potato", "corn", "oats", "cereals",
                              "fruit", "fruit juice", "soda", "beans", "lentils", "legumes", "honey", "maple syrup", "refined sugar");
        };
    }

    /**
     * Get all incompatible foods for a list of dietary preferences
     */
    public Set<String> getAllIncompatibleFoods(java.util.List<DietaryPreference> preferences) {
        if (preferences == null || preferences.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> incompatible = new java.util.HashSet<>();
        for (DietaryPreference pref : preferences) {
            incompatible.addAll(getIncompatibleFoods(pref));
        }
        return incompatible;
    }
}
