package com.stmh.composetemplate.data.repository

import com.stmh.composetemplate.data.model.LotteryDraw

interface LotteryRepository {
    suspend fun getLotteryDraw(drawNumber: Int): Result<LotteryDraw>
    suspend fun getRecentDraws(count: Int): Result<List<LotteryDraw>>
    suspend fun getLatestDrawNumber(): Result<Int>
    suspend fun syncMissingDraws(): Result<Int>
}
