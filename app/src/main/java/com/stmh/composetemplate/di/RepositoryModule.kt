package com.stmh.composetemplate.di

import com.stmh.composetemplate.data.repository.LotteryRepository
import com.stmh.composetemplate.data.repository.LotteryRepositoryImpl
import com.stmh.composetemplate.data.repository.PostRepository
import com.stmh.composetemplate.data.repository.PostRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        postRepositoryImpl: PostRepositoryImpl
    ): PostRepository

    @Binds
    @Singleton
    abstract fun bindLotteryRepository(
        lotteryRepositoryImpl: LotteryRepositoryImpl
    ): LotteryRepository
}
