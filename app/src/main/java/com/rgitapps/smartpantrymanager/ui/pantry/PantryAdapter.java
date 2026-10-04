package com.rgitapps.smartpantrymanager.ui.pantry;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.rgitapps.smartpantrymanager.data.entity.PantryItem;
import com.rgitapps.smartpantrymanager.databinding.ItemPantryBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PantryAdapter extends ListAdapter<PantryItem, PantryAdapter.PantryViewHolder> {

    public interface OnItemActionListener {
        void onEdit(PantryItem item);
        void onDelete(PantryItem item);
    }

    private final OnItemActionListener listener;

    public PantryAdapter(OnItemActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<PantryItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull PantryItem oldItem, @NonNull PantryItem newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull PantryItem oldItem, @NonNull PantryItem newItem) {
            return oldItem.getId() == newItem.getId()
                    && Objects.equals(oldItem.getName(), newItem.getName())
                    && oldItem.getQuantity() == newItem.getQuantity()
                    && Objects.equals(oldItem.getUnit(), newItem.getUnit())
                    && Objects.equals(oldItem.getCategory(), newItem.getCategory())
                    && Objects.equals(oldItem.getExpiryDate(), newItem.getExpiryDate());
        }
    };

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPantryBinding binding = ItemPantryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PantryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    public static class PantryViewHolder extends RecyclerView.ViewHolder {

        private final ItemPantryBinding binding;

        public PantryViewHolder(@NonNull ItemPantryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(PantryItem item, OnItemActionListener listener) {
            binding.tvItemName.setText(item.getName() != null ? item.getName() : "");

            // Format quantity + unit badge
            double qty = item.getQuantity();
            String qtyStr = (qty % 1 == 0) ? String.format(Locale.US, "%d", (long) qty) : String.format(Locale.US, "%.1f", qty);
            String unit = item.getUnit() != null ? item.getUnit() : "";
            binding.tvQuantityBadge.setText(String.format(Locale.US, "%s %s", qtyStr, unit));

            // Category badge
            binding.tvCategoryBadge.setText(item.getCategory() != null ? item.getCategory() : "Other");

            // Calculate expiration status relative to today's date
            int daysRemaining = calculateDaysUntilExpiry(item.getExpiryDate());
            int badgeColor;
            String expiryText;

            if (daysRemaining <= 0) {
                // Red for expired/today
                badgeColor = Color.parseColor("#D32F2F");
                if (daysRemaining < 0) {
                    expiryText = String.format(Locale.US, "Expired (%s)", item.getExpiryDate());
                } else {
                    expiryText = String.format(Locale.US, "Expires Today (%s)", item.getExpiryDate());
                }
            } else if (daysRemaining <= 3) {
                // Orange/Yellow for <= 3 days
                badgeColor = Color.parseColor("#F57C00");
                expiryText = String.format(Locale.US, "Exp: %s (%dd)", item.getExpiryDate(), daysRemaining);
            } else {
                // Green for normal (> 3 days)
                badgeColor = Color.parseColor("#388E3C");
                expiryText = String.format(Locale.US, "Exp: %s", item.getExpiryDate());
            }

            binding.tvExpiryBadge.setText(expiryText);
            binding.tvExpiryBadge.setBackgroundTintList(ColorStateList.valueOf(badgeColor));

            // Click listeners
            binding.btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(item);
                }
            });

            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(item);
                }
            });
        }

        private int calculateDaysUntilExpiry(String expiryDateStr) {
            if (expiryDateStr == null || expiryDateStr.trim().isEmpty()) {
                return Integer.MAX_VALUE;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setLenient(false);
            try {
                Date expiryDate = sdf.parse(expiryDateStr.trim());
                if (expiryDate == null) return Integer.MAX_VALUE;

                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);

                Calendar target = Calendar.getInstance();
                target.setTime(expiryDate);
                target.set(Calendar.HOUR_OF_DAY, 0);
                target.set(Calendar.MINUTE, 0);
                target.set(Calendar.SECOND, 0);
                target.set(Calendar.MILLISECOND, 0);

                long diffMs = target.getTimeInMillis() - today.getTimeInMillis();
                return (int) TimeUnit.MILLISECONDS.toDays(diffMs);
            } catch (ParseException e) {
                return Integer.MAX_VALUE;
            }
        }
    }
}
