package com.stmh.composetemplate.data.remote

import com.stmh.composetemplate.data.model.LotteryDrawResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LotteryApiService {
    @GET("lt645/selectPstLt645InfoNew.do")
    suspend fun getLotteryDraws(
        @Query("srchDir") srchDir: String,
        @Query("srchLtEpsd") srchLtEpsd: Int,
        @Query("_") timestamp: Long
    ): LotteryDrawResponse
}
