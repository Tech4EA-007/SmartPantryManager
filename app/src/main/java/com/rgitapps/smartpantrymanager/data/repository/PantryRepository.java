package com.rgitapps.smartpantrymanager.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.rgitapps.smartpantrymanager.data.dao.PantryDao;
import com.rgitapps.smartpantrymanager.data.dao.RecipeDao;
import com.rgitapps.smartpantrymanager.data.db.AppDatabase;
import com.rgitapps.smartpantrymanager.data.entity.PantryItem;
import com.rgitapps.smartpantrymanager.data.entity.Recipe;
import com.rgitapps.smartpantrymanager.data.entity.RecipeIngredient;
import com.rgitapps.smartpantrymanager.data.entity.RecipeWithIngredients;
import com.rgitapps.smartpantrymanager.util.StrictRecipeMatcher;

import java.util.List;
import java.util.concurrent.Executor;

public class PantryRepository {

    private final PantryDao pantryDao;
    private final RecipeDao recipeDao;
    private final Executor executor;

    public PantryRepository(@NonNull Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.pantryDao = database.pantryDao();
        this.recipeDao = database.recipeDao();
        this.executor = AppDatabase.databaseWriteExecutor;
    }

    public PantryRepository(PantryDao pantryDao, RecipeDao recipeDao, Executor executor) {
        this.pantryDao = pantryDao;
        this.recipeDao = recipeDao;
        this.executor = executor;
    }

    public LiveData<List<PantryItem>> getAllPantryItems() {
        return pantryDao.getAllPantryItems();
    }

    public LiveData<List<RecipeWithIngredients>> getAllRecipesWithIngredients() {
        return recipeDao.getAllRecipesWithIngredients();
    }

    public List<PantryItem> getPantryItemsList() {
        return pantryDao.getPantryItemsList();
    }

    public List<RecipeWithIngredients> getRecipesWithIngredientsList() {
        return recipeDao.getRecipesWithIngredientsList();
    }

    public void insertPantryItem(PantryItem item) {
        executor.execute(() -> pantryDao.insert(item));
    }

    public void updatePantryItem(PantryItem item) {
        executor.execute(() -> pantryDao.update(item));
    }

    public void deletePantryItem(PantryItem item) {
        executor.execute(() -> pantryDao.delete(item));
    }

    public void insertRecipe(Recipe recipe, List<RecipeIngredient> ingredients) {
        executor.execute(() -> {
            long recipeId = recipeDao.insertRecipe(recipe);
            if (ingredients != null) {
                for (RecipeIngredient ingredient : ingredients) {
                    ingredient.setRecipeId((int) recipeId);
                    recipeDao.insertIngredient(ingredient);
                }
            }
        });
    }

    public interface OnCookCompleteListener {
        void onCookComplete();
    }

    public void cookRecipe(RecipeWithIngredients recipeWithIngredients, OnCookCompleteListener listener) {
        executor.execute(() -> {
            if (recipeWithIngredients == null || recipeWithIngredients.getIngredients() == null) {
                if (listener != null) {
                    listener.onCookComplete();
                }
                return;
            }

            List<RecipeIngredient> ingredients = recipeWithIngredients.getIngredients();
            List<PantryItem> pantryItems = pantryDao.getPantryItemsList();

            for (RecipeIngredient req : ingredients) {
                if (req == null || req.getIngredientName() == null) continue;

                String reqNameNorm = StrictRecipeMatcher.normalizeName(req.getIngredientName());
                String reqUnitNorm = StrictRecipeMatcher.normalizeUnit(req.getUnit());
                double remainingNeeded = req.getRequiredQuantity();

                for (PantryItem pantryItem : pantryItems) {
                    if (pantryItem == null || pantryItem.getName() == null) continue;
                    String pantryNameNorm = StrictRecipeMatcher.normalizeName(pantryItem.getName());

                    if (StrictRecipeMatcher.namesMatch(reqNameNorm, pantryNameNorm)) {
                        String pantryUnitNorm = StrictRecipeMatcher.normalizeUnit(pantryItem.getUnit());
                        double pantryQtyInReqUnit = StrictRecipeMatcher.convertQuantity(pantryItem.getQuantity(), pantryUnitNorm, reqUnitNorm);

                        if (pantryQtyInReqUnit >= remainingNeeded - 0.0001) {
                            double leftInReqUnit = pantryQtyInReqUnit - remainingNeeded;
                            double leftInPantryUnit = StrictRecipeMatcher.convertQuantity(leftInReqUnit, reqUnitNorm, pantryUnitNorm);

                            if (leftInPantryUnit <= 0.0001) {
                                pantryDao.delete(pantryItem);
                                pantryItem.setQuantity(0.0);
                            } else {
                                pantryItem.setQuantity(leftInPantryUnit);
                                pantryDao.update(pantryItem);
                            }
                            break;
                        } else {
                            remainingNeeded -= pantryQtyInReqUnit;
                            pantryDao.delete(pantryItem);
                            pantryItem.setQuantity(0.0);
                        }
                    }
                }
            }

            if (listener != null) {
                listener.onCookComplete();
            }
        });
    }

    public void resetDatabase(Runnable onResetComplete) {
        executor.execute(() -> {
            pantryDao.deleteAll();
            recipeDao.deleteAllIngredients();
            recipeDao.deleteAllRecipes();
            AppDatabase.seedAllData(pantryDao, recipeDao);
            if (onResetComplete != null) {
                onResetComplete.run();
            }
        });
    }
}
