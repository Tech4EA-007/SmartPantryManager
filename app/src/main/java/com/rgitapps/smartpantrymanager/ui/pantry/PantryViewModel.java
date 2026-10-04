package com.rgitapps.smartpantrymanager.ui.pantry;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.rgitapps.smartpantrymanager.data.entity.PantryItem;
import com.rgitapps.smartpantrymanager.data.repository.PantryRepository;

import java.util.ArrayList;
import java.util.List;

public class PantryViewModel extends AndroidViewModel {

    private final PantryRepository repository;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> categoryFilter = new MutableLiveData<>("All");

    private final LiveData<List<PantryItem>> allPantryItems;
    private final MediatorLiveData<List<PantryItem>> filteredPantryItems = new MediatorLiveData<>();

    public PantryViewModel(@NonNull Application application) {
        super(application);
        repository = new PantryRepository(application);
        allPantryItems = repository.getAllPantryItems();

        filteredPantryItems.addSource(allPantryItems, items -> filterItems());
        filteredPantryItems.addSource(searchQuery, query -> filterItems());
        filteredPantryItems.addSource(categoryFilter, category -> filterItems());
    }

    private void filterItems() {
        List<PantryItem> rawItems = allPantryItems.getValue();
        if (rawItems == null) {
            filteredPantryItems.setValue(new ArrayList<>());
            return;
        }

        String query = searchQuery.getValue() != null ? searchQuery.getValue().trim().toLowerCase() : "";
        String category = categoryFilter.getValue() != null ? categoryFilter.getValue().trim() : "All";

        List<PantryItem> result = new ArrayList<>();
        for (PantryItem item : rawItems) {
            boolean matchesCategory = "All".equalsIgnoreCase(category)
                    || (item.getCategory() != null && item.getCategory().equalsIgnoreCase(category));

            boolean matchesSearch = query.isEmpty()
                    || (item.getName() != null && item.getName().toLowerCase().contains(query))
                    || (item.getCategory() != null && item.getCategory().toLowerCase().contains(query));

            if (matchesCategory && matchesSearch) {
                result.add(item);
            }
        }
        filteredPantryItems.setValue(result);
    }

    public LiveData<List<PantryItem>> getFilteredPantryItems() {
        return filteredPantryItems;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void setCategoryFilter(String category) {
        categoryFilter.setValue(category);
    }

    public void addPantryItem(PantryItem item) {
        repository.insertPantryItem(item);
    }

    public void updatePantryItem(PantryItem item) {
        repository.updatePantryItem(item);
    }

    public void deletePantryItem(PantryItem item) {
        repository.deletePantryItem(item);
    }
}
