package org.dsoft.control.parser;

import java.util.HashSet;
import java.util.Set;

public class HealthConditionParserRuleBased {
    
    public Set<String> parseHealthConditions(String medicalConditions, String intolerances) {
        Set<String> avoidIngredients = new HashSet<>();
        
        String combined = (medicalConditions != null ? medicalConditions : "") + " " + 
                          (intolerances != null ? intolerances : "");
        String lowerCombined = combined.toLowerCase();
        
        // Dairy-related issues
        if (lowerCombined.contains("lactose") || lowerCombined.contains("dairy") || 
            lowerCombined.contains("intolerant to milk")) {
            avoidIngredients.addAll(Set.of("milk", "cheese", "butter", "lactose", "dairy", 
                                             "cream", "yogurt", "whey", "casein", "ice cream"));
        }
        
        // Gluten/Celiac
        if (lowerCombined.contains("celiac") || lowerCombined.contains("gluten")) {
            avoidIngredients.addAll(Set.of("wheat", "gluten", "barley", "rye", "oats", 
                                             "bread", "pasta", "flour", "bran"));
        }
        
        // Shellfish allergy
        if (lowerCombined.contains("shellfish") || lowerCombined.contains("shrimp")) {
            avoidIngredients.addAll(Set.of("shellfish", "shrimp", "crab", "lobster", 
                                             "clam", "mussels", "oyster", "scallops"));
        }
        
        // Peanut allergy
        if (lowerCombined.contains("peanut")) {
            avoidIngredients.addAll(Set.of("peanuts", "peanut oil", "peanut butter"));
        }
        
        // Tree nut allergy
        if (lowerCombined.contains("tree nuts") || lowerCombined.contains("nut allergy") || 
            lowerCombined.contains("tree nut") || lowerCombined.contains("nuts")) {
            avoidIngredients.addAll(Set.of(
                "almonds", "almond", "cashews", "cashew", "walnuts", "walnut",
                "pecans", "pecan", "hazelnuts", "hazelnut", "nuts", "tree nuts", "macadamia"
            ));
        }
        
        // Egg allergy
        if (lowerCombined.contains("egg")) {
            avoidIngredients.addAll(Set.of("eggs", "egg", "mayonnaise", "meringue"));
        }
        
        // Soy allergy/intolerance
        if (lowerCombined.contains("soy")) {
            avoidIngredients.addAll(Set.of("soy", "soybean", "tofu", "edamame", "soy sauce", "tempeh"));
        }
        
        // Diabetes - avoid sugary/refined foods
        if (lowerCombined.contains("diabetes") || lowerCombined.contains("diabetic") || 
            lowerCombined.contains("glucose") || lowerCombined.contains("blood sugar")) {
            avoidIngredients.addAll(Set.of("sugar", "honey", "high fructose corn syrup", 
                                             "white rice", "white bread", "glucose", "dextrose", 
                                             "sucrose", "candy", "soda", "pastries", "syrup"));
        }
        
        // Hypertension - avoid salt and processed foods
        if (lowerCombined.contains("hypertension") || lowerCombined.contains("high blood pressure")) {
            avoidIngredients.addAll(Set.of("salt", "sodium", "canned vegetables", "cured meats", 
                                             "soy sauce", "bacon", "ham", "salami", "butter"));
        }
        
        // Hypothyroidism - avoid goitrogens
        if (lowerCombined.contains("hypothyroidism") || lowerCombined.contains("thyroid")) {
            avoidIngredients.addAll(Set.of("broccoli", "cabbage", "cauliflower", "kale", 
                                             "soy", "tofu", "millet", "turnips"));
        }
        
        // IBS - avoid trigger foods (general)
        if (lowerCombined.contains("ibs") || lowerCombined.contains("irritable bowel")) {
            avoidIngredients.addAll(Set.of("caffeine", "dairy", "wheat", "beans", 
                                             "artificial sweeteners", "fried foods", "spicy foods"));
        }
        
        // Crohn's disease - avoid inflammatory foods
        if (lowerCombined.contains("crohn") || lowerCombined.contains("inflammatory bowel")) {
            avoidIngredients.addAll(Set.of("dairy", "nuts", "seeds", "popcorn", "corn", 
                                             "beans", "caffeine", "alcohol", "spicy foods"));
        }
        
        // GERD/Acid reflux - avoid acidic/fatty foods
        if (lowerCombined.contains("gerd") || lowerCombined.contains("acid reflux") || 
            lowerCombined.contains("reflux")) {
            avoidIngredients.addAll(Set.of("tomato", "lemon", "orange", "chocolate", "caffeine", 
                                             "chili", "garlic", "onion", "butter", "alcohol"));
        }
        
        // Migraines - avoid common triggers
        if (lowerCombined.contains("migraine") || lowerCombined.contains("headache")) {
            avoidIngredients.addAll(Set.of("caffeine", "alcohol", "chocolate", "cheese", 
                                             "cured meats", "msg", "aspartame", "nitrates"));
        }
        
        // Asthma - general triggers
        if (lowerCombined.contains("asthma")) {
            avoidIngredients.addAll(Set.of("sulfites", "mollusks", "shellfish", "peanuts", "eggs"));
        }
        
        return avoidIngredients;
    }
}
