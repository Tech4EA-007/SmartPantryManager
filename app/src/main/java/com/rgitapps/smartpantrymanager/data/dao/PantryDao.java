package com.rgitapps.smartpantrymanager.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.rgitapps.smartpantrymanager.data.entity.PantryItem;

import java.util.List;

@Dao
public interface PantryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PantryItem item);

    @Update
    void update(PantryItem item);

    @Delete
    void delete(PantryItem item);

    @Query("DELETE FROM pantry_items")
    void deleteAll();

    @Query("SELECT * FROM pantry_items ORDER BY name ASC")
    LiveData<List<PantryItem>> getAllPantryItems();

    @Query("SELECT * FROM pantry_items WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY name ASC")
    LiveData<List<PantryItem>> searchPantryItems(String query);

    @Query("SELECT * FROM pantry_items ORDER BY name ASC")
    List<PantryItem> getPantryItemsList();
}
