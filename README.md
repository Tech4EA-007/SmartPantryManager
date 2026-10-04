# Smart Pantry Manager 🥗

**Smart Pantry Manager** is an intuitive, local-first native Android application designed to streamline household food management. It tracks pantry inventory and expiration dates in real time, reduces food waste, and dynamically suggests recipes based on ingredients you already have in stock.

---

## 🌟 Key Features

* **📦 Dynamic Pantry Inventory (CRUD)**: Create, Read, Update, and Delete pantry items with strict form validation (item name, positive quantity, category, and expiry date).
* **🎯 Strict 100% Recipe Suggestions**: Implements a strict matching engine where a recipe appears under **"Ready to Cook"** only if 100% of required ingredients are available in sufficient quantities.
* **💡 "Almost There" Recommendations**: Displays a separate, clearly marked section for recipes missing **exactly 1 ingredient**, encouraging users to unlock new recipes by adding one item.
* **🔤 Smart Pluralism & Unit Conversions**:
  * **Pluralism**: Automatically matches singular and plural forms (e.g. `"tomato"` vs `"tomatoes"`, `"egg"` vs `"eggs"`).
  * **Unit Conversions**: Supports cross-unit math for discrete items (`1 loaf` of bread = `20 slices`, `1 cup` of cheese = `16 slices`) as well as weight (`g` <-> `kg`) and volume (`ml` <-> `l` <-> `cups` <-> `tbsp` <-> `tsp`).
* **🍳 Automated Cooking Deduction**: Selecting "Cook Recipe" automatically deducts required ingredient quantities from your pantry database.
* **📱 Responsive Mobile UI**: Material 3 design layouts with Bottom Navigation, search bars, category filter chips, badge indicators, and clean empty states.

---

## 🗄️ Database Choice & Rationale

### Selected Database: **Room Persistence Library (SQLite)**

### Rationale:
1. **Local-First & Offline Availability**: A pantry manager is a daily utility app that must function instantly without requiring internet access, cloud sign-in, or server latency.
2. **Compile-Time SQL Verification**: Room validates all SQL queries during compilation, preventing runtime crashes caused by syntax errors or incorrect column references.
3. **Reactive LiveData Integration**: Room natively returns `LiveData` streams. When pantry inventory changes, ViewModel observers automatically update the UI and recalculate recipe matches in real time.
4. **Type-Safe Data Abstraction**: Room eliminates boilerplate SQLite connection management and manual `Cursor` parsing using annotated Java models (`@Entity`, `@Dao`, `@Database`, `@Relation`).

---

## 🏗️ Architecture Overview

The app follows the official **Android Recommended Architecture**:

```
[ UI Layer ]       PantryFragment / SuggestedRecipesFragment / RecipeDetailFragment
                           │  ▲  (LiveData Observers)
                           ▼  │
[ ViewModel Layer ]  PantryViewModel / RecipesViewModel (MediatorLiveData)
                           │  ▲
                           ▼  │
[ Repository Layer ] PantryRepository (Background Executors)
                           │  ▲
                           ▼  │
[ Database Layer ]   Room DB (AppDatabase ➔ PantryDao / RecipeDao ➔ SQLite)
```

---

## 🚀 Setup & Run Instructions

### Prerequisites
* **Android Studio**: Android Studio Ladybug (2024.2.1+) or newer.
* **JDK Version**: Java 17 (configured automatically in Android Studio).
* **Android SDK**: SDK 34 / 37 installed.
* **Device / Emulator**: Android 7.0 (API Level 24) or higher.

### Step-by-Step Installation

1. **Clone or Open the Project**:
   Open Android Studio, select **File ➔ Open**, and navigate to the project directory:
   `C:/Users/Eric/Downloads/EA SPM/SmartPantryManager`

2. **Gradle Sync**:
   * Android Studio will automatically prompt to sync Gradle.
   * If not, click **File ➔ Sync Project with Gradle Files**.

3. **Build the Project**:
   * Select **Build ➔ Make Project** (or press `Ctrl + F9` / `Cmd + F9`).

4. **Run the App**:
   * Select an active Android Emulator or connected physical device from the top toolbar.
   * Click the green **Run** button ▶️ (or press `Shift + F10`).

---

## 🧪 Running Unit Tests

To run the unit test suite covering CRUD operations and the `StrictRecipeMatcher` engine:

* **In Android Studio**:
  Right-click `app/src/test/java` ➔ Select **Run 'Tests in 'smartpantrymanager''**.
* **Via Gradle Command Line**:
  ```bash
  ./gradlew testDebugUnitTest
  ```

---

## 📄 License & Credits

Developed by **RGIT Apps Team** for Smart Pantry Management on Android.
