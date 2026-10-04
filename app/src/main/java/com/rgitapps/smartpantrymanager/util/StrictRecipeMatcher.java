package com.rgitapps.smartpantrymanager.util;

import com.rgitapps.smartpantrymanager.data.entity.PantryItem;
import com.rgitapps.smartpantrymanager.data.entity.RecipeIngredient;
import com.rgitapps.smartpantrymanager.data.entity.RecipeWithIngredients;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StrictRecipeMatcher {

    public static class MissingIngredientInfo {
        private final String ingredientName;
        private final double requiredQuantity;
        private final String requiredUnit;
        private final double availableQuantity;

        public MissingIngredientInfo(String ingredientName, double requiredQuantity, String requiredUnit, double availableQuantity) {
            this.ingredientName = ingredientName;
            this.requiredQuantity = requiredQuantity;
            this.requiredUnit = requiredUnit;
            this.availableQuantity = availableQuantity;
        }

        public String getIngredientName() {
            return ingredientName;
        }

        public double getRequiredQuantity() {
            return requiredQuantity;
        }

        public String getRequiredUnit() {
            return requiredUnit;
        }

        public double getAvailableQuantity() {
            return availableQuantity;
        }
    }

    public static class MatchResult {
        private final boolean isMatched;
        private final int totalIngredientsCount;
        private final int availableIngredientsCount;
        private final List<MissingIngredientInfo> missingIngredients;

        public MatchResult(boolean isMatched, int totalIngredientsCount, int availableIngredientsCount, List<MissingIngredientInfo> missingIngredients) {
            this.isMatched = isMatched;
            this.totalIngredientsCount = totalIngredientsCount;
            this.availableIngredientsCount = availableIngredientsCount;
            this.missingIngredients = missingIngredients != null ? missingIngredients : new ArrayList<>();
        }

        public boolean isMatched() {
            return isMatched;
        }

        public int getTotalIngredientsCount() {
            return totalIngredientsCount;
        }

        public int getAvailableIngredientsCount() {
            return availableIngredientsCount;
        }

        public List<MissingIngredientInfo> getMissingIngredients() {
            return missingIngredients;
        }
    }

    public static boolean isStrictMatch(RecipeWithIngredients recipeWithIngredients, List<PantryItem> pantryItems) {
        return analyzeRecipeMatch(recipeWithIngredients, pantryItems).isMatched();
    }

    public static MatchResult analyzeRecipeMatch(RecipeWithIngredients recipeWithIngredients, List<PantryItem> pantryItems) {
        if (recipeWithIngredients == null || recipeWithIngredients.getIngredients() == null || recipeWithIngredients.getIngredients().isEmpty()) {
            return new MatchResult(false, 0, 0, new ArrayList<>());
        }

        List<RecipeIngredient> requiredIngredients = recipeWithIngredients.getIngredients();
        int totalIngredients = requiredIngredients.size();
        int availableIngredientsCount = 0;
        List<MissingIngredientInfo> missingList = new ArrayList<>();

        List<PantryItem> pantryList = pantryItems != null ? pantryItems : new ArrayList<>();

        for (RecipeIngredient req : requiredIngredients) {
            if (req == null || req.getIngredientName() == null) continue;

            String reqNameNorm = normalizeName(req.getIngredientName());
            String reqUnitNorm = normalizeUnit(req.getUnit());

            double totalAvailableInReqUnit = 0.0;
            boolean foundAny = false;

            for (PantryItem pantryItem : pantryList) {
                if (pantryItem == null || pantryItem.getName() == null) continue;
                String pantryNameNorm = normalizeName(pantryItem.getName());

                if (namesMatch(reqNameNorm, pantryNameNorm)) {
                    foundAny = true;
                    String pantryUnitNorm = normalizeUnit(pantryItem.getUnit());
                    double convertedQty = convertQuantity(pantryItem.getQuantity(), pantryUnitNorm, reqUnitNorm);
                    totalAvailableInReqUnit += convertedQty;
                }
            }

            if (foundAny && (totalAvailableInReqUnit >= req.getRequiredQuantity() - 0.0001)) {
                availableIngredientsCount++;
            } else {
                missingList.add(new MissingIngredientInfo(
                        req.getIngredientName(),
                        req.getRequiredQuantity(),
                        req.getUnit(),
                        totalAvailableInReqUnit
                ));
            }
        }

        boolean isMatched = (availableIngredientsCount == totalIngredients) && (totalIngredients > 0);
        return new MatchResult(isMatched, totalIngredients, availableIngredientsCount, missingList);
    }

    /**
     * Handles ingredient name pluralism (one vs many relationship, e.g. "tomato" vs "tomatoes",
     * reducing plural forms to their singular base stem so matching works seamlessly).
     */
    public static String normalizeName(String name) {
        if (name == null) return "";
        String norm = name.trim().toLowerCase(Locale.US).replaceAll("\\s+", " ");
        // Pluralism rule: reduce regular plural endings ("es", "s") to singular base form
        if (norm.endsWith("es") && norm.length() > 3) {
            norm = norm.substring(0, norm.length() - 2);
        } else if (norm.endsWith("s") && !norm.endsWith("ss") && norm.length() > 3) {
            norm = norm.substring(0, norm.length() - 1);
        }
        return norm;
    }

    /**
     * Performs unit spelling sanity checking and canonicalizes valid English measurement units
     * rather than replacing terms with arbitrary full words.
     */
    public static String normalizeUnit(String unit) {
        if (unit == null) return "";
        String u = unit.trim().toLowerCase(Locale.US);

        // Sanity check for spelling correctness and recognition of standard English units
        return switch (u) {
            case "g", "gram", "grams" -> "g";
            case "kg", "kilogram", "kilograms" -> "kg";
            case "mg", "milligram", "milligrams" -> "mg";
            case "ml", "milliliter", "milliliters", "millilitre", "millilitres" -> "ml";
            case "l", "liter", "liters", "litre", "litres" -> "l";
            case "cup", "cups" -> "cup";
            case "tbsp", "tablespoon", "tablespoons" -> "tbsp";
            case "tsp", "teaspoon", "teaspoons" -> "tsp";
            case "pc", "pcs", "piece", "pieces", "item", "items" -> "pc";
            case "slice", "slices" -> "slice";
            case "clove", "cloves" -> "clove";
            case "stalk", "stalks" -> "stalk";
            case "can", "cans" -> "can";
            case "pinch", "pinches" -> "pinch";
            case "leaf", "leaves" -> "leaf";
            case "loaf", "loaves" -> "loaf";
            case "oz", "ounce", "ounces" -> "oz";
            case "lb", "lbs", "pound", "pounds" -> "lb";
            default -> {
                // Sanity check: strip trailing plural s for unrecognized regular units
                if (u.endsWith("s") && u.length() > 2) {
                    yield u.substring(0, u.length() - 1);
                }
                yield u;
            }
        };
    }

    public static boolean namesMatch(String norm1, String norm2) {
        if (norm1.equals(norm2)) return true;
        return norm1.contains(norm2) || norm2.contains(norm1);
    }

    public static double convertQuantity(double qty, String fromUnitNorm, String toUnitNorm) {
        if (fromUnitNorm.equals(toUnitNorm)) {
            return qty;
        }

        // Handle Bread loaf <-> slice/pc conversion (1 loaf = 20 slices)
        if ((fromUnitNorm.equals("loaf") && (toUnitNorm.equals("slice") || toUnitNorm.equals("pc"))) ||
            (fromUnitNorm.equals("loaf") && toUnitNorm.equals("slices"))) {
            return qty * 20.0;
        }
        if (((fromUnitNorm.equals("slice") || fromUnitNorm.equals("slices") || fromUnitNorm.equals("pc")) && toUnitNorm.equals("loaf"))) {
            return qty / 20.0;
        }

        // Handle cheese/slices <-> cup conversion (approximate: 1 cup = 16 slices)
        if ((fromUnitNorm.equals("cup") || fromUnitNorm.equals("cups")) && (toUnitNorm.equals("slice") || toUnitNorm.equals("slices"))) {
            return qty * 16.0;
        }
        if ((fromUnitNorm.equals("slice") || fromUnitNorm.equals("slices")) && (toUnitNorm.equals("cup") || toUnitNorm.equals("cups"))) {
            return qty / 16.0;
        }

        Double fromMl = toMl(qty, fromUnitNorm);
        Double toMlVal = toMl(1.0, toUnitNorm);
        if (fromMl != null && toMlVal != null && toMlVal > 0) {
            return fromMl / toMlVal;
        }

        Double fromG = toGrams(qty, fromUnitNorm);
        Double toGVal = toGrams(1.0, toUnitNorm);
        if (fromG != null && toGVal != null && toGVal > 0) {
            return fromG / toGVal;
        }

        return qty;
    }

    private static Double toMl(double qty, String unitNorm) {
        return switch (unitNorm) {
            case "ml" -> qty;
            case "l" -> qty * 1000.0;
            case "cup" -> qty * 240.0;
            case "tbsp" -> qty * 15.0;
            case "tsp" -> qty * 5.0;
            default -> null;
        };
    }

    private static Double toGrams(double qty, String unitNorm) {
        return switch (unitNorm) {
            case "g" -> qty;
            case "kg" -> qty * 1000.0;
            default -> null;
        };
    }
}
