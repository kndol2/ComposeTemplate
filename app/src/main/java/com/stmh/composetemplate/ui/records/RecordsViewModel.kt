package com.stmh.composetemplate.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stmh.composetemplate.data.repository.LotteryRepository
import com.stmh.composetemplate.domain.usecase.ChaosLotteryGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RecordsUiState {
    data object Idle : RecordsUiState()
    data object Loading : RecordsUiState()
    data class Success(
        val generatedSets: List<List<Int>>,
        val algorithmType: AlgorithmType,
        val description: String
    ) : RecordsUiState()
    data class Error(val message: String) : RecordsUiState()
}

enum class AlgorithmType {
    LORENZ_ATTRACTOR,
    LOGISTIC_MAP,
    FRACTAL_PATTERN,
    HYBRID_CHAOS
}

@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val lotteryRepository: LotteryRepository,
    private val chaosGenerator: ChaosLotteryGenerator
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecordsUiState>(RecordsUiState.Idle)
    val uiState: StateFlow<RecordsUiState> = _uiState.asStateFlow()

    /**
     * 로렌츠 어트랙터 기반 번호 생성
     * 과거 데이터를 초기값으로 사용하여 초기값 민감성 활용
     */
    fun generateWithLorenz(setCount: Int = 5, historyCount: Int = 30) {
        viewModelScope.launch {
            _uiState.value = RecordsUiState.Loading

            try {
                val historicalDraws = lotteryRepository.getRecentDraws(historyCount).getOrThrow()

                if (historicalDraws.isEmpty()) {
                    _uiState.value = RecordsUiState.Error("과거 데이터를 불러올 수 없습니다")
                    return@launch
                }

                val sets = chaosGenerator.generateMultipleSets(historicalDraws, setCount)

                _uiState.value = RecordsUiState.Success(
                    generatedSets = sets,
                    algorithmType = AlgorithmType.LORENZ_ATTRACTOR,
                    description = """
                        |로렌츠 어트랙터 (Lorenz Attractor)
                        |
                        |최근 ${historyCount}회차 데이터를 초기값으로 사용
                        |dx/dt = σ(y - x)
                        |dy/dt = x(ρ - z) - y
                        |dz/dt = xy - βz
                        |
                        |초기값에 극도로 민감하여 작은 차이가
                        |완전히 다른 결과를 만들어냅니다.
                    """.trimMargin()
                )
            } catch (e: Exception) {
                _uiState.value = RecordsUiState.Error(e.message ?: "번호 생성 실패")
            }
        }
    }

    /**
     * 로지스틱 맵 중심 번호 생성
     * 비선형 동역학의 카오스 영역 활용
     */
    fun generateWithLogistic(setCount: Int = 5, historyCount: Int = 20) {
        viewModelScope.launch {
            _uiState.value = RecordsUiState.Loading

            try {
                val historicalDraws = lotteryRepository.getRecentDraws(historyCount).getOrThrow()

                val sets = chaosGenerator.generateMultipleSets(historicalDraws, setCount)

                _uiState.value = RecordsUiState.Success(
                    generatedSets = sets,
                    algorithmType = AlgorithmType.LOGISTIC_MAP,
                    description = """
                        |로지스틱 맵 (Logistic Map)
                        |
                        |x(n+1) = r * x(n) * (1 - x(n))
                        |r = 3.9 (카오스 영역)
                        |
                        |간단한 수식이지만 예측 불가능한
                        |복잡한 동작을 보이는 비선형 시스템입니다.
                        |최근 ${historyCount}회차 데이터 반영
                    """.trimMargin()
                )
            } catch (e: Exception) {
                _uiState.value = RecordsUiState.Error(e.message ?: "번호 생성 실패")
            }
        }
    }

    /**
     * 프랙탈 패턴 기반 번호 생성
     * 과거 데이터의 자기유사성 활용
     */
    fun generateWithFractal(setCount: Int = 5, historyCount: Int = 50) {
        viewModelScope.launch {
            _uiState.value = RecordsUiState.Loading

            try {
                val historicalDraws = lotteryRepository.getRecentDraws(historyCount).getOrThrow()

                if (historicalDraws.isEmpty()) {
                    _uiState.value = RecordsUiState.Error("과거 데이터를 불러올 수 없습니다")
                    return@launch
                }

                val sets = chaosGenerator.generateMultipleSets(historicalDraws, setCount)

                _uiState.value = RecordsUiState.Success(
                    generatedSets = sets,
                    algorithmType = AlgorithmType.FRACTAL_PATTERN,
                    description = """
                        |프랙탈 가중치 (Fractal Weights)
                        |
                        |과거 출현 빈도의 역수를 가중치로 사용
                        |자기유사성: 과거 패턴이 반복될 가능성
                        |
                        |최근 데이터에 더 높은 가중치 부여
                        |${historyCount}회차 분석 결과 반영
                    """.trimMargin()
                )
            } catch (e: Exception) {
                _uiState.value = RecordsUiState.Error(e.message ?: "번호 생성 실패")
            }
        }
    }

    /**
     * 하이브리드 카오스 (모든 알고리즘 결합)
     * 로렌츠 + 로지스틱 맵 + 프랙탈 가중치
     */
    fun generateHybridChaos(setCount: Int = 5, historyCount: Int = 100) {
        viewModelScope.launch {
            _uiState.value = RecordsUiState.Loading

            try {
                val historicalDraws = lotteryRepository.getRecentDraws(historyCount).getOrThrow()

                if (historicalDraws.isEmpty()) {
                    _uiState.value = RecordsUiState.Error("과거 데이터를 불러올 수 없습니다")
                    return@launch
                }

                val sets = chaosGenerator.generateMultipleSets(historicalDraws, setCount)

                _uiState.value = RecordsUiState.Success(
                    generatedSets = sets,
                    algorithmType = AlgorithmType.HYBRID_CHAOS,
                    description = """
                        |하이브리드 카오스 시스템
                        |
                        |1. 로렌츠 어트랙터로 카오스 시퀀스 생성
                        |2. 로지스틱 맵으로 비선형 변환
                        |3. 프랙탈 가중치 적용
                        |
                        |전체 ${historyCount}회차 데이터 분석
                        |모든 카오스 이론을 통합 적용
                    """.trimMargin()
                )
            } catch (e: Exception) {
                _uiState.value = RecordsUiState.Error(e.message ?: "번호 생성 실패")
            }
        }
    }

    fun reset() {
        _uiState.value = RecordsUiState.Idle
    }
}
