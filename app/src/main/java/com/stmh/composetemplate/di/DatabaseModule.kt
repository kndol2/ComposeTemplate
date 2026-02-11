package com.stmh.composetemplate.di

import android.content.Context
import com.stmh.composetemplate.data.local.LottoDatabaseHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLottoDatabaseHelper(
        @ApplicationContext context: Context
    ): LottoDatabaseHelper {
        return LottoDatabaseHelper(context)
    }
}
