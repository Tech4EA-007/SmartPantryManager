package com.rgitapps.smartpantrymanager.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.rgitapps.smartpantrymanager.data.entity.Recipe;
import com.rgitapps.smartpantrymanager.data.entity.RecipeIngredient;
import com.rgitapps.smartpantrymanager.data.entity.RecipeWithIngredients;

import java.util.List;

@Dao
public interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRecipe(Recipe recipe);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertIngredient(RecipeIngredient ingredient);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIngredients(List<RecipeIngredient> ingredients);

    @Query("DELETE FROM recipes")
    void deleteAllRecipes();

    @Query("DELETE FROM recipe_ingredients")
    void deleteAllIngredients();

    @Transaction
    @Query("SELECT * FROM recipes ORDER BY name ASC")
    LiveData<List<RecipeWithIngredients>> getAllRecipesWithIngredients();

    @Transaction
    @Query("SELECT * FROM recipes ORDER BY name ASC")
    List<RecipeWithIngredients> getRecipesWithIngredientsList();
}
