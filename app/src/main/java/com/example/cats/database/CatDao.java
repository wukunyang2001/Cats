package com.example.cats.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

@Dao
public interface CatDao {

    @Insert
    void insert(Cat cat);

    @Query("DELETE FROM cat_table")
    void delete();

    @Query("SELECT * FROM cat_table")
    LiveData<Cat> query();

}
