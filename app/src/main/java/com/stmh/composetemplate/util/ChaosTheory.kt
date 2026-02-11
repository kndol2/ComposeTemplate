package com.stmh.composetemplate.util

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 카오스 이론을 적용한 번호 생성 알고리즘
 *
 * 사용하는 카오스 시스템:
 * 1. Logistic Map (로지스틱 맵): x(n+1) = r * x(n) * (1 - x(n))
 * 2. Lorenz Attractor (로렌츠 어트랙터): 3차원 비선형 동역학 시스템
 */
class ChaosTheoryLottoGenerator(
    private val seed: Long = System.currentTimeMillis()
) {

    companion object {
        // 로지스틱 맵 파라미터 (카오스 영역)
        private const val LOGISTIC_R = 3.9 // 3.57 < r <= 4.0 범위에서 카오스 발생

        // 로렌츠 어트랙터 파라미터
        private const val SIGMA = 10.0
        private const val RHO = 28.0
        private const val BETA = 8.0 / 3.0
        private const val DT = 0.01 // 시간 간격
    }

    /**
     * 로지스틱 맵 (Logistic Map)
     * 초기값에 매우 민감한 카오스 함수
     * x(n+1) = r * x(n) * (1 - x(n))
     */
    private fun logisticMap(x: Double, iterations: Int = 100): Double {
        var current = x
        repeat(iterations) {
            current = LOGISTIC_R * current * (1 - current)
        }
        return current
    }

    /**
     * 로렌츠 어트랙터 (Lorenz Attractor)
     * 3차원 비선형 동역학 시스템
     * dx/dt = σ(y - x)
     * dy/dt = x(ρ - z) - y
     * dz/dt = xy - βz
     */
    private data class LorenzState(var x: Double, var y: Double, var z: Double)

    private fun lorenzAttractor(
        initialX: Double,
        initialY: Double,
        initialZ: Double,
        steps: Int = 1000
    ): LorenzState {
        val state = LorenzState(initialX, initialY, initialZ)

        repeat(steps) {
            val dx = SIGMA * (state.y - state.x) * DT
            val dy = (state.x * (RHO - state.z) - state.y) * DT
            val dz = (state.x * state.y - BETA * state.z) * DT

            state.x += dx
            state.y += dy
            state.z += dz
        }

        return state
    }

    /**
     * Hénon Map (에농 맵)
     * 2차원 카오스 맵
     * x(n+1) = 1 - a * x(n)^2 + y(n)
     * y(n+1) = b * x(n)
     */
    private data class HenonState(var x: Double, var y: Double)

    private fun henonMap(
        initialX: Double,
        initialY: Double,
        iterations: Int = 100,
        a: Double = 1.4,
        b: Double = 0.3
    ): HenonState {
        val state = HenonState(initialX, initialY)

        repeat(iterations) {
            val newX = 1 - a * state.x * state.x + state.y
            val newY = b * state.x
            state.x = newX
            state.y = newY
        }

        return state
    }

    /**
     * 카오스 이론을 적용하여 로또 번호 6개 생성
     */
    fun generateNumbers(): List<Int> {
        val numbers = mutableSetOf<Int>()

        // seed를 사용하여 초기값 생성
        val initialValues = generateInitialValues(seed)

        var iteration = 0
        while (numbers.size < 6) {
            val method = iteration % 3

            val chaosValue = when (method) {
                0 -> {
                    // 로지스틱 맵 사용
                    val x = (initialValues[iteration % 6] + iteration * 0.01) % 1.0
                    logisticMap(x.coerceIn(0.01, 0.99), 100 + iteration)
                }
                1 -> {
                    // 로렌츠 어트랙터 사용
                    val lorenz = lorenzAttractor(
                        initialValues[0] * 10,
                        initialValues[1] * 10,
                        initialValues[2] * 10,
                        1000 + iteration * 10
                    )
                    // x, y, z 값을 조합하여 0~1 범위로 정규화
                    val combined = (abs(lorenz.x) + abs(lorenz.y) + abs(lorenz.z)) / 100.0
                    combined % 1.0
                }
                else -> {
                    // Hénon 맵 사용
                    val henon = henonMap(
                        initialValues[3] * 2 - 1,
                        initialValues[4] * 2 - 1,
                        100 + iteration
                    )
                    val combined = (abs(henon.x) + abs(henon.y)) / 4.0
                    combined % 1.0
                }
            }

            // 카오스 값을 1~45 범위의 정수로 변환
            val number = (chaosValue * 45).toInt() + 1

            if (number in 1..45) {
                numbers.add(number)
            }

            iteration++

            // 무한 루프 방지
            if (iteration > 1000) {
                // 충분한 번호가 없으면 랜덤으로 채움
                while (numbers.size < 6) {
                    numbers.add((1..45).random())
                }
                break
            }
        }

        return numbers.sorted()
    }

    /**
     * seed로부터 초기값 배열 생성
     */
    private fun generateInitialValues(seed: Long): List<Double> {
        val random = java.util.Random(seed)
        return List(6) { random.nextDouble() }
    }

    /**
     * 카오스 기반 가중치를 적용한 번호 생성
     * 과거 데이터를 초기값으로 사용
     */
    fun generateNumbersWithHistory(recentNumbers: List<Int>): List<Int> {
        val numbers = mutableSetOf<Int>()

        // 최근 번호들을 초기값으로 사용 (나비 효과)
        val initialSeed = recentNumbers.fold(seed) { acc, num ->
            acc * 31 + num
        }

        val initialValues = generateInitialValues(initialSeed)

        // 과거 번호의 평균, 표준편차 계산
        val avg = recentNumbers.average()
        val variance = recentNumbers.map { (it - avg) * (it - avg) }.average()
        val stdDev = sqrt(variance)

        var iteration = 0
        while (numbers.size < 6) {
            // 로렌츠 어트랙터에 과거 데이터 반영
            val lorenz = lorenzAttractor(
                initialValues[0] * avg / 10,
                initialValues[1] * stdDev / 10,
                initialValues[2] * 10,
                1000 + iteration * 50
            )

            // 로지스틱 맵 적용
            val logistic = logisticMap(
                (abs(lorenz.x) % 1.0).coerceIn(0.01, 0.99),
                100 + iteration
            )

            // Hénon 맵으로 최종 변환
            val henon = henonMap(
                lorenz.y / 20.0,
                lorenz.z / 20.0,
                50 + iteration
            )

            // 모든 카오스 값 조합
            val combined = (logistic + abs(henon.x) + abs(henon.y)) / 3.0
            val number = (combined * 45).toInt() + 1

            if (number in 1..45) {
                numbers.add(number)
            }

            iteration++

            if (iteration > 1000) {
                while (numbers.size < 6) {
                    numbers.add((1..45).random())
                }
                break
            }
        }

        return numbers.sorted()
    }

    /**
     * 여러 세트의 번호 생성
     */
    fun generateMultipleSets(count: Int, useHistory: Boolean = false, recentNumbers: List<Int> = emptyList()): List<List<Int>> {
        return List(count) { index ->
            val generator = ChaosTheoryLottoGenerator(seed + index * 1000L)
            if (useHistory && recentNumbers.isNotEmpty()) {
                generator.generateNumbersWithHistory(recentNumbers)
            } else {
                generator.generateNumbers()
            }
        }
    }
}
