package com.rgitapps.smartpantrymanager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rgitapps.smartpantrymanager.data.dao.PantryDao;
import com.rgitapps.smartpantrymanager.data.dao.RecipeDao;
import com.rgitapps.smartpantrymanager.data.entity.PantryItem;
import com.rgitapps.smartpantrymanager.data.entity.Recipe;
import com.rgitapps.smartpantrymanager.data.entity.RecipeIngredient;
import com.rgitapps.smartpantrymanager.data.entity.RecipeWithIngredients;
import com.rgitapps.smartpantrymanager.data.repository.PantryRepository;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PantryRepositoryTest {

    private PantryRepository repository;

    @Before
    public void setUp() {
        FakePantryDao fakePantryDao = new FakePantryDao();
        FakeRecipeDao fakeRecipeDao = new FakeRecipeDao();
        // Use synchronous Runnable::run executor for immediate execution in tests
        repository = new PantryRepository(fakePantryDao, fakeRecipeDao, Runnable::run);
    }

    @Test
    public void insertPantryItem_addsItemToRepository() {
        PantryItem item = new PantryItem("Milk", 2.0, "cups", "Dairy", "2026-05-01");
        repository.insertPantryItem(item);

        List<PantryItem> items = repository.getPantryItemsList();
        assertEquals(1, items.size());
        assertEquals("Milk", items.get(0).getName());
        assertEquals(2.0, items.get(0).getQuantity(), 0.001);
    }

    @Test
    public void updatePantryItem_updatesItemInRepository() {
        PantryItem item = new PantryItem("Eggs", 12.0, "pcs", "Dairy", "2026-05-01");
        repository.insertPantryItem(item);

        PantryItem insertedItem = repository.getPantryItemsList().get(0);
        insertedItem.setQuantity(6.0);
        repository.updatePantryItem(insertedItem);

        List<PantryItem> updatedItems = repository.getPantryItemsList();
        assertEquals(1, updatedItems.size());
        assertEquals(6.0, updatedItems.get(0).getQuantity(), 0.001);
    }

    @Test
    public void deletePantryItem_removesItemFromRepository() {
        PantryItem item1 = new PantryItem("Eggs", 12.0, "pcs", "Dairy", "2026-05-01");
        PantryItem item2 = new PantryItem("Milk", 2.0, "cups", "Dairy", "2026-05-01");
        repository.insertPantryItem(item1);
        repository.insertPantryItem(item2);

        PantryItem toDelete = repository.getPantryItemsList().get(0);
        repository.deletePantryItem(toDelete);

        List<PantryItem> remainingItems = repository.getPantryItemsList();
        assertEquals(1, remainingItems.size());
        assertEquals("Milk", remainingItems.get(0).getName());
    }

    @Test
    public void insertRecipe_addsRecipeWithIngredientsToRepository() {
        Recipe recipe = new Recipe("Omelet", "Breakfast", "Cook eggs in butter.");
        RecipeIngredient ing1 = new RecipeIngredient(0, "Eggs", 2.0, "pcs");
        RecipeIngredient ing2 = new RecipeIngredient(0, "Butter", 1.0, "tbsp");

        repository.insertRecipe(recipe, Arrays.asList(ing1, ing2));

        List<RecipeWithIngredients> recipes = repository.getRecipesWithIngredientsList();
        assertEquals(1, recipes.size());
        assertEquals("Omelet", recipes.get(0).getRecipe().getName());
        assertEquals(2, recipes.get(0).getIngredients().size());
    }

    @Test
    public void cookRecipe_exactQuantity_removesPantryItem() {
        PantryItem item = new PantryItem("Eggs", 2.0, "pcs", "Dairy", "2026-05-01");
        repository.insertPantryItem(item);

        Recipe recipe = new Recipe("Omelet", "Breakfast", "Cook eggs.");
        RecipeIngredient req = new RecipeIngredient(0, "Eggs", 2.0, "pcs");
        RecipeWithIngredients recipeWithIngredients = new RecipeWithIngredients(recipe, Collections.singletonList(req));

        AtomicBoolean listenerCalled = new AtomicBoolean(false);
        repository.cookRecipe(recipeWithIngredients, () -> listenerCalled.set(true));

        assertTrue("Cook complete listener should be invoked", listenerCalled.get());
        List<PantryItem> remainingPantry = repository.getPantryItemsList();
        assertEquals("Pantry item should be deleted after exact quantity consumed", 0, remainingPantry.size());
    }

    @Test
    public void cookRecipe_excessQuantity_reducesPantryItemQuantity() {
        PantryItem item = new PantryItem("Eggs", 10.0, "pcs", "Dairy", "2026-05-01");
        repository.insertPantryItem(item);

        Recipe recipe = new Recipe("Omelet", "Breakfast", "Cook eggs.");
        RecipeIngredient req = new RecipeIngredient(0, "Eggs", 3.0, "pcs");
        RecipeWithIngredients recipeWithIngredients = new RecipeWithIngredients(recipe, Collections.singletonList(req));

        repository.cookRecipe(recipeWithIngredients, null);

        List<PantryItem> remainingPantry = repository.getPantryItemsList();
        assertEquals(1, remainingPantry.size());
        assertEquals(7.0, remainingPantry.get(0).getQuantity(), 0.001);
    }

    @Test
    public void cookRecipe_unitConversion_deductsCorrectly() {
        // Pantry has 1 kg of flour, recipe requires 200 g
        PantryItem item = new PantryItem("Flour", 1.0, "kg", "Pantry", "2026-05-01");
        repository.insertPantryItem(item);

        Recipe recipe = new Recipe("Cake", "Dessert", "Bake cake.");
        RecipeIngredient req = new RecipeIngredient(0, "Flour", 200.0, "g");
        RecipeWithIngredients recipeWithIngredients = new RecipeWithIngredients(recipe, Collections.singletonList(req));

        repository.cookRecipe(recipeWithIngredients, null);

        List<PantryItem> remainingPantry = repository.getPantryItemsList();
        assertEquals(1, remainingPantry.size());
        // Remaining should be 0.8 kg
        assertEquals(0.8, remainingPantry.get(0).getQuantity(), 0.001);
    }

    @Test
    public void cookRecipe_multiplePantryItems_deductsAcrossItems() {
        // Pantry has 2 items for Eggs: Item 1 has 1 egg, Item 2 has 3 eggs
        PantryItem item1 = new PantryItem("Eggs", 1.0, "pcs", "Dairy", "2026-05-01");
        PantryItem item2 = new PantryItem("Eggs", 3.0, "pcs", "Dairy", "2026-05-02");
        repository.insertPantryItem(item1);
        repository.insertPantryItem(item2);

        Recipe recipe = new Recipe("Scrambled Eggs", "Breakfast", "Cook eggs.");
        RecipeIngredient req = new RecipeIngredient(0, "Eggs", 2.0, "pcs");
        RecipeWithIngredients recipeWithIngredients = new RecipeWithIngredients(recipe, Collections.singletonList(req));

        repository.cookRecipe(recipeWithIngredients, null);

        List<PantryItem> remainingPantry = repository.getPantryItemsList();
        // Item 1 (1 egg) consumed completely, Item 2 (3 eggs - 1 egg) has 2 eggs left
        assertEquals(1, remainingPantry.size());
        assertEquals(2.0, remainingPantry.get(0).getQuantity(), 0.001);
    }

    @Test
    public void cookRecipe_nullOrEmptyRecipe_triggersCallbackSafely() {
        AtomicBoolean listenerCalled = new AtomicBoolean(false);
        repository.cookRecipe(null, () -> listenerCalled.set(true));
        assertTrue("Callback should be called for null recipe", listenerCalled.get());

        listenerCalled.set(false);
        RecipeWithIngredients emptyRecipe = new RecipeWithIngredients(new Recipe("Empty", "Cat", "Inst"), null);
        repository.cookRecipe(emptyRecipe, () -> listenerCalled.set(true));
        assertTrue("Callback should be called for recipe with null ingredients", listenerCalled.get());
    }

    @Test
    public void resetDatabase_clearsAndSeedsData() {
        PantryItem oldItem = new PantryItem("Old Item", 10.0, "pcs", "Old", "2026-01-01");
        repository.insertPantryItem(oldItem);

        AtomicBoolean resetCompleted = new AtomicBoolean(false);
        repository.resetDatabase(() -> resetCompleted.set(true));

        assertTrue("Reset completion runnable should be executed", resetCompleted.get());
        List<PantryItem> seededPantry = repository.getPantryItemsList();
        assertFalse("Seeded pantry should contain pre-seeded items", seededPantry.isEmpty());

        List<RecipeWithIngredients> seededRecipes = repository.getRecipesWithIngredientsList();
        assertFalse("Seeded recipes should contain pre-seeded recipes", seededRecipes.isEmpty());
    }

    // ==========================================
    // Fake DAOs for in-memory testing
    // ==========================================

    public static class FakePantryDao implements PantryDao {
        private final List<PantryItem> items = new ArrayList<>();
        private int idCounter = 1;

        @Override
        public long insert(PantryItem item) {
            if (item.getId() <= 0) {
                item.setId(idCounter++);
            } else {
                items.removeIf(i -> i.getId() == item.getId());
            }
            items.add(item);
            return item.getId();
        }

        @Override
        public void update(PantryItem item) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getId() == item.getId()) {
                    items.set(i, item);
                    return;
                }
            }
        }

        @Override
        public void delete(PantryItem item) {
            items.removeIf(i -> i.getId() == item.getId());
        }

        @Override
        public void deleteAll() {
            items.clear();
        }

        @Override
        public LiveData<List<PantryItem>> getAllPantryItems() {
            MutableLiveData<List<PantryItem>> liveData = new MutableLiveData<>();
            liveData.setValue(new ArrayList<>(items));
            return liveData;
        }

        @Override
        public LiveData<List<PantryItem>> searchPantryItems(String query) {
            List<PantryItem> filtered = new ArrayList<>();
            for (PantryItem item : items) {
                if ((item.getName() != null && item.getName().toLowerCase().contains(query.toLowerCase())) ||
                        (item.getCategory() != null && item.getCategory().toLowerCase().contains(query.toLowerCase()))) {
                    filtered.add(item);
                }
            }
            MutableLiveData<List<PantryItem>> liveData = new MutableLiveData<>();
            liveData.setValue(filtered);
            return liveData;
        }

        @Override
        public List<PantryItem> getPantryItemsList() {
            return new ArrayList<>(items);
        }
    }

    public static class FakeRecipeDao implements RecipeDao {
        private final TestRecipeDaoHelper helper = new TestRecipeDaoHelper();

        @Override
        public long insertRecipe(Recipe recipe) {
            return helper.insertRecipe(recipe);
        }

        @Override
        public long insertIngredient(RecipeIngredient ingredient) {
            return helper.insertIngredient(ingredient);
        }

        @Override
        public void insertIngredients(List<RecipeIngredient> ingredientList) {
            helper.insertIngredients(ingredientList);
        }

        @Override
        public void deleteAllRecipes() {
            helper.deleteAllRecipes();
        }

        @Override
        public void deleteAllIngredients() {
            helper.deleteAllIngredients();
        }

        @Override
        public LiveData<List<RecipeWithIngredients>> getAllRecipesWithIngredients() {
            return helper.getAllRecipesWithIngredients();
        }

        @Override
        public List<RecipeWithIngredients> getRecipesWithIngredientsList() {
            return helper.getRecipesWithIngredientsList();
        }
    }

    private static class TestRecipeDaoHelper {
        private final List<Recipe> recipes = new ArrayList<>();
        private final List<RecipeIngredient> ingredients = new ArrayList<>();
        private int recipeIdCounter = 1;
        private int ingredientIdCounter = 1;

        public long insertRecipe(Recipe recipe) {
            if (recipe.getId() <= 0) {
                recipe.setId(recipeIdCounter++);
            } else {
                recipes.removeIf(r -> r.getId() == recipe.getId());
            }
            recipes.add(recipe);
            return recipe.getId();
        }

        public long insertIngredient(RecipeIngredient ingredient) {
            if (ingredient.getId() <= 0) {
                ingredient.setId(ingredientIdCounter++);
            } else {
                ingredients.removeIf(i -> i.getId() == ingredient.getId());
            }
            ingredients.add(ingredient);
            return ingredient.getId();
        }

        public void insertIngredients(List<RecipeIngredient> ingredientList) {
            if (ingredientList != null) {
                for (RecipeIngredient ing : ingredientList) {
                    insertIngredient(ing);
                }
            }
        }

        public void deleteAllRecipes() {
            recipes.clear();
        }

        public void deleteAllIngredients() {
            ingredients.clear();
        }

        public LiveData<List<RecipeWithIngredients>> getAllRecipesWithIngredients() {
            MutableLiveData<List<RecipeWithIngredients>> liveData = new MutableLiveData<>();
            liveData.setValue(getRecipesWithIngredientsList());
            return liveData;
        }

        public List<RecipeWithIngredients> getRecipesWithIngredientsList() {
            List<RecipeWithIngredients> result = new ArrayList<>();
            for (Recipe r : recipes) {
                List<RecipeIngredient> recipeIngredients = new ArrayList<>();
                for (RecipeIngredient ing : ingredients) {
                    if (ing.getRecipeId() == r.getId()) {
                        recipeIngredients.add(ing);
                    }
                }
                result.add(new RecipeWithIngredients(r, recipeIngredients));
            }
            return result;
        }
    }
}
