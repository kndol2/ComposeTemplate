package com.stmh.composetemplate.ui.hospital

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stmh.composetemplate.data.model.LotteryDraw
import com.stmh.composetemplate.data.repository.LotteryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

sealed class HospitalUiState {
    data object Loading : HospitalUiState()
    data class Success(
        val recommendedNumbers: List<Int>,
        val recentDraws: List<LotteryDraw>,
        val statistics: LotteryStatistics
    ) : HospitalUiState()
    data class Error(val message: String) : HospitalUiState()
    data object Idle : HospitalUiState()
}

data class LotteryStatistics(
    val frequency: Map<Int, Int>,
    val recentFrequency: Map<Int, Int>,
    val avgGap: Map<Int, Double>
)

@HiltViewModel
class HospitalViewModel @Inject constructor(
    private val lotteryRepository: LotteryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HospitalUiState>(HospitalUiState.Idle)
    val uiState: StateFlow<HospitalUiState> = _uiState.asStateFlow()

    fun generateRandomNumbers() {
        val numbers = (1..45).shuffled(Random).take(6).sorted()
        _uiState.value = HospitalUiState.Success(
            recommendedNumbers = numbers,
            recentDraws = emptyList(),
            statistics = LotteryStatistics(emptyMap(), emptyMap(), emptyMap())
        )
    }

    fun generateAnalyzedNumbers(analysisCount: Int = 50) {
        viewModelScope.launch {
            _uiState.value = HospitalUiState.Loading

            try {
                val recentDraws = lotteryRepository.getRecentDraws(analysisCount).getOrThrow()

                if (recentDraws.isEmpty()) {
                    _uiState.value = HospitalUiState.Error("데이터를 불러올 수 없습니다")
                    return@launch
                }

                val statistics = analyzeDraws(recentDraws)
                val recommendedNumbers = generateWeightedNumbers(statistics, recentDraws)

                _uiState.value = HospitalUiState.Success(
                    recommendedNumbers = recommendedNumbers,
                    recentDraws = recentDraws,
                    statistics = statistics
                )
            } catch (e: Exception) {
                _uiState.value = HospitalUiState.Error(e.message ?: "알 수 없는 오류가 발생했습니다")
            }
        }
    }

    private fun analyzeDraws(draws: List<LotteryDraw>): LotteryStatistics {
        val frequency = mutableMapOf<Int, Int>()
        val recentFrequency = mutableMapOf<Int, Int>()
        val lastAppearance = mutableMapOf<Int, Int>()

        draws.forEachIndexed { index, draw ->
            draw.getWinningNumbers().forEach { number ->
                frequency[number] = (frequency[number] ?: 0) + 1

                if (index >= draws.size - 20) {
                    recentFrequency[number] = (recentFrequency[number] ?: 0) + 1
                }

                lastAppearance[number] = index
            }
        }

        val avgGap = (1..45).associateWith { number ->
            val appearances = draws.indices.filter { index ->
                draws[index].getWinningNumbers().contains(number)
            }

            if (appearances.size > 1) {
                val gaps = appearances.zipWithNext { a, b -> b - a }
                gaps.average()
            } else {
                draws.size.toDouble()
            }
        }

        return LotteryStatistics(
            frequency = frequency,
            recentFrequency = recentFrequency,
            avgGap = avgGap
        )
    }

    private fun generateWeightedNumbers(
        statistics: LotteryStatistics,
        draws: List<LotteryDraw>
    ): List<Int> {
        val weights = (1..45).associateWith { number ->
            val freq = statistics.frequency[number] ?: 0
            val recentFreq = statistics.recentFrequency[number] ?: 0
            val avgGap = statistics.avgGap[number] ?: 0.0

            val frequencyWeight = freq.toDouble()
            val recentWeight = recentFreq.toDouble() * 1.5
            val gapWeight = if (avgGap > 0) 1.0 / avgGap else 0.0

            frequencyWeight + recentWeight + gapWeight * 10
        }

        val selectedNumbers = mutableSetOf<Int>()
        val random = Random.Default

        while (selectedNumbers.size < 6) {
            val totalWeight = weights.filter { it.key !in selectedNumbers }.values.sum()
            var randomValue = random.nextDouble(totalWeight)

            for ((number, weight) in weights.filter { it.key !in selectedNumbers }) {
                randomValue -= weight
                if (randomValue <= 0) {
                    selectedNumbers.add(number)
                    break
                }
            }
        }

        return selectedNumbers.sorted()
    }
}
