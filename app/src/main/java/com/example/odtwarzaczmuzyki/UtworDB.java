package com.example.odtwarzaczmuzyki;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Utwor.class}, version = 1, exportSchema = false)
public abstract class UtworDB extends RoomDatabase {
    public abstract UtworDao zwrocDao();
}
