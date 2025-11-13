package com.stmh.composetemplate.data.repository

import com.stmh.composetemplate.data.model.Post

interface PostRepository {
    suspend fun getPosts(): Result<List<Post>>
    suspend fun getPost(): Result<Post>
}
