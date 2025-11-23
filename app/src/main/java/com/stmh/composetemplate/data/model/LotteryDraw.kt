package com.stmh.composetemplate.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LotteryDraw(
    @SerialName("drwNo")
    val drawNumber: Int = 0,

    @SerialName("drwNoDate")
    val drawDate: String = "",

    @SerialName("drwtNo1")
    val number1: Int = 0,

    @SerialName("drwtNo2")
    val number2: Int = 0,

    @SerialName("drwtNo3")
    val number3: Int = 0,

    @SerialName("drwtNo4")
    val number4: Int = 0,

    @SerialName("drwtNo5")
    val number5: Int = 0,

    @SerialName("drwtNo6")
    val number6: Int = 0,

    @SerialName("bnusNo")
    val bonusNumber: Int = 0,

    @SerialName("firstWinamnt")
    val firstPrizeAmount: Long = 0,

    @SerialName("firstPrzwnerCo")
    val firstPrizeWinners: Int = 0,

    @SerialName("totSellamnt")
    val totalSalesAmount: Long = 0,

    @SerialName("returnValue")
    val returnValue: String = ""
) {
    fun getWinningNumbers(): List<Int> {
        return listOf(number1, number2, number3, number4, number5, number6)
    }
}
