# Project Plan

Smart Pantry Manager app written in Java featuring full CRUD pantry management, pre-loaded recipe database, strict matching logic, recipe details with quick-cook deduction, and settings.

## Project Brief

# Smart Pantry Manager - Project Brief

## Features
- **Pantry Inventory Management (CRUD)**: Easily add, view, edit, and delete pantry items, tracking quantities, measurement units, and expiration dates.
- **Recipe Database & Strict Matching**: Browse a pre-loaded library of recipes dynamically filtered by strict matching with ingredients currently available in the pantry.
- **Recipe Details & Quick-Cook Deduction**: Inspect detailed recipe instructions and automatically deduct the required ingredient quantities from the pantry upon cooking.
- **Settings & Preferences**: Configure app settings, manage default units, and reset pantry or recipe data as needed.

## High-Level Technical Stack
- **Language**: Java 17
- **UI Layer**: Android Material Components, XML Layouts, and ViewBinding
- **Navigation**: Jetpack Navigation Graph (`NavHostFragment`)
- **Architecture**: MVVM using Jetpack `ViewModel` and `LiveData`
- **Persistence**: Room Database (Java Entities and DAOs)

## Implementation Steps
**Total Duration:** 61h 3m 23s

### Task_1_DataLayerAndPersistence: Set up Room database schema (PantryItem, Recipe, RecipeIngredient entities), DAOs, repository, and pre-seed database with 15-20 diverse recipes on first launch.
- **Status:** COMPLETED
- **Updates:** Room entities (PantryItem, Recipe, RecipeIngredient, RecipeWithIngredients), DAOs, AppDatabase with pre-seeded 20 recipes callback, and PantryRepository created in Java 17. Gradle build app:assembleDebug verified.
- **Acceptance Criteria:**
  - Room entities, DAOs, and repository created
  - Pre-populated database with 15-20 recipes on first launch
  - build pass
- **Duration:** 22m 1s

### Task_2_PantryManagementUI: Implement Pantry Inventory screen with full CRUD (Add/Edit/Delete ingredients), search functionality, category filtering, and color-coded expiration warnings.
- **Status:** COMPLETED
- **Updates:** Implemented PantryViewModel, fragment_pantry.xml, item_pantry.xml, dialog_add_edit_pantry_item.xml, PantryAdapter with DiffUtil and expiry color coding (Red for expired/today, Orange for <=3 days, Green for normal), PantryFragment with Add/Edit/Delete dialogs, search, category chips, and input validation. gradle assembleDebug passed.
- **Acceptance Criteria:**
  - Pantry UI supports full CRUD operations for ingredients
  - Search bar and category filtering working
  - Expiration warnings displayed with color coding
  - build pass
- **Duration:** 9m 7s

### Task_3_RecipeMatchingAndBrowseScreens: Build strict recipe matching engine (100% required ingredients available) and create Suggested Recipes tab (with empty state) and All Recipes tab with missing ingredient indicators.
- **Status:** COMPLETED
- **Updates:** StrictRecipeMatcher algorithm implemented with normalized name/unit matching and strict 100% quantity verification. Created RecipesViewModel, SuggestedRecipesFragment (with empty state), AllRecipesFragment (with missing ingredient details), fragment_suggested_recipes.xml, fragment_all_recipes.xml, item_recipe.xml, and RecipeAdapter. Gradle build app:assembleDebug passed.
- **Acceptance Criteria:**
  - Strict matching engine filters recipes based on pantry stock
  - Suggested Recipes screen displays matching recipes or empty state
  - All Recipes screen highlights missing or insufficient ingredients
  - build pass
- **Duration:** 6m 30s

### Task_4_RecipeDetailCookDeductionAndSettings: Build Recipe Detail screen with 'Cook Recipe' button that deducts ingredient amounts from pantry (deleting empty items), and Settings screen for unit preferences and expiry alert toggles.
- **Status:** COMPLETED
- **Updates:** Implemented cookRecipe auto-deduction in PantryRepository, RecipeDetailFragment, fragment_recipe_detail.xml, RecipeIngredientAdapter, SettingsFragment, fragment_settings.xml with SharedPreferences and reset DB functionality. gradle assembleDebug passed.
- **Acceptance Criteria:**
  - Recipe Detail screen shows ingredients and step-by-step instructions
  - Cook Recipe button deducts pantry stock correctly
  - Settings screen saves unit preference and alert options
  - build pass
- **Duration:** 60h 25m 42s

### Task_5_NavigationAndIntegration: Set up Jetpack Navigation Graph and BottomNavigationView to seamlessly link Pantry, Suggested Recipes, All Recipes, and Settings screens, ensuring complete app flow integration.
- **Status:** COMPLETED
- **Updates:** Configured activity_main.xml with BottomNavigationView and NavHostFragment. Created nav_graph.xml connecting pantryFragment, suggestedRecipesFragment, allRecipesFragment, settingsFragment, and recipeDetailFragment. Set up NavigationUI in MainActivity. gradle assembleDebug passed.
- **Acceptance Criteria:**
  - Bottom navigation switches smoothly between all primary screens
  - make sure all existing tests pass
  - build pass
  - app does not crash
- **Duration:** 3s

### Task_6_RunAndVerify: Run and verify application stability, instruct critic_agent to verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - Application stability verified with no crashes
  - Alignment with user requirements confirmed
  - Critical UI issues reported
  - make sure all existing tests pass
  - build pass
  - app does not crash
- **StartTime:** 2026-10-03 04:33:28 SAST

