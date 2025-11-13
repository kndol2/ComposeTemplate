package com.stmh.composetemplate.data.repository

import com.stmh.composetemplate.data.model.Post
import com.stmh.composetemplate.data.remote.ApiService
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : PostRepository {

    override suspend fun getPosts(): Result<List<Post>> {
        return try {
            val posts = apiService.getPosts()
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPost(): Result<Post> {
        return try {
            val post = apiService.getPost()
            Result.success(post)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
