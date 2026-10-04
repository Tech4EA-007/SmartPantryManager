package com.rgitapps.smartpantrymanager.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.rgitapps.smartpantrymanager.R;
import com.rgitapps.smartpantrymanager.data.repository.PantryRepository;

public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "SmartPantryPrefs";
    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts_enabled";
    private static final String KEY_UNIT_SYSTEM = "unit_system";

    private SwitchMaterial chkExpiryAlerts;
    private RadioGroup radioUnitsGroup;
    private RadioButton radioMetric;
    private RadioButton radioImperial;
    private Button btnResetData;

    private PantryRepository repository;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        chkExpiryAlerts = view.findViewById(R.id.chkExpiryAlerts);
        radioUnitsGroup = view.findViewById(R.id.radioUnitsGroup);
        radioMetric = view.findViewById(R.id.radioMetric);
        radioImperial = view.findViewById(R.id.radioImperial);
        btnResetData = view.findViewById(R.id.btnResetData);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new PantryRepository(requireContext());
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        loadSettings();
        setupListeners();
    }

    private void loadSettings() {
        boolean expiryAlerts = prefs.getBoolean(KEY_EXPIRY_ALERTS, true);
        chkExpiryAlerts.setChecked(expiryAlerts);

        String unitSystem = prefs.getString(KEY_UNIT_SYSTEM, "metric");
        if ("imperial".equalsIgnoreCase(unitSystem)) {
            radioImperial.setChecked(true);
        } else {
            radioMetric.setChecked(true);
        }
    }

    private void setupListeners() {
        chkExpiryAlerts.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, isChecked).apply());

        radioUnitsGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String unitSystem = (checkedId == R.id.radioImperial) ? "imperial" : "metric";
            prefs.edit().putString(KEY_UNIT_SYSTEM, unitSystem).apply();
        });

        btnResetData.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Reset Data")
                .setMessage("Are you sure you want to reset pantry items and re-seed sample recipes?")
                .setPositiveButton("Reset", (dialog, which) -> repository.resetDatabase(() -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Database reset successfully", Toast.LENGTH_SHORT).show();
                    }
                }))
                .setNegativeButton("Cancel", null)
                .show());
    }
}
