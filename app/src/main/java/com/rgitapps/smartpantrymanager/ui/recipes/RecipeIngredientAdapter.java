package com.rgitapps.smartpantrymanager.ui.recipes;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rgitapps.smartpantrymanager.R;
import com.rgitapps.smartpantrymanager.data.entity.PantryItem;
import com.rgitapps.smartpantrymanager.data.entity.RecipeIngredient;
import com.rgitapps.smartpantrymanager.util.StrictRecipeMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class RecipeIngredientAdapter extends RecyclerView.Adapter<RecipeIngredientAdapter.ViewHolder> {

    private List<RecipeIngredient> ingredients = new ArrayList<>();
    private List<PantryItem> pantryItems = new ArrayList<>();

    public void setIngredients(List<RecipeIngredient> ingredients) {
        this.ingredients = Objects.requireNonNullElseGet(ingredients, ArrayList::new);
        notifyDataSetChanged();
    }

    public void setPantryItems(List<PantryItem> pantryItems) {
        this.pantryItems = Objects.requireNonNullElseGet(pantryItems, ArrayList::new);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe_detail_ingredient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeIngredient req = ingredients.get(position);
        if (req == null) return;

        holder.textIngredientName.setText(req.getIngredientName());
        holder.textRequiredQty.setText(String.format(Locale.US, "Required: %.1f %s", req.getRequiredQuantity(), req.getUnit()));

        String reqNameNorm = StrictRecipeMatcher.normalizeName(req.getIngredientName());
        String reqUnitNorm = StrictRecipeMatcher.normalizeUnit(req.getUnit());

        double totalAvailable = 0.0;
        for (PantryItem pantryItem : pantryItems) {
            if (pantryItem == null || pantryItem.getName() == null) continue;

            String pantryNameNorm = StrictRecipeMatcher.normalizeName(pantryItem.getName());
            if (StrictRecipeMatcher.namesMatch(reqNameNorm, pantryNameNorm)) {
                String pantryUnitNorm = StrictRecipeMatcher.normalizeUnit(pantryItem.getUnit());
                double converted = StrictRecipeMatcher.convertQuantity(pantryItem.getQuantity(), pantryUnitNorm, reqUnitNorm);
                totalAvailable += converted;
            }
        }

        holder.textAvailableQty.setText(String.format(Locale.US, "Pantry: %.1f %s", totalAvailable, req.getUnit()));

        if (totalAvailable >= req.getRequiredQuantity() - 0.0001) {
            holder.textIngredientStatus.setText("✓ In Stock");
            holder.textIngredientStatus.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            holder.textIngredientStatus.setText("✗ Insufficient");
            holder.textIngredientStatus.setTextColor(Color.parseColor("#C62828"));
        }
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textIngredientName;
        final TextView textRequiredQty;
        final TextView textAvailableQty;
        final TextView textIngredientStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textIngredientName = itemView.findViewById(R.id.textIngredientName);
            textRequiredQty = itemView.findViewById(R.id.textRequiredQty);
            textAvailableQty = itemView.findViewById(R.id.textAvailableQty);
            textIngredientStatus = itemView.findViewById(R.id.textIngredientStatus);
        }
    }
}
