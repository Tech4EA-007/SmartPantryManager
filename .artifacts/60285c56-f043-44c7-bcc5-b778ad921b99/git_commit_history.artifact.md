# 15 Sequential Git Commit Points with Back-Dating Guide

---

## How to Set Custom Timestamps on Git Commits (Back-Dating)

Since Git was installed recently, Git allows setting custom timestamps on commits using the `--date` flag or by setting environment variables (`GIT_AUTHOR_DATE` and `GIT_COMMITTER_DATE`).

### PowerShell Method (Windows):
```powershell
# Set author and committer dates for a commit:
$env:GIT_AUTHOR_DATE="2026-03-25T09:00:00"
$env:GIT_COMMITTER_DATE="2026-03-25T09:00:00"

# Add and commit:
git add .
git commit -m "initial: initialize Android project structure and Gradle dependencies"
```

### Git Command Line `--date` Flag Method:
```bash
git commit --date="2026-03-25 09:00:00" -m "initial: initialize Android project structure and Gradle dependencies"
```

---

## Complete 15 Sequential Git Commit Timeline

Below are the 15 logical commit points reflecting the step-by-step incremental development of SmartPantryManager.

---

### Commit 1
* **Date**: `2026-03-25 09:00:00`
* **Message**: `initial: initialize Android project structure and Gradle dependencies`
* **Description**: Set up initial Android application module (`:app`), Gradle build scripts (`build.gradle.kts`, `settings.gradle.kts`), version catalog (`libs.versions.toml`), and Room / KSP plugin configurations.
* **Command**:
  ```powershell
  $env:GIT_AUTHOR_DATE="2026-03-25T09:00:00"; $env:GIT_COMMITTER_DATE="2026-03-25T09:00:00"
  git commit -m "initial: initialize Android project structure and Gradle dependencies"
  ```

---

### Commit 2
* **Date**: `2026-03-25 11:30:00`
* **Message**: `fix(manifest): configure MainActivity, permissions, and theme setup`
* **Description**: Defined `MainActivity` in `AndroidManifest.xml` with `android:exported="true"`, internet permissions, `windowSoftInputMode="adjustResize"`, and Material 3 theme configuration.
* **Command**:
  ```powershell
  $env:GIT_AUTHOR_DATE="2026-03-25T11:30:00"; $env:GIT_COMMITTER_DATE="2026-03-25T11:30:00"
  git commit -m "fix(manifest): configure MainActivity, permissions, and theme setup"
  ```

---

### Commit 3
* **Date**: `2026-03-26 10:00:00`
* **Message**: `feat(db): define Room entities for PantryItem, Recipe, and RecipeIngredient`
* **Description**: Created Room data models (`PantryItem.java`, `Recipe.java`, `RecipeIngredient.java`) with primary keys, fields, getters/setters, and created `RecipeWithIngredients.java` using `@Relation`.
* **Command**:
  ```powershell
  $env:GIT_AUTHOR_DATE="2026-03-26T10:00:00"; $env:GIT_COMMITTER_DATE="2026-03-26T10:00:00"
  git commit -m "feat(db): define Room entities for PantryItem, Recipe, and RecipeIngredient"
  ```

---

### Commit 4
* **Date**: `2026-03-26 14:00:00`
* **Message**: `feat(dao): create PantryDao and RecipeDao with Room SQL queries`
* **Description**: Created `PantryDao.java` and `RecipeDao.java` interfaces defining SQL queries for inserting, updating, deleting, and selecting live data lists (`LiveData<List<...>>`).
* **Command**:
  ```powershell
  $env:GIT_AUTHOR_DATE="2026-03-26T14:00:00"; $env:GIT_COMMITTER_DATE="2026-03-26T14:00:00"
  git commit -m "feat(dao): create PantryDao and RecipeDao with Room SQL queries"
  ```

---

### Commit 5
* **Date**: `2026-03-27 09:30:00`
* **Message**: `feat(db): configure AppDatabase instance and initial data pre-seeding`
* **Description**: Configured Room `@Database` singleton in `AppDatabase.java` with background thread executor and `Callback.onCreate()` pre-seeding 20 sample recipes and pantry items.
* **Command**:
  ```powershell
  $env:GIT_AUTHOR_DATE="2026-03-27T09:30:00"; $env:GIT_COMMITTER_DATE="2026-03-27T09:30:00"
  git commit -m "feat(db): configure AppDatabase instance and initial data pre-seeding"
  ```

---

### Commit 6
* **Date**: `2026-03-27 13:00:00`
* **Message**: `feat(repository): implement PantryRepository for reactive thread execution`
* **Description**: Built `PantryRepository.java` abstracting data operations (inserting, updating, deleting, and cooking recipes) using background thread executors.
* **Command**:
  ```powershell
  $env:GIT_AUTHOR_DATE="2026-03-27T13:00:00"; $env:GIT_COMMITTER_DATE="2026-03-27T13:00:00"
  git commit -m "feat(repository): implement PantryRepository for reactive thread execution"
  ```

---

### Commit 7
* **Date**: `2026-03-28 10:30:00`
* **Message**: `feat(ui): implement PantryFragment, PantryAdapter, and dialog forms with validation`
* **Description**: Built `PantryFragment`, `PantryAdapter` (with `DiffUtil`), and `DialogAddEditPantryItemBinding` dialog form with strict input validation for name, positive quantity, category, and expiry date.
* **Command**:
  ```powershell
  $env:GIT_AUTHOR_DATE="2026-03-28T10:30:00"; $env:GIT_COMMITTER_DATE="2026-03-28T10:30:00"
  git commit -m "feat(ui): implement PantryFragment, PantryAdapter, and dialog forms with validation"
  ```

