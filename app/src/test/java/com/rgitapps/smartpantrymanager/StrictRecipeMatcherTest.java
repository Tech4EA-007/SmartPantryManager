package com.rgitapps.smartpantrymanager;

import com.rgitapps.smartpantrymanager.data.entity.PantryItem;
import com.rgitapps.smartpantrymanager.data.entity.Recipe;
import com.rgitapps.smartpantrymanager.data.entity.RecipeIngredient;
import com.rgitapps.smartpantrymanager.data.entity.RecipeWithIngredients;
import com.rgitapps.smartpantrymanager.util.StrictRecipeMatcher;
import com.rgitapps.smartpantrymanager.util.StrictRecipeMatcher.MatchResult;
import com.rgitapps.smartpantrymanager.util.StrictRecipeMatcher.MissingIngredientInfo;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class StrictRecipeMatcherTest {

    private RecipeWithIngredients createRecipe(String name, RecipeIngredient... ingredients) {
        Recipe recipe = new Recipe(name, "Category", "Instructions");
        return new RecipeWithIngredients(recipe, Arrays.asList(ingredients));
    }

    // ==========================================
    // 1. Tests for StrictRecipeMatcher.isStrictMatch
    // ==========================================

    @Test
    public void isStrictMatch_returnsTrue_when100PercentIngredientsExistWithExactQuantity() {
        RecipeIngredient req1 = new RecipeIngredient(1, "Eggs", 2.0, "pcs");
        RecipeIngredient req2 = new RecipeIngredient(1, "Milk", 1.0, "cup");
        RecipeWithIngredients recipe = createRecipe("Omelet", req1, req2);

        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("Eggs", 2.0, "pcs", "Dairy", "2026-05-01"),
                new PantryItem("Milk", 1.0, "cup", "Dairy", "2026-05-01")
        );

        assertTrue("Should return true when all required ingredients exist with exact quantity",
                StrictRecipeMatcher.isStrictMatch(recipe, pantry));
    }

    @Test
    public void isStrictMatch_returnsTrue_whenPantryHasSufficientExcessQuantity() {
        RecipeIngredient req1 = new RecipeIngredient(1, "Eggs", 2.0, "pcs");
        RecipeIngredient req2 = new RecipeIngredient(1, "Milk", 1.0, "cup");
        RecipeWithIngredients recipe = createRecipe("Omelet", req1, req2);

        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("Eggs", 12.0, "pcs", "Dairy", "2026-05-01"),
                new PantryItem("Milk", 5.0, "cups", "Dairy", "2026-05-01")
        );

        assertTrue("Should return true when pantry has more than required quantity",
                StrictRecipeMatcher.isStrictMatch(recipe, pantry));
    }

    @Test
    public void isStrictMatch_returnsTrue_whenMultiplePantryItemsCombineToSufficientQuantity() {
        RecipeIngredient req = new RecipeIngredient(1, "Eggs", 3.0, "pcs");
        RecipeWithIngredients recipe = createRecipe("Egg Dish", req);

        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("Eggs", 1.0, "pcs", "Dairy", "2026-05-01"),
                new PantryItem("Eggs", 2.0, "pcs", "Dairy", "2026-05-02")
        );

        assertTrue("Should combine multiple pantry items for the same ingredient",
                StrictRecipeMatcher.isStrictMatch(recipe, pantry));
    }

    @Test
    public void isStrictMatch_returnsFalse_whenAnIngredientIsMissing() {
        RecipeIngredient req1 = new RecipeIngredient(1, "Eggs", 2.0, "pcs");
        RecipeIngredient req2 = new RecipeIngredient(1, "Milk", 1.0, "cup");
        RecipeIngredient req3 = new RecipeIngredient(1, "Cheese", 1.0, "slice");
        RecipeWithIngredients recipe = createRecipe("Cheesy Omelet", req1, req2, req3);

        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("Eggs", 2.0, "pcs", "Dairy", "2026-05-01"),
                new PantryItem("Milk", 1.0, "cup", "Dairy", "2026-05-01")
        );

        assertFalse("Should return false when Cheese is missing from pantry",
                StrictRecipeMatcher.isStrictMatch(recipe, pantry));
    }

    @Test
    public void recipeNeeds5IngredientsHas4_excludedFromStrictSuggestions_includedInAlmostThere() {
        RecipeIngredient req1 = new RecipeIngredient(1, "Eggs", 1.0, "pcs");
        RecipeIngredient req2 = new RecipeIngredient(1, "Milk", 1.0, "cup");
        RecipeIngredient req3 = new RecipeIngredient(1, "Flour", 1.0, "cup");
        RecipeIngredient req4 = new RecipeIngredient(1, "Sugar", 1.0, "cup");
        RecipeIngredient req5 = new RecipeIngredient(1, "Butter", 1.0, "tbsp");
        RecipeWithIngredients recipe = createRecipe("Cake", req1, req2, req3, req4, req5);

        // Pantry has 4 out of 5 ingredients (Butter is missing)
        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("Eggs", 1.0, "pcs", "Dairy", "2026-05-01"),
                new PantryItem("Milk", 1.0, "cup", "Dairy", "2026-05-01"),
                new PantryItem("Flour", 1.0, "cup", "Pantry", "2026-05-01"),
                new PantryItem("Sugar", 1.0, "cup", "Pantry", "2026-05-01")
        );

        // Must NOT appear in strict suggestions
        assertFalse("Recipe needing 5 ingredients with only 4 in pantry must NOT be a strict match",
                StrictRecipeMatcher.isStrictMatch(recipe, pantry));

        // Must be identified as "Almost There" (missing exactly 1 ingredient)
        MatchResult result = StrictRecipeMatcher.analyzeRecipeMatch(recipe, pantry);
        assertFalse(result.isMatched());
        assertEquals(5, result.getTotalIngredientsCount());
        assertEquals(4, result.getAvailableIngredientsCount());
        assertEquals(1, result.getMissingIngredients().size());
        assertEquals("Butter", result.getMissingIngredients().get(0).getIngredientName());
    }

    @Test
    public void isStrictMatch_returnsFalse_whenPantryIsEmpty() {
        RecipeIngredient req = new RecipeIngredient(1, "Eggs", 2.0, "pcs");
        RecipeWithIngredients recipe = createRecipe("Omelet", req);

        assertFalse("Should return false when pantry is empty",
                StrictRecipeMatcher.isStrictMatch(recipe, new ArrayList<>()));
        assertFalse("Should return false when pantry is null",
                StrictRecipeMatcher.isStrictMatch(recipe, null));
    }

    @Test
    public void isStrictMatch_returnsFalse_whenIngredientExistsButQuantityIsInsufficient() {
        RecipeIngredient req = new RecipeIngredient(1, "Eggs", 3.0, "pcs");
        RecipeWithIngredients recipe = createRecipe("Omelet", req);

        List<PantryItem> pantry = Collections.singletonList(
                new PantryItem("Eggs", 2.0, "pcs", "Dairy", "2026-05-01")
        );

        assertFalse("Should return false when pantry has 2 eggs but recipe requires 3",
                StrictRecipeMatcher.isStrictMatch(recipe, pantry));
    }

    @Test
    public void isStrictMatch_returnsFalse_whenMultiplePantryItemsSumIsInsufficient() {
        RecipeIngredient req = new RecipeIngredient(1, "Eggs", 5.0, "pcs");
        RecipeWithIngredients recipe = createRecipe("Omelet", req);

        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("Eggs", 2.0, "pcs", "Dairy", "2026-05-01"),
                new PantryItem("Eggs", 2.0, "pcs", "Dairy", "2026-05-02")
        );

        assertFalse("Should return false when combined total of 4 eggs is less than 5 required",
                StrictRecipeMatcher.isStrictMatch(recipe, pantry));
    }

    @Test
    public void isStrictMatch_handlesCaseInsensitiveIngredientMatching() {
        RecipeIngredient req1 = new RecipeIngredient(1, "EGGS", 2.0, "PCS");
        RecipeIngredient req2 = new RecipeIngredient(1, "mIlK", 1.0, "CUP");
        RecipeWithIngredients recipe = createRecipe("Omelet", req1, req2);

        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("eggs", 2.0, "pcs", "Dairy", "2026-05-01"),
                new PantryItem("Milk", 1.0, "cup", "Dairy", "2026-05-01")
        );

        assertTrue("Should handle case-insensitive ingredient name and unit matching",
                StrictRecipeMatcher.isStrictMatch(recipe, pantry));
    }

    @Test
    public void isStrictMatch_handlesPluralizationAndWhitespace() {
        RecipeIngredient req1 = new RecipeIngredient(1, "  Tomato  ", 2.0, "pcs");
        RecipeIngredient req2 = new RecipeIngredient(1, "Egg", 2.0, "pcs");
        RecipeWithIngredients recipe = createRecipe("Salad", req1, req2);

        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("Tomatoes", 2.0, "pcs", "Produce", "2026-05-01"),
                new PantryItem("Eggs", 2.0, "pcs", "Dairy", "2026-05-01")
        );

        assertTrue("Should handle pluralization and leading/trailing whitespace ('tomato' vs 'tomatoes')",
                StrictRecipeMatcher.isStrictMatch(recipe, pantry));
    }

    @Test
    public void isStrictMatch_handlesUnitNormalizationAndConversions() {
        // Test unit normalization: "tablespoon" vs "tbsp", "pieces" vs "pcs"
        RecipeIngredient req1 = new RecipeIngredient(1, "Butter", 1.0, "tbsp");
        RecipeIngredient req2 = new RecipeIngredient(1, "Bread", 2.0, "pcs");
        RecipeWithIngredients recipe1 = createRecipe("Toast", req1, req2);

        List<PantryItem> pantry1 = Arrays.asList(
                new PantryItem("Butter", 1.0, "tablespoon", "Dairy", "2026-05-01"),
                new PantryItem("Bread", 2.0, "pieces", "Bakery", "2026-05-01")
        );

        assertTrue("Should normalize unit synonyms like tablespoon -> tbsp, pieces -> pcs",
                StrictRecipeMatcher.isStrictMatch(recipe1, pantry1));

        // Test weight conversion: Required 500 g, Pantry has 1 kg
        RecipeIngredient req3 = new RecipeIngredient(1, "Flour", 500.0, "g");
        RecipeWithIngredients recipe2 = createRecipe("Cake", req3);

        List<PantryItem> pantry2 = Collections.singletonList(
                new PantryItem("Flour", 1.0, "kg", "Pantry", "2026-05-01")
        );

        assertTrue("Should convert 1 kg in pantry to 1000 g to satisfy 500 g requirement",
                StrictRecipeMatcher.isStrictMatch(recipe2, pantry2));

        // Test volume conversion: Required 250 ml, Pantry has 1 l
        RecipeIngredient req4 = new RecipeIngredient(1, "Water", 250.0, "ml");
        RecipeWithIngredients recipe3 = createRecipe("Soup", req4);

        List<PantryItem> pantry3 = Collections.singletonList(
                new PantryItem("Water", 1.0, "l", "Beverage", "2026-05-01")
        );

        assertTrue("Should convert 1 l in pantry to 1000 ml to satisfy 250 ml requirement",
                StrictRecipeMatcher.isStrictMatch(recipe3, pantry3));
    }

    @Test
    public void isStrictMatch_returnsFalse_forNullOrEmptyRecipe() {
        assertFalse("Should return false for null recipe",
                StrictRecipeMatcher.isStrictMatch(null, Collections.emptyList()));

        RecipeWithIngredients emptyRecipe = new RecipeWithIngredients(new Recipe("Empty", "Cat", "Inst"), new ArrayList<>());
        assertFalse("Should return false for recipe with no ingredients",
                StrictRecipeMatcher.isStrictMatch(emptyRecipe, Collections.emptyList()));
    }

    // ==========================================
    // 2. Tests for StrictRecipeMatcher.analyzeRecipeMatch
    // ==========================================

    @Test
    public void analyzeRecipeMatch_returnsCorrectStructure_whenCompletelyMatched() {
        RecipeIngredient req1 = new RecipeIngredient(1, "Eggs", 2.0, "pcs");
        RecipeIngredient req2 = new RecipeIngredient(1, "Milk", 1.0, "cup");
        RecipeWithIngredients recipe = createRecipe("Omelet", req1, req2);

        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("Eggs", 2.0, "pcs", "Dairy", "2026-05-01"),
                new PantryItem("Milk", 1.0, "cup", "Dairy", "2026-05-01")
        );

        MatchResult result = StrictRecipeMatcher.analyzeRecipeMatch(recipe, pantry);

        assertNotNull("Result should not be null", result);
        assertTrue("isMatched should be true", result.isMatched());
        assertEquals("totalIngredientsCount should be 2", 2, result.getTotalIngredientsCount());
        assertEquals("availableIngredientsCount should be 2", 2, result.getAvailableIngredientsCount());
        assertTrue("missingIngredients list should be empty", result.getMissingIngredients().isEmpty());
    }

    @Test
    public void analyzeRecipeMatch_correctlyListsMissingAndInsufficientIngredients() {
        RecipeIngredient req1 = new RecipeIngredient(1, "Eggs", 4.0, "pcs");
        RecipeIngredient req2 = new RecipeIngredient(1, "Milk", 2.0, "cups");
        RecipeIngredient req3 = new RecipeIngredient(1, "Butter", 1.0, "tbsp");
        RecipeWithIngredients recipe = createRecipe("Pancakes", req1, req2, req3);

        // Pantry has: Eggs (only 2 available vs 4 required), Milk (0 available), Butter (1 tbsp available)
        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("Eggs", 2.0, "pcs", "Dairy", "2026-05-01"),
                new PantryItem("Butter", 1.0, "tbsp", "Dairy", "2026-05-01")
        );

        MatchResult result = StrictRecipeMatcher.analyzeRecipeMatch(recipe, pantry);

        assertNotNull("Result should not be null", result);
        assertFalse("isMatched should be false", result.isMatched());
        assertEquals("totalIngredientsCount should be 3", 3, result.getTotalIngredientsCount());
        assertEquals("availableIngredientsCount should be 1", 1, result.getAvailableIngredientsCount());

        List<MissingIngredientInfo> missingList = result.getMissingIngredients();
        assertEquals("There should be 2 missing/insufficient ingredients", 2, missingList.size());

        // Verify Eggs info (insufficient)
        MissingIngredientInfo eggsInfo = missingList.stream()
                .filter(i -> i.getIngredientName().equalsIgnoreCase("Eggs"))
                .findFirst()
                .orElse(null);
        assertNotNull("Eggs should be in missing list", eggsInfo);
        assertEquals(4.0, eggsInfo.getRequiredQuantity(), 0.001);
        assertEquals("pcs", eggsInfo.getRequiredUnit());
        assertEquals(2.0, eggsInfo.getAvailableQuantity(), 0.001);

        // Verify Milk info (completely missing)
        MissingIngredientInfo milkInfo = missingList.stream()
                .filter(i -> i.getIngredientName().equalsIgnoreCase("Milk"))
                .findFirst()
                .orElse(null);
        assertNotNull("Milk should be in missing list", milkInfo);
        assertEquals(2.0, milkInfo.getRequiredQuantity(), 0.001);
        assertEquals("cups", milkInfo.getRequiredUnit());
        assertEquals(0.0, milkInfo.getAvailableQuantity(), 0.001);
    }

    @Test
    public void analyzeRecipeMatch_reflectsConvertedAvailableQuantitiesForMissingItems() {
        // Recipe requires 1000 g flour, pantry has 0.25 kg (250 g)
        RecipeIngredient req = new RecipeIngredient(1, "Flour", 1000.0, "g");
        RecipeWithIngredients recipe = createRecipe("Bread", req);

        List<PantryItem> pantry = Collections.singletonList(
                new PantryItem("Flour", 0.25, "kg", "Pantry", "2026-05-01")
        );

        MatchResult result = StrictRecipeMatcher.analyzeRecipeMatch(recipe, pantry);

        assertFalse("isMatched should be false", result.isMatched());
        assertEquals(1, result.getMissingIngredients().size());

        MissingIngredientInfo flourInfo = result.getMissingIngredients().get(0);
        assertEquals("Flour", flourInfo.getIngredientName());
        assertEquals(1000.0, flourInfo.getRequiredQuantity(), 0.001);
        assertEquals("g", flourInfo.getRequiredUnit());
        assertEquals(250.0, flourInfo.getAvailableQuantity(), 0.001);
    }

    @Test
    public void analyzeRecipeMatch_handlesNullOrEmptyInputsSafely() {
        MatchResult nullRecipeResult = StrictRecipeMatcher.analyzeRecipeMatch(null, Collections.emptyList());
        assertFalse(nullRecipeResult.isMatched());
        assertEquals(0, nullRecipeResult.getTotalIngredientsCount());
        assertEquals(0, nullRecipeResult.getAvailableIngredientsCount());
        assertTrue(nullRecipeResult.getMissingIngredients().isEmpty());

        RecipeWithIngredients recipe = createRecipe("Test", new RecipeIngredient(1, "Sugar", 1.0, "cup"));
        MatchResult nullPantryResult = StrictRecipeMatcher.analyzeRecipeMatch(recipe, null);
        assertFalse(nullPantryResult.isMatched());
        assertEquals(1, nullPantryResult.getTotalIngredientsCount());
        assertEquals(0, nullPantryResult.getAvailableIngredientsCount());
        assertEquals(1, nullPantryResult.getMissingIngredients().size());
        assertEquals(0.0, nullPantryResult.getMissingIngredients().get(0).getAvailableQuantity(), 0.001);
    }
}
