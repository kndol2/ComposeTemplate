package com.stmh.composetemplate.data.repository

import com.stmh.composetemplate.data.local.LottoDatabaseHelper
import com.stmh.composetemplate.data.local.LottoDrawEntity
import com.stmh.composetemplate.data.model.LotteryDraw
import com.stmh.composetemplate.data.model.LotteryDrawItem
import com.stmh.composetemplate.data.remote.LotteryApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.GregorianCalendar
import javax.inject.Inject

class LotteryRepositoryImpl @Inject constructor(
    private val databaseHelper: LottoDatabaseHelper,
    private val apiService: LotteryApiService
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

    override suspend fun syncMissingDraws(): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                Result.success(syncInternal())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun syncInternal(): Int {
        // 1단계: DB 마지막 회차 확인
        val localLatest = databaseHelper.getLatestDrawNumber()
            ?: throw Exception("DB is empty")

        // 2단계: 최신 회차부터 역방향으로 탐색
        // srchLtEpsd=N 은 N을 포함한 10개 회차를 반환하므로
        // 최신부터 내려오면서 localLatest를 포함하는 청크에 도달하면 종료
        var target = calculateEstimatedLatestRound()
        var synced = 0
        var isFirstCall = true

        while (true) {
            val list = runCatching {
                apiService.getLotteryDraws(
                    srchDir = "center",
                    srchLtEpsd = target,
                    timestamp = System.currentTimeMillis()
                )
            }.getOrNull()?.data?.list ?: break

            if (list.isEmpty()) break

            // 첫 호출: API 최신 회차와 DB 최신 회차 비교
            if (isFirstCall) {
                val apiLatest = list.maxOfOrNull { it.ltEpsd } ?: break
                if (apiLatest <= localLatest) return 0  // 이미 최신
                isFirstCall = false
            }

            // localLatest 초과 회차만 저장
            list.filter { it.ltEpsd > localLatest }
                .sortedBy { it.ltEpsd }
                .forEach { draw ->
                    databaseHelper.insertDraw(draw.toEntity())
                    synced++
                }

            // 이번 응답의 최솟값이 localLatest 이하면 모든 누락 회차 커버 완료
            val minReturned = list.minOfOrNull { it.ltEpsd } ?: break
            if (minReturned <= localLatest) break

            // 다음 청크: 이번 최솟값 바로 아래 회차를 상한으로 설정
            target = minReturned - 1
        }

        return synced
    }

    // 1회차 추첨일(2002-12-07) 기준으로 현재 회차 추정 (+1은 1회차 보정)
    private fun calculateEstimatedLatestRound(): Int {
        val firstDrawMs = GregorianCalendar(2002, 11, 7).timeInMillis
        val daysBetween = (System.currentTimeMillis() - firstDrawMs) / (1000L * 60 * 60 * 24)
        return (daysBetween / 7).toInt() + 1
    }

    private fun LotteryDrawItem.toEntity() = LottoDrawEntity(
        drawNumber = ltEpsd,
        drawDate = ltRflYmd.toFormattedDate(),
        number1 = tm1WnNo,
        number2 = tm2WnNo,
        number3 = tm3WnNo,
        number4 = tm4WnNo,
        number5 = tm5WnNo,
        number6 = tm6WnNo,
        bonusNumber = bnsWnNo
    )

    // "20260509" → "2026-05-09"
    private fun String.toFormattedDate(): String =
        if (length == 8) "${substring(0, 4)}-${substring(4, 6)}-${substring(6, 8)}" else this

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
