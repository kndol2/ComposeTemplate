package com.stmh.composetemplate.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stmh.composetemplate.ui.records.AlgorithmType
import com.stmh.composetemplate.ui.records.RecordsUiState
import com.stmh.composetemplate.ui.records.RecordsViewModel

@Composable
fun RecordsScreen(
    viewModel: RecordsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 헤더
        Text(
            text = "📊 카오스 이론 분석",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Text(
            text = "과거 데이터 기반 카오스 동역학 시스템",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 알고리즘 선택 버튼들
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AlgorithmButton(
                    title = "로렌츠\n어트랙터",
                    emoji = "🌪️",
                    subtitle = "30회차",
                    onClick = { viewModel.generateWithLorenz(5, 30) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EA)
                    )
                )
                AlgorithmButton(
                    title = "로지스틱\n맵",
                    emoji = "📈",
                    subtitle = "20회차",
                    onClick = { viewModel.generateWithLogistic(5, 20) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00BFA5)
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AlgorithmButton(
                    title = "프랙탈\n패턴",
                    emoji = "🔷",
                    subtitle = "50회차",
                    onClick = { viewModel.generateWithFractal(5, 50) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6D00)
                    )
                )
                AlgorithmButton(
                    title = "하이브리드\n카오스",
                    emoji = "⚡",
                    subtitle = "100회차",
                    onClick = { viewModel.generateHybridChaos(5, 100) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD50000)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 결과 표시
        when (val state = uiState) {
            is RecordsUiState.Idle -> {
                IdleState()
            }
            is RecordsUiState.Loading -> {
                LoadingState()
            }
            is RecordsUiState.Success -> {
                SuccessState(
                    generatedSets = state.generatedSets,
                    algorithmType = state.algorithmType,
                    description = state.description
                )
            }
            is RecordsUiState.Error -> {
                ErrorState(message = state.message)
            }
        }
    }
}

@Composable
fun AlgorithmButton(
    title: String,
    emoji: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors()
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = colors
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun IdleState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎲",
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "알고리즘을 선택하세요",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "과거 당첨 데이터를 분석하여\n카오스 이론 기반 번호를 생성합니다",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "카오스 시퀀스 생성 중...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SuccessState(
    generatedSets: List<List<Int>>,
    algorithmType: AlgorithmType,
    description: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 알고리즘 설명 카드
        item {
            AlgorithmDescriptionCard(
                algorithmType = algorithmType,
                description = description
            )
        }

        // 생성된 번호 세트들
        itemsIndexed(generatedSets) { index, numbers ->
            ChaosNumberSetCard(
                setNumber = index + 1,
                numbers = numbers,
                algorithmType = algorithmType
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ErrorState(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.displaySmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "오류 발생",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AlgorithmDescriptionCard(
    algorithmType: AlgorithmType,
    description: String
) {
    val gradientColors = when (algorithmType) {
        AlgorithmType.LORENZ_ATTRACTOR -> listOf(Color(0xFF6200EA), Color(0xFF9D46FF))
        AlgorithmType.LOGISTIC_MAP -> listOf(Color(0xFF00BFA5), Color(0xFF00E5CC))
        AlgorithmType.FRACTAL_PATTERN -> listOf(Color(0xFFFF6D00), Color(0xFFFF9E40))
        AlgorithmType.HYBRID_CHAOS -> listOf(Color(0xFFD50000), Color(0xFFFF5131))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(gradientColors)
                )
                .padding(20.dp)
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight.times(1.5f)
            )
        }
    }
}

@Composable
fun ChaosNumberSetCard(
    setNumber: Int,
    numbers: List<Int>,
    algorithmType: AlgorithmType
) {
    val accentColor = when (algorithmType) {
        AlgorithmType.LORENZ_ATTRACTOR -> Color(0xFF6200EA)
        AlgorithmType.LOGISTIC_MAP -> Color(0xFF00BFA5)
        AlgorithmType.FRACTAL_PATTERN -> Color(0xFFFF6D00)
        AlgorithmType.HYBRID_CHAOS -> Color(0xFFD50000)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Set $setNumber",
                    style = MaterialTheme.typography.labelLarge,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                numbers.forEach { number ->
                    ChaosLottoNumberBall(number, accentColor)
                }
            }
        }
    }
}

@Composable
fun ChaosLottoNumberBall(number: Int, accentColor: Color) {
    val ballColor = when {
        number <= 10 -> Color(0xFFFFC107) // Yellow
        number <= 20 -> Color(0xFF2196F3) // Blue
        number <= 30 -> Color(0xFFF44336) // Red
        number <= 40 -> Color(0xFF9E9E9E) // Gray
        else -> Color(0xFF4CAF50) // Green
    }

    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(ballColor)
            .border(3.dp, accentColor.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}
