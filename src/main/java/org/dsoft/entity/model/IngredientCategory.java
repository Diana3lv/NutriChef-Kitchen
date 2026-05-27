package org.dsoft.entity.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum IngredientCategory {
    FRUITS("Fruits"),
    VEGETABLES("Vegetables"),
    DAIRY_AND_EGGS("Dairy & Eggs"),
    MEAT("Meat"),
    SEAFOOD("Seafood"),
    GRAINS_AND_PASTA("Grains & Pasta"),
    BAKING_AND_SPICES("Baking & Spices"),
    CANNED_GOODS("Canned Goods"),
    SAUCES_AND_CONDIMENTS("Sauces & Condiments"),
    SNACKS_AND_SWEETS("Snacks & Sweets"),
    BEVERAGES("Beverages"),
    FROZEN("Frozen"),
    OTHER("Other");

    private final String displayValue;

    IngredientCategory(String displayValue) {
        this.displayValue = displayValue;
    }

    @JsonValue
    public String getDisplayValue() {
        return displayValue;
    }

    @JsonCreator
    public static IngredientCategory fromDisplayValue(String displayValue) {
        if (displayValue == null || displayValue.isBlank()) {
            return OTHER;
        }
        for (IngredientCategory category : values()) {
            if (category.displayValue.equals(displayValue)) {
                return category;
            }
        }
        return OTHER;
    }
}