package com.stmh.composetemplate.di

import com.stmh.composetemplate.domain.usecase.ChaosLotteryGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideChaosLotteryGenerator(): ChaosLotteryGenerator {
        return ChaosLotteryGenerator()
    }
}
