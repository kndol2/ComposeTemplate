package com.stmh.composetemplate.domain.usecase

import com.stmh.composetemplate.data.model.LotteryDraw
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 카오스 이론을 적용한 로또 번호 생성기
 *
 * 주요 카오스 이론 개념:
 * 1. 로렌츠 어트랙터(Lorenz Attractor): 초기값 민감성
 * 2. 로지스틱 맵(Logistic Map): 비선형 동역학
 * 3. 프랙탈 패턴: 과거 데이터의 자기유사성
 */
class ChaosLotteryGenerator {

    /**
     * 로렌츠 시스템 파라미터
     */
    private val sigma = 10.0
    private val rho = 28.0
    private val beta = 8.0 / 3.0

    /**
     * 로지스틱 맵 파라미터 (카오스 영역: 3.57 < r < 4)
     */
    private val logisticR = 3.9

    /**
     * 과거 당첨 데이터를 기반으로 카오스 이론 적용하여 번호 생성
     */
    fun generateNumbers(
        historicalDraws: List<LotteryDraw>,
        seed: Long = System.currentTimeMillis()
    ): List<Int> {
        if (historicalDraws.isEmpty()) {
            return generateRandomNumbers(seed)
        }

        // 1. 초기값 설정 (과거 데이터 + 시드)
        val initialState = calculateInitialState(historicalDraws, seed)

        // 2. 로렌츠 어트랙터로 카오스 시퀀스 생성
        val chaosSequence = generateLorenzSequence(initialState, 1000)

        // 3. 로지스틱 맵으로 추가 비선형성 부여
        val transformedSequence = applyLogisticMap(chaosSequence)

        // 4. 프랙탈 가중치 적용 (과거 패턴 반영)
        val weights = calculateFractalWeights(historicalDraws)

        // 5. 카오스 시퀀스를 1-45 범위의 번호로 변환
        return selectNumbersFromChaos(transformedSequence, weights)
    }

    /**
     * 과거 데이터로부터 로렌츠 시스템의 초기값 계산
     */
    private fun calculateInitialState(
        historicalDraws: List<LotteryDraw>,
        seed: Long
    ): Triple<Double, Double, Double> {
        val recentDraws = historicalDraws.takeLast(10)

        val avgX = recentDraws.flatMap { it.getWinningNumbers() }
            .average() / 45.0

        val variance = recentDraws.map { draw ->
            val numbers = draw.getWinningNumbers()
            numbers.max() - numbers.min()
        }.average() / 45.0

        val seedNormalized = (seed % 1000) / 1000.0

        return Triple(
            avgX * 20.0 - 10.0,
            variance * 20.0 - 10.0,
            seedNormalized * 20.0
        )
    }

    /**
     * 로렌츠 어트랙터를 이용한 카오스 시퀀스 생성
     * dx/dt = σ(y - x)
     * dy/dt = x(ρ - z) - y
     * dz/dt = xy - βz
     */
    private fun generateLorenzSequence(
        initialState: Triple<Double, Double, Double>,
        iterations: Int
    ): List<Triple<Double, Double, Double>> {
        val sequence = mutableListOf<Triple<Double, Double, Double>>()
        var (x, y, z) = initialState
        val dt = 0.01

        repeat(iterations) {
            val dx = sigma * (y - x)
            val dy = x * (rho - z) - y
            val dz = x * y - beta * z

            x += dx * dt
            y += dy * dt
            z += dz * dt

            sequence.add(Triple(x, y, z))
        }

        return sequence
    }

    /**
     * 로지스틱 맵 적용 (비선형 변환)
     * x_(n+1) = r * x_n * (1 - x_n)
     */
    private fun applyLogisticMap(sequence: List<Triple<Double, Double, Double>>): List<Double> {
        return sequence.map { (x, y, z) ->
            // 로렌츠 값을 [0, 1] 범위로 정규화
            val normalized = (abs(x) + abs(y) + abs(z)) / 100.0 % 1.0

            // 로지스틱 맵 적용
            var value = normalized
            repeat(5) {
                value = logisticR * value * (1 - value)
            }
            value
        }
    }

    /**
     * 프랙탈 가중치 계산 (과거 데이터의 출현 빈도 기반)
     * 자기유사성 원리: 과거 패턴이 미래에도 반복될 가능성
     */
    private fun calculateFractalWeights(historicalDraws: List<LotteryDraw>): Map<Int, Double> {
        val frequency = mutableMapOf<Int, Int>()

        // 최근 데이터에 더 높은 가중치
        historicalDraws.reversed().forEachIndexed { index, draw ->
            val weight = 1.0 / (1.0 + index / 10.0)
            draw.getWinningNumbers().forEach { number ->
                frequency[number] = frequency.getOrDefault(number, 0) + weight.toInt()
            }
        }

        // 정규화 (역빈도 가중치: 적게 나온 번호에 높은 가중치)
        val maxFreq = frequency.values.maxOrNull() ?: 1
        return (1..45).associateWith { number ->
            val freq = frequency[number] ?: 0
            1.0 - (freq.toDouble() / maxFreq.toDouble())
        }
    }

    /**
     * 카오스 시퀀스로부터 최종 번호 선택
     */
    private fun selectNumbersFromChaos(
        chaosSequence: List<Double>,
        weights: Map<Int, Double>
    ): List<Int> {
        val selectedNumbers = mutableSetOf<Int>()
        var index = 0

        while (selectedNumbers.size < 6 && index < chaosSequence.size) {
            val chaosValue = chaosSequence[index]

            // 카오스 값을 가중치와 결합
            val weightedNumbers = (1..45).map { number ->
                val baseWeight = weights[number] ?: 0.5
                val chaosWeight = abs(sin(chaosValue * number * 3.14159))
                number to (baseWeight * chaosWeight)
            }.sortedByDescending { it.second }

            // 상위 후보 중에서 카오스 값으로 선택
            val candidateIndex = (chaosValue * 10).toInt() % minOf(10, weightedNumbers.size)
            val selectedNumber = weightedNumbers[candidateIndex].first

            if (selectedNumber !in selectedNumbers) {
                selectedNumbers.add(selectedNumber)
            }

            index++
        }

        // 6개 미만이면 랜덤으로 채우기
        while (selectedNumbers.size < 6) {
            val number = ((chaosSequence[index % chaosSequence.size] * 45).toInt() % 45) + 1
            selectedNumbers.add(number)
            index++
        }

        return selectedNumbers.sorted()
    }

    /**
     * 백업: 과거 데이터가 없을 때 순수 카오스 기반 생성
     */
    private fun generateRandomNumbers(seed: Long): List<Int> {
        val initialState = Triple(
            (seed % 100) / 10.0,
            ((seed / 100) % 100) / 10.0,
            ((seed / 10000) % 100) / 10.0
        )

        val sequence = generateLorenzSequence(initialState, 500)
        val transformed = applyLogisticMap(sequence)

        val numbers = mutableSetOf<Int>()
        var index = 0

        while (numbers.size < 6 && index < transformed.size) {
            val number = ((transformed[index] * 45).toInt() % 45) + 1
            numbers.add(number)
            index++
        }

        return numbers.sorted()
    }

    /**
     * 여러 세트 생성
     */
    fun generateMultipleSets(
        historicalDraws: List<LotteryDraw>,
        count: Int
    ): List<List<Int>> {
        return (0 until count).map { i ->
            val seed = System.currentTimeMillis() + i * 1000L
            generateNumbers(historicalDraws, seed)
        }
    }
}
