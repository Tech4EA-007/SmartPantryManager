package com.rgitapps.smartpantrymanager.ui.recipes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rgitapps.smartpantrymanager.R;
import com.rgitapps.smartpantrymanager.data.entity.PantryItem;
import com.rgitapps.smartpantrymanager.data.entity.RecipeWithIngredients;
import com.rgitapps.smartpantrymanager.data.repository.PantryRepository;
import com.rgitapps.smartpantrymanager.util.StrictRecipeMatcher;

import java.util.List;

public class RecipeDetailFragment extends Fragment {

    private TextView textTitle;
    private TextView textCategory;
    private TextView textInstructions;
    private RecyclerView rvIngredientsList;
    private Button btnCookRecipe;

    private RecipesViewModel viewModel;
    private PantryRepository repository;
    private RecipeIngredientAdapter ingredientAdapter;

    private int recipeId = -1;
    private RecipeWithIngredients currentRecipe;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_detail, container, false);

        textTitle = view.findViewById(R.id.textTitle);
        textCategory = view.findViewById(R.id.textCategory);
        textInstructions = view.findViewById(R.id.textInstructions);
        rvIngredientsList = view.findViewById(R.id.rvIngredientsList);
        btnCookRecipe = view.findViewById(R.id.btnCookRecipe);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            recipeId = getArguments().getInt("recipeId", -1);
        }

        viewModel = new ViewModelProvider(this).get(RecipesViewModel.class);
        repository = new PantryRepository(requireContext());

        setupRecyclerView();
        setupObservers();

        btnCookRecipe.setOnClickListener(v -> {
            if (currentRecipe == null) return;
            btnCookRecipe.setEnabled(false);
            repository.cookRecipe(currentRecipe, () -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Recipe cooked! Ingredients deducted from pantry", Toast.LENGTH_SHORT).show();
                }
                if (getView() != null) {
                    Navigation.findNavController(requireView()).popBackStack();
                }
            });
        });
    }

    private void setupRecyclerView() {
        ingredientAdapter = new RecipeIngredientAdapter();
        rvIngredientsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvIngredientsList.setAdapter(ingredientAdapter);
    }

    private void setupObservers() {
        viewModel.getFilteredAllRecipesLiveData().observe(getViewLifecycleOwner(), recipes ->
                updateRecipeDetails(recipes, viewModel.getAllPantryItems().getValue()));

        viewModel.getAllPantryItems().observe(getViewLifecycleOwner(), pantryItems ->
                updateRecipeDetails(viewModel.getFilteredAllRecipesLiveData().getValue(), pantryItems));
    }

    private void updateRecipeDetails(List<RecipeWithIngredients> recipes, List<PantryItem> pantryItems) {
        if (recipes == null) return;

        for (RecipeWithIngredients r : recipes) {
            if (r != null && r.getRecipe() != null && r.getRecipe().getId() == recipeId) {
                currentRecipe = r;
                break;
            }
        }

        if (currentRecipe != null && currentRecipe.getRecipe() != null) {
            textTitle.setText(currentRecipe.getRecipe().getName());
            textCategory.setText(currentRecipe.getRecipe().getCategory() != null ? currentRecipe.getRecipe().getCategory() : "General");
            textInstructions.setText(currentRecipe.getRecipe().getInstructions() != null ? currentRecipe.getRecipe().getInstructions() : "No instructions available.");

            if (currentRecipe.getIngredients() != null) {
                ingredientAdapter.setIngredients(currentRecipe.getIngredients());
            }

            if (pantryItems != null) {
                ingredientAdapter.setPantryItems(pantryItems);
            }

            boolean isMatched = StrictRecipeMatcher.isStrictMatch(currentRecipe, pantryItems);
            btnCookRecipe.setEnabled(isMatched);
        }
    }
}
