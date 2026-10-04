package com.rgitapps.smartpantrymanager.data.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.rgitapps.smartpantrymanager.data.dao.PantryDao;
import com.rgitapps.smartpantrymanager.data.dao.RecipeDao;
import com.rgitapps.smartpantrymanager.data.entity.PantryItem;
import com.rgitapps.smartpantrymanager.data.entity.Recipe;
import com.rgitapps.smartpantrymanager.data.entity.RecipeIngredient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {PantryItem.class, Recipe.class, RecipeIngredient.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract PantryDao pantryDao();
    public abstract RecipeDao recipeDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "smart_pantry_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final Callback sRoomDatabaseCallback = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            databaseWriteExecutor.execute(() -> seedAllData(INSTANCE.pantryDao(), INSTANCE.recipeDao()));
        }
    };

    public static void seedAllData(PantryDao pantryDao, RecipeDao recipeDao) {
        // Pre-seed comprehensive initial sample Pantry items covering all recipe ingredients
        pantryDao.insert(new PantryItem("Eggs", 24, "pcs", "Dairy", "2026-04-15"));
        pantryDao.insert(new PantryItem("Milk", 4, "cups", "Dairy", "2026-04-10"));
        pantryDao.insert(new PantryItem("Butter", 4, "tbsp", "Dairy", "2026-05-01"));
        pantryDao.insert(new PantryItem("Bread", 1, "loaf", "Bakery", "2026-04-05"));
        pantryDao.insert(new PantryItem("Cheese", 16, "slices", "Dairy", "2026-04-20"));
        pantryDao.insert(new PantryItem("Salt", 10, "tsp", "Pantry", "2026-12-31"));
        pantryDao.insert(new PantryItem("Pepper", 10, "pinch", "Pantry", "2026-12-31"));
        pantryDao.insert(new PantryItem("Pasta", 500, "g", "Pantry", "2026-12-31"));
        pantryDao.insert(new PantryItem("Tomato Sauce", 2, "cups", "Pantry", "2026-12-31"));
        pantryDao.insert(new PantryItem("Garlic", 10, "cloves", "Produce", "2026-05-01"));
        pantryDao.insert(new PantryItem("Olive Oil", 5, "tbsp", "Pantry", "2026-12-31"));
        pantryDao.insert(new PantryItem("Lettuce", 4, "cups", "Produce", "2026-05-01"));
        pantryDao.insert(new PantryItem("Tomato", 4, "pcs", "Produce", "2026-05-01"));
        pantryDao.insert(new PantryItem("Cucumber", 2, "pcs", "Produce", "2026-05-01"));
        pantryDao.insert(new PantryItem("Lemon", 2, "pcs", "Produce", "2026-05-01"));
        pantryDao.insert(new PantryItem("Chicken Breast", 500, "g", "Meat", "2026-05-01"));
        pantryDao.insert(new PantryItem("Soy Sauce", 5, "tbsp", "Pantry", "2026-12-31"));
        pantryDao.insert(new PantryItem("Rice", 3, "cups", "Pantry", "2026-12-31"));
        pantryDao.insert(new PantryItem("Flour", 2, "cups", "Pantry", "2026-12-31"));
        pantryDao.insert(new PantryItem("Sugar", 5, "tbsp", "Pantry", "2026-12-31"));
        pantryDao.insert(new PantryItem("Tortilla", 6, "pcs", "Bakery", "2026-05-01"));
        pantryDao.insert(new PantryItem("Carrot", 4, "pcs", "Produce", "2026-05-01"));
        pantryDao.insert(new PantryItem("Potato", 6, "pcs", "Produce", "2026-05-01"));
        pantryDao.insert(new PantryItem("Onion", 3, "pcs", "Produce", "2026-05-01"));
        pantryDao.insert(new PantryItem("Broth", 6, "cups", "Pantry", "2026-12-31"));
        pantryDao.insert(new PantryItem("Avocado", 3, "pcs", "Produce", "2026-05-01"));
        pantryDao.insert(new PantryItem("Lime", 2, "pcs", "Produce", "2026-05-01"));
        pantryDao.insert(new PantryItem("Tortilla Chips", 200, "g", "Snacks", "2026-12-31"));
        pantryDao.insert(new PantryItem("Shrimp", 400, "g", "Seafood", "2026-05-01"));
        pantryDao.insert(new PantryItem("Peanut Butter", 4, "tbsp", "Pantry", "2026-12-31"));
        pantryDao.insert(new PantryItem("Jelly", 4, "tbsp", "Pantry", "2026-12-31"));
        pantryDao.insert(new PantryItem("Banana", 3, "pcs", "Produce", "2026-05-01"));
        pantryDao.insert(new PantryItem("Yogurt", 2, "cups", "Dairy", "2026-05-01"));
        pantryDao.insert(new PantryItem("Honey", 4, "tbsp", "Pantry", "2026-12-31"));
        pantryDao.insert(new PantryItem("Ground Beef", 500, "g", "Meat", "2026-05-01"));
        pantryDao.insert(new PantryItem("Taco Shells", 8, "pcs", "Pantry", "2026-12-31"));
        pantryDao.insert(new PantryItem("Cinnamon", 2, "tsp", "Pantry", "2026-12-31"));
        pantryDao.insert(new PantryItem("Peas", 1, "cup", "Pantry", "2026-12-31"));
        pantryDao.insert(new PantryItem("Mozzarella", 200, "g", "Dairy", "2026-05-01"));
        pantryDao.insert(new PantryItem("Basil", 20, "leaves", "Produce", "2026-05-01"));
        pantryDao.insert(new PantryItem("Canned Tuna", 2, "can", "Pantry", "2026-12-31"));
        pantryDao.insert(new PantryItem("Mayonnaise", 4, "tbsp", "Pantry", "2026-12-31"));
        pantryDao.insert(new PantryItem("Celery", 3, "stalk", "Produce", "2026-05-01"));
        pantryDao.insert(new PantryItem("Oats", 3, "cups", "Pantry", "2026-12-31"));

        // Pre-seed 20 diverse recipes with ingredients
        seedRecipe(recipeDao, "Classic Omelet", "Breakfast",
                "Whisk eggs and milk. Melt butter in pan. Pour egg mixture and cook until set. Season with salt and pepper.",
                new RecipeIngredient(0, "Eggs", 2, "pcs"),
                new RecipeIngredient(0, "Milk", 2, "tbsp"),
                new RecipeIngredient(0, "Butter", 1, "tbsp"),
                new RecipeIngredient(0, "Salt", 1, "pinch"),
                new RecipeIngredient(0, "Pepper", 1, "pinch"));

        seedRecipe(recipeDao, "Tomato Pasta", "Dinner",
                "Boil pasta in salted water. Sauté garlic in olive oil, add tomato sauce, then mix with cooked pasta.",
                new RecipeIngredient(0, "Pasta", 200, "g"),
                new RecipeIngredient(0, "Tomato Sauce", 1, "cup"),
                new RecipeIngredient(0, "Garlic", 2, "cloves"),
                new RecipeIngredient(0, "Olive Oil", 1, "tbsp"),
                new RecipeIngredient(0, "Salt", 1, "tsp"));

        seedRecipe(recipeDao, "Fresh Garden Salad", "Vegetarian",
                "Chop lettuce, tomato, and cucumber. Toss together with olive oil and fresh lemon juice.",
                new RecipeIngredient(0, "Lettuce", 2, "cups"),
                new RecipeIngredient(0, "Tomato", 2, "pcs"),
                new RecipeIngredient(0, "Cucumber", 1, "pc"),
                new RecipeIngredient(0, "Olive Oil", 2, "tbsp"),
                new RecipeIngredient(0, "Lemon", 1, "pc"));

        seedRecipe(recipeDao, "Chicken Stir-Fry", "Dinner",
                "Cook rice. Sauté chicken breast pieces with garlic and soy sauce until fully cooked. Serve over rice.",
                new RecipeIngredient(0, "Chicken Breast", 300, "g"),
                new RecipeIngredient(0, "Soy Sauce", 2, "tbsp"),
                new RecipeIngredient(0, "Garlic", 2, "cloves"),
                new RecipeIngredient(0, "Rice", 1, "cup"));

        seedRecipe(recipeDao, "Fluffy Pancakes", "Breakfast",
                "Mix flour, milk, egg, and sugar. Melt butter on griddle and pour batter to make fluffy pancakes.",
                new RecipeIngredient(0, "Flour", 1, "cup"),
                new RecipeIngredient(0, "Milk", 1, "cup"),
                new RecipeIngredient(0, "Eggs", 1, "pc"),
                new RecipeIngredient(0, "Sugar", 2, "tbsp"),
                new RecipeIngredient(0, "Butter", 2, "tbsp"));

        seedRecipe(recipeDao, "Cheesy Quesadilla", "Snacks",
                "Place cheese between two tortillas. Toast in a skillet with butter until cheese melts and tortilla is golden.",
                new RecipeIngredient(0, "Tortilla", 2, "pcs"),
                new RecipeIngredient(0, "Cheese", 1, "cup"),
                new RecipeIngredient(0, "Butter", 1, "tbsp"));

        seedRecipe(recipeDao, "Grilled Cheese Sandwich", "Snacks",
                "Butter outside of bread slices. Place cheese inside and grill on medium heat until golden brown.",
                new RecipeIngredient(0, "Bread", 2, "slices"),
                new RecipeIngredient(0, "Butter", 1, "tbsp"),
                new RecipeIngredient(0, "Cheese", 2, "slices"));

        seedRecipe(recipeDao, "Vegetable Soup", "Vegetarian",
                "Dice carrot, potato, and onion. Simmer in broth for 25 minutes until tender.",
                new RecipeIngredient(0, "Carrot", 2, "pcs"),
                new RecipeIngredient(0, "Potato", 2, "pcs"),
                new RecipeIngredient(0, "Onion", 1, "pc"),
                new RecipeIngredient(0, "Broth", 4, "cups"));

        seedRecipe(recipeDao, "Guacamole & Chips", "Snacks",
                "Mash avocados with lime juice, diced onion, and salt. Serve with crisp tortilla chips.",
                new RecipeIngredient(0, "Avocado", 2, "pcs"),
                new RecipeIngredient(0, "Lime", 1, "pc"),
                new RecipeIngredient(0, "Onion", 0.5, "pc"),
                new RecipeIngredient(0, "Salt", 0.5, "tsp"),
                new RecipeIngredient(0, "Tortilla Chips", 150, "g"));

        seedRecipe(recipeDao, "Garlic Butter Shrimp", "Dinner",
                "Sauté minced garlic in butter. Add shrimp and cook 3-4 mins until pink. Squeeze lemon on top.",
                new RecipeIngredient(0, "Shrimp", 300, "g"),
                new RecipeIngredient(0, "Garlic", 3, "cloves"),
                new RecipeIngredient(0, "Butter", 2, "tbsp"),
                new RecipeIngredient(0, "Lemon", 1, "pc"));

        seedRecipe(recipeDao, "PB&J Sandwich", "Snacks",
                "Spread peanut butter on one slice of bread and jelly on the other. Press together and serve.",
                new RecipeIngredient(0, "Bread", 2, "slices"),
                new RecipeIngredient(0, "Peanut Butter", 2, "tbsp"),
                new RecipeIngredient(0, "Jelly", 2, "tbsp"));

        seedRecipe(recipeDao, "Scrambled Eggs", "Breakfast",
                "Whisk eggs with salt. Melt butter in pan over medium-low heat. Stir gently until soft curds form.",
                new RecipeIngredient(0, "Eggs", 3, "pcs"),
                new RecipeIngredient(0, "Butter", 1, "tbsp"),
                new RecipeIngredient(0, "Salt", 0.5, "tsp"));

        seedRecipe(recipeDao, "Fruit Smoothie", "Breakfast",
                "Blend banana, milk, yogurt, and honey until smooth and creamy.",
                new RecipeIngredient(0, "Banana", 1, "pc"),
                new RecipeIngredient(0, "Milk", 1, "cup"),
                new RecipeIngredient(0, "Yogurt", 0.5, "cup"),
                new RecipeIngredient(0, "Honey", 1, "tbsp"));

        seedRecipe(recipeDao, "Beef Tacos", "Dinner",
                "Brown ground beef with taco seasoning. Fill taco shells with beef, diced tomato, and shredded cheese.",
                new RecipeIngredient(0, "Ground Beef", 300, "g"),
                new RecipeIngredient(0, "Taco Shells", 4, "pcs"),
                new RecipeIngredient(0, "Cheese", 0.5, "cup"),
                new RecipeIngredient(0, "Tomato", 1, "pc"));

        seedRecipe(recipeDao, "French Toast", "Breakfast",
                "Whisk eggs, milk, and cinnamon. Dip bread slices and fry in buttered pan until golden.",
                new RecipeIngredient(0, "Bread", 4, "slices"),
                new RecipeIngredient(0, "Eggs", 2, "pcs"),
                new RecipeIngredient(0, "Milk", 0.5, "cup"),
                new RecipeIngredient(0, "Cinnamon", 0.5, "tsp"),
                new RecipeIngredient(0, "Butter", 1, "tbsp"));

        seedRecipe(recipeDao, "Fried Rice", "Dinner",
                "Stir fry cold cooked rice with beaten eggs, soy sauce, and peas over high heat until hot and fragrant.",
                new RecipeIngredient(0, "Rice", 2, "cups"),
                new RecipeIngredient(0, "Eggs", 2, "pcs"),
                new RecipeIngredient(0, "Soy Sauce", 2, "tbsp"),
                new RecipeIngredient(0, "Peas", 0.5, "cup"));

        seedRecipe(recipeDao, "Caresse Salad", "Vegetarian",
                "Slice tomatoes and mozzarella. Layer with fresh basil leaves and drizzle generously with olive oil.",
                new RecipeIngredient(0, "Tomato", 2, "pcs"),
                new RecipeIngredient(0, "Mozzarella", 150, "g"),
                new RecipeIngredient(0, "Basil", 10, "leaves"),
                new RecipeIngredient(0, "Olive Oil", 2, "tbsp"));

        seedRecipe(recipeDao, "Tuna Salad", "Snacks",
                "Drain tuna, mix with diced celery and mayonnaise. Season to taste and serve chilled.",
                new RecipeIngredient(0, "Canned Tuna", 1, "can"),
                new RecipeIngredient(0, "Mayonnaise", 2, "tbsp"),
                new RecipeIngredient(0, "Celery", 1, "stalk"));

        seedRecipe(recipeDao, "Oatmeal with Honey", "Breakfast",
                "Simmer oats in milk for 5-7 minutes. Stir in honey before serving warm.",
                new RecipeIngredient(0, "Oats", 1, "cup"),
                new RecipeIngredient(0, "Milk", 2, "cups"),
                new RecipeIngredient(0, "Honey", 2, "tbsp"));

        seedRecipe(recipeDao, "Mashed Potatoes", "Vegetarian",
                "Boil potatoes until soft, mash with melted butcher, warm milk, and salt until smooth.",
                new RecipeIngredient(0, "Potato", 4, "pcs"),
                new RecipeIngredient(0, "Butter", 3, "tbsp"),
                new RecipeIngredient(0, "Milk", 0.25, "cup"),
                new RecipeIngredient(0, "Salt", 1, "tsp"));
    }

    private static void seedRecipe(RecipeDao recipeDao, String name, String category, String instructions, RecipeIngredient... ingredients) {
        Recipe recipe = new Recipe(name, category, instructions);
        long recipeId = recipeDao.insertRecipe(recipe);
        for (RecipeIngredient ingredient : ingredients) {
            ingredient.setRecipeId((int) recipeId);
            recipeDao.insertIngredient(ingredient);
        }
    }
}
