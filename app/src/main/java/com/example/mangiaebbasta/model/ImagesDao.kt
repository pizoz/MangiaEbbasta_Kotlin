package com.example.mangiaebbasta.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ImagesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(ImageForDB: ImageForDB)

    @Query("SELECT * FROM images WHERE mid = :mid")
    suspend fun getImageFromDB(mid: Int): ImageForDB?

}