package com.rgitapps.smartpantrymanager.ui.recipes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.rgitapps.smartpantrymanager.data.entity.PantryItem;
import com.rgitapps.smartpantrymanager.data.entity.RecipeIngredient;
import com.rgitapps.smartpantrymanager.data.entity.RecipeWithIngredients;
import com.rgitapps.smartpantrymanager.data.repository.PantryRepository;
import com.rgitapps.smartpantrymanager.util.StrictRecipeMatcher;

import java.util.ArrayList;
import java.util.List;

public class RecipesViewModel extends AndroidViewModel {

    private final LiveData<List<PantryItem>> allPantryItems;
    private final LiveData<List<RecipeWithIngredients>> allRecipes;

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> categoryFilter = new MutableLiveData<>("All");

    private final MediatorLiveData<List<RecipeWithIngredients>> suggestedRecipesLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<List<RecipeWithIngredients>> almostThereRecipesLiveData = new MediatorLiveData<>();
    private final MediatorLiveData<List<RecipeWithIngredients>> filteredAllRecipesLiveData = new MediatorLiveData<>();

    public RecipesViewModel(@NonNull Application application) {
        super(application);
        PantryRepository repository = new PantryRepository(application);

        allPantryItems = repository.getAllPantryItems();
        allRecipes = repository.getAllRecipesWithIngredients();

        suggestedRecipesLiveData.addSource(allRecipes, recipes -> updateSuggestedRecipes());
        suggestedRecipesLiveData.addSource(allPantryItems, pantry -> updateSuggestedRecipes());
        suggestedRecipesLiveData.addSource(searchQuery, query -> updateSuggestedRecipes());
        suggestedRecipesLiveData.addSource(categoryFilter, category -> updateSuggestedRecipes());

        almostThereRecipesLiveData.addSource(allRecipes, recipes -> updateSuggestedRecipes());
        almostThereRecipesLiveData.addSource(allPantryItems, pantry -> updateSuggestedRecipes());
        almostThereRecipesLiveData.addSource(searchQuery, query -> updateSuggestedRecipes());
        almostThereRecipesLiveData.addSource(categoryFilter, category -> updateSuggestedRecipes());

        filteredAllRecipesLiveData.addSource(allRecipes, recipes -> updateAllRecipes());
        filteredAllRecipesLiveData.addSource(searchQuery, query -> updateAllRecipes());
        filteredAllRecipesLiveData.addSource(categoryFilter, category -> updateAllRecipes());
    }

    private void updateSuggestedRecipes() {
        List<RecipeWithIngredients> rawRecipes = allRecipes.getValue();
        List<PantryItem> pantry = allPantryItems.getValue();

        if (rawRecipes == null || rawRecipes.isEmpty()) {
            suggestedRecipesLiveData.setValue(new ArrayList<>());
            almostThereRecipesLiveData.setValue(new ArrayList<>());
            return;
        }

        String query = searchQuery.getValue() != null ? searchQuery.getValue().trim().toLowerCase() : "";
        String category = categoryFilter.getValue() != null ? categoryFilter.getValue().trim() : "All";

        List<RecipeWithIngredients> strictResult = new ArrayList<>();
        List<RecipeWithIngredients> almostThereResult = new ArrayList<>();

        for (RecipeWithIngredients recipeWithIngredients : rawRecipes) {
            if (recipeWithIngredients == null || recipeWithIngredients.getRecipe() == null) continue;

            if (!matchesCategory(recipeWithIngredients, category)) {
                continue;
            }

            if (!matchesQuery(recipeWithIngredients, query)) {
                continue;
            }

            StrictRecipeMatcher.MatchResult matchResult = StrictRecipeMatcher.analyzeRecipeMatch(recipeWithIngredients, pantry);

            if (matchResult.isMatched()) {
                strictResult.add(recipeWithIngredients);
            } else if (matchResult.getTotalIngredientsCount() > 0 && matchResult.getMissingIngredients().size() == 1) {
                almostThereResult.add(recipeWithIngredients);
            }
        }

        suggestedRecipesLiveData.setValue(strictResult);
        almostThereRecipesLiveData.setValue(almostThereResult);
    }

    private void updateAllRecipes() {
        List<RecipeWithIngredients> rawRecipes = allRecipes.getValue();
        if (rawRecipes == null || rawRecipes.isEmpty()) {
            filteredAllRecipesLiveData.setValue(new ArrayList<>());
            return;
        }

        String query = searchQuery.getValue() != null ? searchQuery.getValue().trim().toLowerCase() : "";
        String category = categoryFilter.getValue() != null ? categoryFilter.getValue().trim() : "All";

        List<RecipeWithIngredients> result = new ArrayList<>();
        for (RecipeWithIngredients recipeWithIngredients : rawRecipes) {
            if (recipeWithIngredients == null || recipeWithIngredients.getRecipe() == null) continue;

            if (!matchesCategory(recipeWithIngredients, category)) {
                continue;
            }

            if (!matchesQuery(recipeWithIngredients, query)) {
                continue;
            }

            result.add(recipeWithIngredients);
        }

        filteredAllRecipesLiveData.setValue(result);
    }

    private boolean matchesCategory(RecipeWithIngredients recipeWithIngredients, String category) {
        if ("All".equalsIgnoreCase(category) || category.isEmpty()) {
            return true;
        }
        String recipeCat = recipeWithIngredients.getRecipe().getCategory();
        return recipeCat != null && recipeCat.equalsIgnoreCase(category);
    }

    private boolean matchesQuery(RecipeWithIngredients recipeWithIngredients, String query) {
        if (query.isEmpty()) {
            return true;
        }
        String name = recipeWithIngredients.getRecipe().getName();
        if (name != null && name.toLowerCase().contains(query)) {
            return true;
        }
        String category = recipeWithIngredients.getRecipe().getCategory();
        if (category != null && category.toLowerCase().contains(query)) {
            return true;
        }
        if (recipeWithIngredients.getIngredients() != null) {
            for (RecipeIngredient ing : recipeWithIngredients.getIngredients()) {
                if (ing != null && ing.getIngredientName() != null && ing.getIngredientName().toLowerCase().contains(query)) {
                    return true;
                }
            }
        }
        return false;
    }

    public LiveData<List<PantryItem>> getAllPantryItems() {
        return allPantryItems;
    }

    public LiveData<List<RecipeWithIngredients>> getSuggestedRecipesLiveData() {
        return suggestedRecipesLiveData;
    }

    public LiveData<List<RecipeWithIngredients>> getAlmostThereRecipesLiveData() {
        return almostThereRecipesLiveData;
    }

    public LiveData<List<RecipeWithIngredients>> getFilteredAllRecipesLiveData() {
        return filteredAllRecipesLiveData;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void setCategoryFilter(String category) {
        categoryFilter.setValue(category);
    }
}
