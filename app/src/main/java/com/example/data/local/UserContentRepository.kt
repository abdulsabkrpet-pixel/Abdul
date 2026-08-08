package com.example.data.local

import kotlinx.coroutines.flow.Flow

class UserContentRepository(private val userContentDao: UserContentDao) {
    val allContent: Flow<List<UserContent>> = userContentDao.getAllContent()

    suspend fun insert(content: UserContent) {
        userContentDao.insertContent(content)
    }

    suspend fun delete(content: UserContent) {
        userContentDao.deleteContent(content)
    }

    suspend fun deleteById(id: Int) {
        userContentDao.deleteById(id)
    }
}
