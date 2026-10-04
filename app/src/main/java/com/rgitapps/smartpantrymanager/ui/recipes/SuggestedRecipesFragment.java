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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import androidx.navigation.Navigation;
import com.rgitapps.smartpantrymanager.R;
import com.rgitapps.smartpantrymanager.data.entity.RecipeWithIngredients;
import com.rgitapps.smartpantrymanager.databinding.FragmentSuggestedRecipesBinding;
import com.rgitapps.smartpantrymanager.util.StrictRecipeMatcher;

public class SuggestedRecipesFragment extends Fragment implements RecipeAdapter.OnRecipeClickListener {

    private FragmentSuggestedRecipesBinding binding;
    private RecipesViewModel viewModel;
    private RecipeAdapter strictAdapter;
    private RecipeAdapter almostThereAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSuggestedRecipesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(RecipesViewModel.class);

        setupRecyclerViews();
        setupSearchAndFilter();
        setupObservers();
    }

    private void setupRecyclerViews() {
        strictAdapter = new RecipeAdapter(this);
        binding.rvSuggestedRecipes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSuggestedRecipes.setAdapter(strictAdapter);

        almostThereAdapter = new RecipeAdapter(this);
        binding.rvAlmostThereRecipes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAlmostThereRecipes.setAdapter(almostThereAdapter);
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
        viewModel.getAllPantryItems().observe(getViewLifecycleOwner(), pantryItems -> {
            strictAdapter.setPantryItems(pantryItems);
            almostThereAdapter.setPantryItems(pantryItems);
        });

        viewModel.getSuggestedRecipesLiveData().observe(getViewLifecycleOwner(), strictRecipes -> {
            boolean hasStrict = strictRecipes != null && !strictRecipes.isEmpty();
            if (hasStrict) {
                binding.tvStrictHeader.setVisibility(View.VISIBLE);
                binding.rvSuggestedRecipes.setVisibility(View.VISIBLE);
            } else {
                binding.tvStrictHeader.setVisibility(View.GONE);
                binding.rvSuggestedRecipes.setVisibility(View.GONE);
            }
            strictAdapter.submitList(strictRecipes);
            updateEmptyStateVisibility();
        });

        viewModel.getAlmostThereRecipesLiveData().observe(getViewLifecycleOwner(), almostThereRecipes -> {
            boolean hasAlmostThere = almostThereRecipes != null && !almostThereRecipes.isEmpty();
            if (hasAlmostThere) {
                binding.tvAlmostThereHeader.setVisibility(View.VISIBLE);
                binding.tvAlmostThereAlertMessage.setVisibility(View.VISIBLE);
                binding.rvAlmostThereRecipes.setVisibility(View.VISIBLE);
            } else {
                binding.tvAlmostThereHeader.setVisibility(View.GONE);
                binding.tvAlmostThereAlertMessage.setVisibility(View.GONE);
                binding.rvAlmostThereRecipes.setVisibility(View.GONE);
            }
            almostThereAdapter.submitList(almostThereRecipes);
            updateEmptyStateVisibility();
        });
    }

    private void updateEmptyStateVisibility() {
        boolean strictEmpty = binding.rvSuggestedRecipes.getVisibility() == View.GONE;
        boolean almostThereEmpty = binding.rvAlmostThereRecipes.getVisibility() == View.GONE;

        if (strictEmpty && almostThereEmpty) {
            binding.scrollViewContent.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.scrollViewContent.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
        }
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
