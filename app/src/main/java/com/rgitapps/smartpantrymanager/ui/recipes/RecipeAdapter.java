package com.rgitapps.smartpantrymanager.ui.recipes;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.rgitapps.smartpantrymanager.data.entity.PantryItem;
import com.rgitapps.smartpantrymanager.data.entity.RecipeIngredient;
import com.rgitapps.smartpantrymanager.data.entity.RecipeWithIngredients;
import com.rgitapps.smartpantrymanager.databinding.ItemRecipeBinding;
import com.rgitapps.smartpantrymanager.util.StrictRecipeMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class RecipeAdapter extends ListAdapter<RecipeWithIngredients, RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(RecipeWithIngredients recipeWithIngredients, StrictRecipeMatcher.MatchResult matchResult);
    }

    private final OnRecipeClickListener listener;
    private List<PantryItem> pantryItems = new ArrayList<>();

    public RecipeAdapter(OnRecipeClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public void setPantryItems(List<PantryItem> pantryItems) {
        this.pantryItems = Objects.requireNonNullElseGet(pantryItems, ArrayList::new);
        notifyDataSetChanged();
    }

    private static final DiffUtil.ItemCallback<RecipeWithIngredients> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull RecipeWithIngredients oldItem, @NonNull RecipeWithIngredients newItem) {
            return oldItem.getRecipe() != null && newItem.getRecipe() != null
                    && oldItem.getRecipe().getId() == newItem.getRecipe().getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull RecipeWithIngredients oldItem, @NonNull RecipeWithIngredients newItem) {
            if (oldItem.getRecipe() == null || newItem.getRecipe() == null) return false;
            return oldItem.getRecipe().getId() == newItem.getRecipe().getId()
                    && Objects.equals(oldItem.getRecipe().getName(), newItem.getRecipe().getName())
                    && Objects.equals(oldItem.getRecipe().getCategory(), newItem.getRecipe().getCategory())
                    && Objects.equals(oldItem.getRecipe().getInstructions(), newItem.getRecipe().getInstructions())
                    && Objects.equals(oldItem.getIngredients(), newItem.getIngredients());
        }
    };

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecipeBinding binding = ItemRecipeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RecipeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        holder.bind(getItem(position), pantryItems, listener);
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {

        private final ItemRecipeBinding binding;

        public RecipeViewHolder(@NonNull ItemRecipeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(RecipeWithIngredients item, List<PantryItem> pantryItems, OnRecipeClickListener listener) {
            if (item == null || item.getRecipe() == null) return;

            binding.tvRecipeTitle.setText(item.getRecipe().getName() != null ? item.getRecipe().getName() : "");
            binding.tvCategoryBadge.setText(item.getRecipe().getCategory() != null ? item.getRecipe().getCategory() : "General");

            StrictRecipeMatcher.MatchResult matchResult = StrictRecipeMatcher.analyzeRecipeMatch(item, pantryItems);

            if (matchResult.isMatched()) {
                binding.tvMatchStatusBadge.setText("100% Ready to Cook");
                binding.tvMatchStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#388E3C")));
            } else {
                int missingCount = matchResult.getMissingIngredients().size();
                String text = String.format(Locale.US, "Missing %d %s", missingCount, missingCount == 1 ? "ingredient" : "ingredients");
                binding.tvMatchStatusBadge.setText(text);
                binding.tvMatchStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D32F2F")));
            }

            // Summary of ingredients with green checkmarks or red warnings
            SpannableStringBuilder summaryBuilder = new SpannableStringBuilder();
            List<RecipeIngredient> ingredients = item.getIngredients();

            if (ingredients != null && !ingredients.isEmpty()) {
                for (int i = 0; i < ingredients.size(); i++) {
                    RecipeIngredient req = ingredients.get(i);
                    if (req == null) continue;

                    StrictRecipeMatcher.MissingIngredientInfo missingInfo = findMissingInfo(req, matchResult.getMissingIngredients());

                    int startPos = summaryBuilder.length();
                    String line;
                    int color;

                    if (missingInfo != null) {
                        // Missing or insufficient stock
                        line = String.format(Locale.US, "✗ %s (%s %s - %s available)",
                                req.getIngredientName(),
                                formatQty(req.getRequiredQuantity()),
                                req.getUnit(),
                                formatQty(missingInfo.getAvailableQuantity()));
                        color = Color.parseColor("#D32F2F"); // Red
                    } else {
                        // Available in stock
                        line = String.format(Locale.US, "✓ %s (%s %s)",
                                req.getIngredientName(),
                                formatQty(req.getRequiredQuantity()),
                                req.getUnit());
                        color = Color.parseColor("#388E3C"); // Green
                    }

                    summaryBuilder.append(line);
                    summaryBuilder.setSpan(
                            new ForegroundColorSpan(color),
                            startPos,
                            startPos + line.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );

                    if (i < ingredients.size() - 1) {
                        summaryBuilder.append("\n");
                    }
                }
            } else {
                summaryBuilder.append("No ingredients listed.");
            }

            binding.tvIngredientsSummary.setText(summaryBuilder);

            binding.cardRecipe.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecipeClick(item, matchResult);
                }
            });
        }

        private StrictRecipeMatcher.MissingIngredientInfo findMissingInfo(
                RecipeIngredient req, List<StrictRecipeMatcher.MissingIngredientInfo> missingList) {
            if (missingList == null || req == null || req.getIngredientName() == null) return null;
            String normReqName = StrictRecipeMatcher.normalizeName(req.getIngredientName());

            for (StrictRecipeMatcher.MissingIngredientInfo info : missingList) {
                if (info != null && info.getIngredientName() != null) {
                    String normMissingName = StrictRecipeMatcher.normalizeName(info.getIngredientName());
                    if (Objects.equals(normReqName, normMissingName)) {
                        return info;
                    }
                }
            }
            return null;
        }

        private String formatQty(double qty) {
            if (qty % 1 == 0) {
                return String.format(Locale.US, "%d", (long) qty);
            } else {
                return String.format(Locale.US, "%.1f", qty);
            }
        }
    }
}
