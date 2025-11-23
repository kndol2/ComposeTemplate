package com.stmh.composetemplate.data.remote

import com.stmh.composetemplate.data.model.LotteryDraw
import retrofit2.http.GET
import retrofit2.http.Query

interface LotteryApiService {
    @GET("common.do?method=getLottoNumber")
    suspend fun getLotteryDraw(
        @Query("drwNo") drawNumber: Int
    ): LotteryDraw
}
