package com.stmh.composetemplate.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LotteryDrawResponse(
    val resultCode: String? = null,
    val resultMessage: String? = null,
    val data: LotteryDrawData? = null
)

@Serializable
data class LotteryDrawData(
    val list: List<LotteryDrawItem> = emptyList()
)

@Serializable
data class LotteryDrawItem(
    val ltEpsd: Int = 0,
    val ltRflYmd: String = "",
    val tm1WnNo: Int = 0,
    val tm2WnNo: Int = 0,
    val tm3WnNo: Int = 0,
    val tm4WnNo: Int = 0,
    val tm5WnNo: Int = 0,
    val tm6WnNo: Int = 0,
    val bnsWnNo: Int = 0
)
