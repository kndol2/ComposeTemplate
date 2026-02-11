package com.stmh.composetemplate.data.repository

import com.stmh.composetemplate.data.local.LottoDatabaseHelper
import com.stmh.composetemplate.data.local.LottoDrawEntity
import com.stmh.composetemplate.data.model.LotteryDraw
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LotteryRepositoryImpl @Inject constructor(
    private val databaseHelper: LottoDatabaseHelper
) : LotteryRepository {

    override suspend fun getLotteryDraw(drawNumber: Int): Result<LotteryDraw> {
        return withContext(Dispatchers.IO) {
            try {
                val entity = databaseHelper.getDrawByNumber(drawNumber)
                if (entity != null) {
                    Result.success(entity.toLotteryDraw())
                } else {
                    Result.failure(Exception("Draw not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getRecentDraws(count: Int): Result<List<LotteryDraw>> {
        return withContext(Dispatchers.IO) {
            try {
                val entities = databaseHelper.getRecentDraws(count)
                Result.success(entities.map { it.toLotteryDraw() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getLatestDrawNumber(): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val latestNumber = databaseHelper.getLatestDrawNumber()
                if (latestNumber != null) {
                    Result.success(latestNumber)
                } else {
                    Result.failure(Exception("Cannot determine latest draw number"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun LottoDrawEntity.toLotteryDraw(): LotteryDraw {
        return LotteryDraw(
            drawNumber = this.drawNumber,
            drawDate = this.drawDate,
            number1 = this.number1,
            number2 = this.number2,
            number3 = this.number3,
            number4 = this.number4,
            number5 = this.number5,
            number6 = this.number6,
            bonusNumber = this.bonusNumber,
            firstPrizeAmount = 0L,
            firstPrizeWinners = 0,
            totalSalesAmount = 0L,
            returnValue = "success"
        )
    }
}
