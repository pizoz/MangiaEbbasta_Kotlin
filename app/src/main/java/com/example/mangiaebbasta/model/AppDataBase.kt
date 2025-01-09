package com.example.mangiaebbasta.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ImageForDB::class], version = 1)
abstract class AppDataBase : RoomDatabase() {
    abstract fun imagesDao(): ImagesDao
}

