package com.example.data.remote

import retrofit2.http.GET

interface ContentApiService {
    @GET("posts")
    suspend fun fetchRemoteContent(): List<NetworkContentDto>
}
