package com.example.cats.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "cat_table")
public class Cat {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "catGif")
    public byte[] catGif;

    public Cat(@NonNull byte[] catGif) {
        this.catGif = catGif;
    }
}