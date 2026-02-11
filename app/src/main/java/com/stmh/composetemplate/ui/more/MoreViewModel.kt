package com.stmh.composetemplate.ui.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stmh.composetemplate.data.repository.LotteryRepository
import com.stmh.composetemplate.util.ChaosTheoryLottoGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ChaosUiState {
    data object Idle : ChaosUiState()
    data object Loading : ChaosUiState()
    data class Success(
        val generatedSets: List<List<Int>>,
        val method: String,
        val description: String
    ) : ChaosUiState()
    data class Error(val message: String) : ChaosUiState()
}

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val lotteryRepository: LotteryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChaosUiState>(ChaosUiState.Idle)
    val uiState: StateFlow<ChaosUiState> = _uiState.asStateFlow()

    /**
     * 순수 카오스 이론 기반 번호 생성
     * 로지스틱 맵, 로렌츠 어트랙터, Hénon 맵 사용
     */
    fun generatePureChaos(setCount: Int = 5) {
        viewModelScope.launch {
            _uiState.value = ChaosUiState.Loading

            try {
                val generator = ChaosTheoryLottoGenerator()
                val sets = generator.generateMultipleSets(setCount)

                _uiState.value = ChaosUiState.Success(
                    generatedSets = sets,
                    method = "순수 카오스 이론",
                    description = """
                        |로지스틱 맵, 로렌츠 어트랙터, Hénon 맵을 조합
                        |초기값에 민감한 비선형 동역학 시스템 활용
                        |시간 기반 seed로 매번 다른 초기 조건 생성
                    """.trimMargin()
                )
            } catch (e: Exception) {
                _uiState.value = ChaosUiState.Error(e.message ?: "번호 생성 실패")
            }
        }
    }

    /**
     * 과거 데이터 기반 카오스 이론 (나비 효과)
     * 최근 당첨번호를 초기값으로 사용
     */
    fun generateChaosWithHistory(setCount: Int = 5, historyCount: Int = 10) {
        viewModelScope.launch {
            _uiState.value = ChaosUiState.Loading

            try {
                // DB에서 최근 회차 데이터 가져오기
                val recentDraws = lotteryRepository.getRecentDraws(historyCount).getOrThrow()

                if (recentDraws.isEmpty()) {
                    _uiState.value = ChaosUiState.Error("과거 데이터를 불러올 수 없습니다")
                    return@launch
                }

                // 최근 당첨 번호들을 모두 모음
                val recentNumbers = recentDraws.flatMap { it.getWinningNumbers() }

                val generator = ChaosTheoryLottoGenerator()
                val sets = generator.generateMultipleSets(
                    count = setCount,
                    useHistory = true,
                    recentNumbers = recentNumbers
                )

                _uiState.value = ChaosUiState.Success(
                    generatedSets = sets,
                    method = "나비 효과 (최근 ${historyCount}회차)",
                    description = """
                        |최근 당첨번호를 초기값으로 사용
                        |과거 데이터의 평균과 표준편차를 로렌츠 어트랙터에 반영
                        |작은 변화(과거 번호)가 큰 결과 차이를 만드는 나비 효과 적용
                    """.trimMargin()
                )
            } catch (e: Exception) {
                _uiState.value = ChaosUiState.Error(e.message ?: "번호 생성 실패")
            }
        }
    }

    /**
     * 프랙탈 패턴 기반 번호 생성
     */
    fun generateFractalPattern(setCount: Int = 5) {
        viewModelScope.launch {
            _uiState.value = ChaosUiState.Loading

            try {
                // 여러 시간 스케일에서 카오스 생성 (자기 유사성)
                val sets = mutableListOf<List<Int>>()

                repeat(setCount) { index ->
                    // 시간 스케일을 달리하여 프랙탈 패턴 생성
                    val scale = 1000L * (index + 1) * (index + 1)
                    val generator = ChaosTheoryLottoGenerator(System.currentTimeMillis() + scale)
                    sets.add(generator.generateNumbers())
                }

                _uiState.value = ChaosUiState.Success(
                    generatedSets = sets,
                    method = "프랙탈 패턴",
                    description = """
                        |서로 다른 시간 스케일에서 카오스 생성
                        |자기 유사성을 가진 프랙탈 패턴 적용
                        |각 세트는 다른 스케일의 초기값 사용
                    """.trimMargin()
                )
            } catch (e: Exception) {
                _uiState.value = ChaosUiState.Error(e.message ?: "번호 생성 실패")
            }
        }
    }

    /**
     * 어트랙터 수렴 패턴
     */
    fun generateAttractorPattern(setCount: Int = 5) {
        viewModelScope.launch {
            _uiState.value = ChaosUiState.Loading

            try {
                val recentDraws = lotteryRepository.getRecentDraws(20).getOrThrow()
                val recentNumbers = recentDraws.flatMap { it.getWinningNumbers() }

                // 서로 다른 초기값에서 시작하지만 같은 어트랙터로 수렴
                val sets = List(setCount) { index ->
                    val offset = index * 123456L
                    val generator = ChaosTheoryLottoGenerator(System.currentTimeMillis() + offset)

                    if (recentNumbers.isNotEmpty()) {
                        generator.generateNumbersWithHistory(recentNumbers)
                    } else {
                        generator.generateNumbers()
                    }
                }

                _uiState.value = ChaosUiState.Success(
                    generatedSets = sets,
                    method = "어트랙터 수렴",
                    description = """
                        |다른 초기값에서 시작하여 어트랙터로 수렴
                        |장기적 패턴 반영
                        |과거 데이터가 있으면 나비 효과와 결합
                    """.trimMargin()
                )
            } catch (e: Exception) {
                _uiState.value = ChaosUiState.Error(e.message ?: "번호 생성 실패")
            }
        }
    }

    fun reset() {
        _uiState.value = ChaosUiState.Idle
    }
}
