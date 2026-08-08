package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserContentDao {
    @Query("SELECT * FROM user_content ORDER BY timestamp DESC")
    fun getAllContent(): Flow<List<UserContent>>

    @Query("SELECT * FROM user_content WHERE title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchContent(query: String): Flow<List<UserContent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(content: UserContent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contents: List<UserContent>)

    @Delete
    suspend fun deleteContent(content: UserContent)

    @Query("DELETE FROM user_content WHERE id = :id")
    suspend fun deleteById(id: Int)
}
