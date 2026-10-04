package com.rgitapps.smartpantrymanager.ui.recipes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.rgitapps.smartpantrymanager.R;
import com.rgitapps.smartpantrymanager.data.entity.RecipeWithIngredients;
import com.rgitapps.smartpantrymanager.databinding.FragmentAllRecipesBinding;
import com.rgitapps.smartpantrymanager.util.StrictRecipeMatcher;

public class AllRecipesFragment extends Fragment implements RecipeAdapter.OnRecipeClickListener {

    private FragmentAllRecipesBinding binding;
    private RecipesViewModel viewModel;
    private RecipeAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAllRecipesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(RecipesViewModel.class);

        setupRecyclerView();
        setupSearchAndFilter();
        setupObservers();
    }

    private void setupRecyclerView() {
        adapter = new RecipeAdapter(this);
        binding.rvAllRecipes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAllRecipes.setAdapter(adapter);
    }

    private void setupSearchAndFilter() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.categoryChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                viewModel.setCategoryFilter("All");
                return;
            }
            Chip chip = group.findViewById(checkedId);
            if (chip != null) {
                viewModel.setCategoryFilter(chip.getText().toString());
            } else {
                viewModel.setCategoryFilter("All");
            }
        });
    }

    private void setupObservers() {
        viewModel.getAllPantryItems().observe(getViewLifecycleOwner(), pantryItems ->
                adapter.setPantryItems(pantryItems));

        viewModel.getFilteredAllRecipesLiveData().observe(getViewLifecycleOwner(), recipes -> {
            if (recipes == null || recipes.isEmpty()) {
                binding.rvAllRecipes.setVisibility(View.GONE);
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
            } else {
                binding.rvAllRecipes.setVisibility(View.VISIBLE);
                binding.layoutEmptyState.setVisibility(View.GONE);
            }
            adapter.submitList(recipes);
        });
    }

    @Override
    public void onRecipeClick(RecipeWithIngredients recipeWithIngredients, StrictRecipeMatcher.MatchResult matchResult) {
        if (recipeWithIngredients == null || recipeWithIngredients.getRecipe() == null) return;

        Bundle bundle = new Bundle();
        bundle.putInt("recipeId", recipeWithIngredients.getRecipe().getId());
        Navigation.findNavController(requireView()).navigate(R.id.recipeDetailFragment, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
