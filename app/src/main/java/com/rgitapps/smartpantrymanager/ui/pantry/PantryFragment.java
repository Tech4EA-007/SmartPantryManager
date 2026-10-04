package com.rgitapps.smartpantrymanager.ui.pantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rgitapps.smartpantrymanager.R;
import com.rgitapps.smartpantrymanager.data.entity.PantryItem;
import com.rgitapps.smartpantrymanager.databinding.DialogAddEditPantryItemBinding;
import com.rgitapps.smartpantrymanager.databinding.FragmentPantryBinding;

import java.util.Calendar;
import java.util.Locale;

public class PantryFragment extends Fragment implements PantryAdapter.OnItemActionListener {

    private FragmentPantryBinding binding;
    private PantryViewModel viewModel;
    private PantryAdapter adapter;

    private final String[] UNITS = new String[]{"g", "kg", "ml", "l", "pcs", "cups", "tbsp", "tsp", "pinch", "items"};
    private final String[] CATEGORIES = new String[]{"Produce", "Dairy", "Meat", "Spices", "Staples", "Other"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPantryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PantryViewModel.class);

        setupRecyclerView();
        setupSearchAndFilter();
        setupObservers();

        binding.fabAddItem.setOnClickListener(v -> showAddEditDialog(null));
    }

    private void setupRecyclerView() {
        adapter = new PantryAdapter(this);
        binding.rvPantryItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPantryItems.setAdapter(adapter);
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
        viewModel.getFilteredPantryItems().observe(getViewLifecycleOwner(), items -> {
            if (items == null || items.isEmpty()) {
                binding.rvPantryItems.setVisibility(View.GONE);
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
            } else {
                binding.rvPantryItems.setVisibility(View.VISIBLE);
                binding.layoutEmptyState.setVisibility(View.GONE);
            }
            adapter.submitList(items);
        });
    }

    @Override
    public void onEdit(PantryItem item) {
        showAddEditDialog(item);
    }

    @Override
    public void onDelete(PantryItem item) {
        showDeleteConfirmationDialog(item);
    }

    private void showAddEditDialog(@Nullable PantryItem existingItem) {
        DialogAddEditPantryItemBinding dialogBinding = DialogAddEditPantryItemBinding.inflate(getLayoutInflater());

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, UNITS);
        dialogBinding.actvUnit.setAdapter(unitAdapter);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, CATEGORIES);
        dialogBinding.actvCategory.setAdapter(categoryAdapter);

        View.OnClickListener dateClickListener = v -> {
            Calendar calendar = Calendar.getInstance();
            String currentStr = dialogBinding.etExpiryDate.getText() != null ? dialogBinding.etExpiryDate.getText().toString().trim() : "";
            if (!currentStr.isEmpty() && currentStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                String[] parts = currentStr.split("-");
                calendar.set(Calendar.YEAR, Integer.parseInt(parts[0]));
                calendar.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
                calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
            }

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        String formattedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        dialogBinding.etExpiryDate.setText(formattedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        };

        dialogBinding.etExpiryDate.setOnClickListener(dateClickListener);
        dialogBinding.tilExpiryDate.setEndIconOnClickListener(dateClickListener);

        boolean isEdit = existingItem != null;
        if (isEdit) {
            dialogBinding.tvDialogTitle.setText(R.string.dialog_edit_pantry_item);
            dialogBinding.etName.setText(existingItem.getName());
            dialogBinding.etQuantity.setText(String.format(Locale.US, "%.1f", existingItem.getQuantity()));
            dialogBinding.actvUnit.setText(existingItem.getUnit(), false);
            dialogBinding.actvCategory.setText(existingItem.getCategory(), false);
            dialogBinding.etExpiryDate.setText(existingItem.getExpiryDate());
        } else {
            dialogBinding.tvDialogTitle.setText(R.string.dialog_add_pantry_item);
            dialogBinding.actvUnit.setText(UNITS[0], false);
            dialogBinding.actvCategory.setText(CATEGORIES[0], false);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton(isEdit ? "Update" : "Add", null)
                .setNegativeButton("Cancel", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(d ->
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = dialogBinding.etName.getText() != null ? dialogBinding.etName.getText().toString().trim() : "";
                String qtyStr = dialogBinding.etQuantity.getText() != null ? dialogBinding.etQuantity.getText().toString().trim() : "";
                String selectedUnit = dialogBinding.actvUnit.getText().toString().trim();
                String category = dialogBinding.actvCategory.getText().toString().trim();
                String expiryDate = dialogBinding.etExpiryDate.getText() != null ? dialogBinding.etExpiryDate.getText().toString().trim() : "";

                boolean isValid = true;

                if (name.isEmpty()) {
                    dialogBinding.tilName.setError("Name is required");
                    isValid = false;
                } else {
                    dialogBinding.tilName.setError(null);
                }

                double quantity = 0;
                if (qtyStr.isEmpty()) {
                    dialogBinding.tilQuantity.setError("Quantity is required");
                    isValid = false;
                } else {
                    try {
                        quantity = Double.parseDouble(qtyStr);
                        if (quantity <= 0) {
                            dialogBinding.tilQuantity.setError("Quantity must be greater than 0");
                            isValid = false;
                        } else {
                            dialogBinding.tilQuantity.setError(null);
                        }
                    } catch (NumberFormatException e) {
                        dialogBinding.tilQuantity.setError("Invalid number");
                        isValid = false;
                    }
                }

                if (category.isEmpty()) {
                    dialogBinding.tilCategory.setError("Category is required");
                    isValid = false;
                } else {
                    dialogBinding.tilCategory.setError(null);
                }

                if (expiryDate.isEmpty()) {
                    dialogBinding.tilExpiryDate.setError("Expiry date is required");
                    isValid = false;
                } else {
                    dialogBinding.tilExpiryDate.setError(null);
                }

                if (!isValid) return;

                String unit = selectedUnit.isEmpty() ? "pcs" : selectedUnit;

                if (isEdit) {
                    existingItem.setName(name);
                    existingItem.setQuantity(quantity);
                    existingItem.setUnit(unit);
                    existingItem.setCategory(category);
                    existingItem.setExpiryDate(expiryDate);
                    viewModel.updatePantryItem(existingItem);
                    Toast.makeText(requireContext(), "Pantry item updated", Toast.LENGTH_SHORT).show();
                } else {
                    PantryItem newItem = new PantryItem(name, quantity, unit, category, expiryDate);
                    viewModel.addPantryItem(newItem);
                    Toast.makeText(requireContext(), "Pantry item added", Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
            })
        );

        dialog.show();
    }

    private void showDeleteConfirmationDialog(PantryItem item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Pantry Item")
                .setMessage("Are you sure you want to delete '" + item.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deletePantryItem(item);
                    Toast.makeText(requireContext(), "Item deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
