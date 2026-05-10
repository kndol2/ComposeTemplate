package com.stmh.composetemplate.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.FileOutputStream

class LottoDatabaseHelper(private val context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "lotto.db"
        private const val DATABASE_VERSION = 1

        // Table and column names (기존 DB 스키마 사용)
        const val TABLE_LOTTO = "lotto"
        const val COLUMN_ROUND = "round"
        const val COLUMN_DATE = "date"
        const val COLUMN_NUM1 = "num1"
        const val COLUMN_NUM2 = "num2"
        const val COLUMN_NUM3 = "num3"
        const val COLUMN_NUM4 = "num4"
        const val COLUMN_NUM5 = "num5"
        const val COLUMN_NUM6 = "num6"
        const val COLUMN_BONUS = "bonus"
    }

    init {
        copyDatabaseFromAssets()
    }

    private fun copyDatabaseFromAssets() {
        val dbPath = context.getDatabasePath(DATABASE_NAME)

        // 이미 DB 파일이 존재하면 복사하지 않음
        if (dbPath.exists()) {
            return
        }

        // 부모 디렉토리가 없으면 생성
        dbPath.parentFile?.mkdirs()

        try {
            context.assets.open(DATABASE_NAME).use { input ->
                FileOutputStream(dbPath).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Failed to copy database from assets", e)
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // assets에서 복사하므로 테이블 생성 불필요
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 버전 업그레이드 시 필요한 마이그레이션 로직
    }

    /**
     * 최근 N개의 회차 데이터를 가져옵니다
     */
    fun getRecentDraws(count: Int): List<LottoDrawEntity> {
        val draws = mutableListOf<LottoDrawEntity>()
        val db = readableDatabase

        val cursor = db.query(
            TABLE_LOTTO,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_ROUND DESC",
            count.toString()
        )

        cursor.use {
            while (it.moveToNext()) {
                draws.add(
                    LottoDrawEntity(
                        drawNumber = it.getInt(it.getColumnIndexOrThrow(COLUMN_ROUND)),
                        drawDate = it.getString(it.getColumnIndexOrThrow(COLUMN_DATE)),
                        number1 = it.getInt(it.getColumnIndexOrThrow(COLUMN_NUM1)),
                        number2 = it.getInt(it.getColumnIndexOrThrow(COLUMN_NUM2)),
                        number3 = it.getInt(it.getColumnIndexOrThrow(COLUMN_NUM3)),
                        number4 = it.getInt(it.getColumnIndexOrThrow(COLUMN_NUM4)),
                        number5 = it.getInt(it.getColumnIndexOrThrow(COLUMN_NUM5)),
                        number6 = it.getInt(it.getColumnIndexOrThrow(COLUMN_NUM6)),
                        bonusNumber = it.getInt(it.getColumnIndexOrThrow(COLUMN_BONUS))
                    )
                )
            }
        }

        return draws
    }

    /**
     * 특정 회차의 데이터를 가져옵니다
     */
    fun getDrawByNumber(drawNumber: Int): LottoDrawEntity? {
        val db = readableDatabase

        val cursor = db.query(
            TABLE_LOTTO,
            null,
            "$COLUMN_ROUND = ?",
            arrayOf(drawNumber.toString()),
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                return LottoDrawEntity(
                    drawNumber = it.getInt(it.getColumnIndexOrThrow(COLUMN_ROUND)),
                    drawDate = it.getString(it.getColumnIndexOrThrow(COLUMN_DATE)),
                    number1 = it.getInt(it.getColumnIndexOrThrow(COLUMN_NUM1)),
                    number2 = it.getInt(it.getColumnIndexOrThrow(COLUMN_NUM2)),
                    number3 = it.getInt(it.getColumnIndexOrThrow(COLUMN_NUM3)),
                    number4 = it.getInt(it.getColumnIndexOrThrow(COLUMN_NUM4)),
                    number5 = it.getInt(it.getColumnIndexOrThrow(COLUMN_NUM5)),
                    number6 = it.getInt(it.getColumnIndexOrThrow(COLUMN_NUM6)),
                    bonusNumber = it.getInt(it.getColumnIndexOrThrow(COLUMN_BONUS))
                )
            }
        }

        return null
    }

    /**
     * 회차 데이터를 삽입합니다. 이미 존재하면 무시합니다.
     */
    fun insertDraw(entity: LottoDrawEntity): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ROUND, entity.drawNumber)
            put(COLUMN_DATE, entity.drawDate)
            put(COLUMN_NUM1, entity.number1)
            put(COLUMN_NUM2, entity.number2)
            put(COLUMN_NUM3, entity.number3)
            put(COLUMN_NUM4, entity.number4)
            put(COLUMN_NUM5, entity.number5)
            put(COLUMN_NUM6, entity.number6)
            put(COLUMN_BONUS, entity.bonusNumber)
        }
        return db.insertWithOnConflict(TABLE_LOTTO, null, values, SQLiteDatabase.CONFLICT_IGNORE) != -1L
    }

    /**
     * 최신 회차 번호를 가져옵니다
     */
    fun getLatestDrawNumber(): Int? {
        val db = readableDatabase

        val cursor = db.query(
            TABLE_LOTTO,
            arrayOf(COLUMN_ROUND),
            null,
            null,
            null,
            null,
            "$COLUMN_ROUND DESC",
            "1"
        )

        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(it.getColumnIndexOrThrow(COLUMN_ROUND))
            }
        }

        return null
    }
}

data class LottoDrawEntity(
    val drawNumber: Int,
    val drawDate: String,
    val number1: Int,
    val number2: Int,
    val number3: Int,
    val number4: Int,
    val number5: Int,
    val number6: Int,
    val bonusNumber: Int
)
