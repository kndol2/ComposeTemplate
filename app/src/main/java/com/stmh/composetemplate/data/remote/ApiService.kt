package com.stmh.composetemplate.data.remote

import com.stmh.composetemplate.data.model.Post
import retrofit2.http.GET

interface ApiService {
    @GET("posts")
    suspend fun getPosts(): List<Post>

    @GET("posts/1")
    suspend fun getPost(): Post
}