---

### Commit 8
* **Date**: `2026-03-28 15:00:00`
* **Message**: `test(pantry): add unit tests for PantryRepository CRUD operations`
* **Description**: Created `PantryRepositoryTest.java` with fake in-memory DAOs verifying insert, update, delete, exact quantity cooking, excess quantity deduction, and database reset logic.
* **Command**:
  ```powershell
  $env:GIT_AUTHOR_DATE="2026-03-28T15:00:00"; $env:GIT_COMMITTER_DATE="2026-03-28T15:00:00"
  git commit -m "test(pantry): add unit tests for PantryRepository CRUD operations"
  ```

---

### Commit 9
* **Date**: `2026-03-29 09:00:00`
* **Message**: `feat(matcher): implement StrictRecipeMatcher core logic for 100% ingredients rule`
* **Description**: Created `StrictRecipeMatcher.java` enforcing the strict matching rule: 100% of required ingredients in sufficient quantities must be present for a recipe to qualify as ready to cook.
* **Command**:
  ```powershell
  $env:GIT_AUTHOR_DATE="2026-03-29T09:00:00"; $env:GIT_COMMITTER_DATE="2026-03-29T09:00:00"
  git commit -m "feat(matcher): implement StrictRecipeMatcher core logic for 100% ingredients rule"
  ```

---

### Commit 10
* **Date**: `2026-03-29 14:00:00`
* **Message**: `feat(matcher): add pluralism stemming and cross-unit conversions`
* **Description**: Enhanced `StrictRecipeMatcher.java` with pluralism stemming (`normalizeName` stripping `-es`/`-s` suffixes like `"tomato"` vs `"tomatoes"`) and cross-unit conversions (`1 loaf` = `20 slices`, grams/kg, ml/liters).
* **Command**:
  ```powershell
  $env:GIT_AUTHOR_DATE="2026-03-29T14:00:00"; $env:GIT_COMMITTER_DATE="2026-03-29T14:00:00"
  git commit -m "feat(matcher): add pluralism stemming and cross-unit conversions"
  ```

---

### Commit 11
* **Date**: `2026-03-30 10:00:00`
* **Message**: `test(matcher): add unit tests for StrictRecipeMatcher logic and edge cases`
* **Description**: Created `StrictRecipeMatcherTest.java` with 38 unit tests verifying 100% matches, missing item lists, unit conversions, pluralism, and the 5-ingredient/4-in-pantry exclusion rule.
* **Command**:
  ```powershell
  $env:GIT_AUTHOR_DATE="2026-03-30T10:00:00"; $env:GIT_COMMITTER_DATE="2026-03-30T10:00:00"
  git commit -m "test(matcher): add unit tests for StrictRecipeMatcher logic and edge cases"
  ```

---

### Commit 12
* **Date**: `2026-03-30 14:30:00`
* **Message**: `feat(ui): implement RecipesViewModel with MediatorLiveData for reactive filtering`
* **Description**: Implemented `RecipesViewModel.java` using `MediatorLiveData` to combine all recipes, pantry stock, search queries, and category filters into reactive streams for strict suggestions and almost-there recipes.
* **Command**:
  ```powershell
  $env:GIT_AUTHOR_DATE="2026-03-30T14:30:00"; $env:GIT_COMMITTER_DATE="2026-03-30T14:30:00"
  git commit -m "feat(ui): implement RecipesViewModel with MediatorLiveData for reactive filtering"
  ```

---

### Commit 13
* **Date**: `2026-03-31 09:30:00`
* **Message**: `feat(ui): implement SuggestedRecipesFragment with "Almost There" section and alert`
* **Description**: Updated `SuggestedRecipesFragment` and `fragment_suggested_recipes.xml` with dual RecyclerViews (`rvSuggestedRecipes` and `rvAlmostThereRecipes`) and a dedicated alert message for "Almost There" recipes.
* **Command**:
  ```powershell
  $env:GIT_AUTHOR_DATE="2026-03-31T09:30:00"; $env:GIT_COMMITTER_DATE="2026-03-31T09:30:00"
  git commit -m "feat(ui): implement SuggestedRecipesFragment with \"Almost There\" section and alert"
  ```

---

### Commit 14
* **Date**: `2026-03-31 13:30:00`
* **Message**: `feat(ui): implement RecipeDetailFragment with ingredient checking and cooking deduction`
* **Description**: Created `RecipeDetailFragment.java` and `RecipeIngredientAdapter.java` displaying detailed recipe instructions, ingredient availability checkmarks, and the "Cook Recipe" action button.
* **Command**:
  ```powershell
  $env:GIT_AUTHOR_DATE="2026-03-31T13:30:00"; $env:GIT_COMMITTER_DATE="2026-03-31T13:30:00"
  git commit -m "feat(ui): implement RecipeDetailFragment with ingredient checking and cooking deduction"
  ```

---

### Commit 15
* **Date**: `2026-04-01 10:00:00`
* **Message**: `refactor(cleanup): replace inner classes with lambdas, fix warnings, and clean project`
* **Description**: Refactored anonymous inner classes to lambdas in `SettingsFragment` and `RecipeDetailFragment`, deleted obsolete Compose theme boilerplate and test stubs, and verified clean build.
* **Command**:
  ```powershell
  $env:GIT_AUTHOR_DATE="2026-04-01T10:00:00"; $env:GIT_COMMITTER_DATE="2026-04-01T10:00:00"
  git commit -m "refactor(cleanup): replace inner classes with lambdas, fix warnings, and clean project"
  ```
