package com.example.cats.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Cat.class}, version = 1, exportSchema = false)
public abstract class CatDatabase extends RoomDatabase {

    public abstract CatDao catDao();

    private static volatile CatDatabase INSTANCE;

    public static CatDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (CatDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            CatDatabase.class, "word_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}