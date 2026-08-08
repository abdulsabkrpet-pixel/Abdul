package com.example.data

import com.example.data.local.UserContent
import com.example.data.local.UserContentDao
import com.example.data.remote.ContentApiService
import com.example.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository class that synchronizes remote network data (Retrofit)
 * with the local Room database to ensure seamless offline access.
 */
class ContentSyncRepository(
    private val userContentDao: UserContentDao,
    private val apiService: ContentApiService = RetrofitClient.apiService
) {
    /**
     * Room database serves as the Single Source of Truth for UI observation.
     * Offline access is always available from local cache.
     */
    val cachedContent: Flow<List<UserContent>> = userContentDao.getAllContent()

    /**
     * Search cached Room content in real-time as the user types.
     */
    fun searchContent(query: String): Flow<List<UserContent>> {
        return if (query.isBlank()) {
            userContentDao.getAllContent()
        } else {
            userContentDao.searchContent(query)
        }
    }

    /**
     * Synchronizes remote data from Retrofit into the local Room database.
     * Maps DTOs to local entities and saves them to local storage.
     */
    suspend fun syncRemoteData(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val remotePosts = apiService.fetchRemoteContent()
            val entities = remotePosts.take(15).map { dto ->
                UserContent(
                    id = dto.id,
                    title = dto.title,
                    body = dto.body,
                    timestamp = System.currentTimeMillis()
                )
            }
            userContentDao.insertAll(entities)
            Result.success(entities.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addLocalContent(title: String, body: String) = withContext(Dispatchers.IO) {
        val newContent = UserContent(
            title = title,
            body = body,
            timestamp = System.currentTimeMillis()
        )
        userContentDao.insertContent(newContent)
    }

    suspend fun deleteContent(content: UserContent) = withContext(Dispatchers.IO) {
        userContentDao.deleteContent(content)
    }
}
