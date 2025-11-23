package com.stmh.composetemplate.data.repository

import com.stmh.composetemplate.data.model.LotteryDraw
import com.stmh.composetemplate.data.remote.LotteryApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class LotteryRepositoryImpl @Inject constructor(
    private val lotteryApiService: LotteryApiService
) : LotteryRepository {

    override suspend fun getLotteryDraw(drawNumber: Int): Result<LotteryDraw> {
        return try {
            val draw = lotteryApiService.getLotteryDraw(drawNumber)
            if (draw.returnValue == "success") {
                Result.success(draw)
            } else {
                Result.failure(Exception("Invalid draw number"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecentDraws(count: Int): Result<List<LotteryDraw>> {
        return try {
            val latestDrawNumber = getLatestDrawNumber().getOrThrow()
            val startDrawNumber = maxOf(1, latestDrawNumber - count + 1)

            coroutineScope {
                val draws = (startDrawNumber..latestDrawNumber).map { drawNumber ->
                    async {
                        getLotteryDraw(drawNumber).getOrNull()
                    }
                }.awaitAll().filterNotNull()

                Result.success(draws)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLatestDrawNumber(): Result<Int> {
        return try {
            val currentDraw = lotteryApiService.getLotteryDraw(1)
            if (currentDraw.returnValue == "success") {
                var low = 1
                var high = 2000
                var latest = 1

                while (low <= high) {
                    val mid = (low + high) / 2
                    val draw = lotteryApiService.getLotteryDraw(mid)

                    if (draw.returnValue == "success") {
                        latest = mid
                        low = mid + 1
                    } else {
                        high = mid - 1
                    }
                }

                Result.success(latest)
            } else {
                Result.failure(Exception("Cannot determine latest draw number"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
