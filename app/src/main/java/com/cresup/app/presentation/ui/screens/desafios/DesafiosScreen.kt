package com.cresup.app.presentation.ui.screens.desafios

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cresup.app.domain.model.Challenge
import com.cresup.app.domain.model.ChallengeDifficulty
import com.cresup.app.presentation.ui.components.GlassCard
import com.cresup.app.presentation.ui.theme.*
import com.cresup.app.presentation.viewmodel.DesafiosViewModel

@Composable
fun DesafiosScreen(viewModel: DesafiosViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage, state.errorMessage) {
        state.successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
        state.errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                ChallengesHeader(
                    completed = state.challenges.count { it.isCompleted },
                    total = state.challenges.size
                )
            }

            val active = state.challenges.filter { it.isActive && !it.isCompleted }
            if (active.isNotEmpty()) {
                item {
                    ActiveSummaryCard(active = active)
                }
            }

            items(state.challenges, key = { it.id }) { challenge ->
                ChallengeCard(
                    challenge = challenge,
                    onActivate = { viewModel.activateChallenge(challenge) },
                    onProgress = { viewModel.progressChallenge(challenge) }
                )
            }
        }
    }
}

@Composable
private fun ChallengesHeader(completed: Int, total: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(AccentYellow.copy(alpha = 0.08f), Background),
                    endY = 240f
                )
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Desafios", fontSize = 28.sp, fontWeight = FontWeight.Black, color = TextPrimary)
            Text(
                text = "$completed de $total desafios concluídos",
                fontSize = 13.sp,
                color = TextSecondary
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (total > 0) completed.toFloat() / total else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = NeonGreen,
                trackColor = CardBorder,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun ActiveSummaryCard(active: List<Challenge>) {
    GlassCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = AccentOrange,
                    modifier = Modifier.size(16.dp)
                )
                Text("Em Progresso", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentOrange)
            }
            active.forEach { ch ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            challengeIcon(ch.id),
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(ch.title, fontSize = 13.sp, color = TextPrimary, maxLines = 1)
                    }
                    Text(
                        "${ch.progressCurrent}/${ch.durationDays}d",
                        fontSize = 12.sp,
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    challenge: Challenge,
    onActivate: () -> Unit,
    onProgress: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = challenge.progress,
        animationSpec = tween(800),
        label = "challenge_progress"
    )

    val borderColor = when {
        challenge.isCompleted -> AccentYellow
        challenge.isActive -> NeonGreen
        else -> CardBorder
    }

    GlassCard(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    challenge.isCompleted -> AccentYellow.copy(0.15f)
                                    challenge.isActive -> NeonGreen.copy(0.12f)
                                    else -> CardBorder.copy(0.5f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = challengeIcon(challenge.id),
                            contentDescription = null,
                            tint = when {
                                challenge.isCompleted -> AccentYellow
                                challenge.isActive -> NeonGreen
                                else -> TextMuted
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = challenge.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = challenge.description,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                        DifficultyBadge(difficulty = challenge.difficulty)
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "+${challenge.xpReward}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = AccentYellow
                    )
                    Text(text = "XP", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                }
            }

            if (challenge.isActive || challenge.isCompleted) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${challenge.progressCurrent} / ${challenge.durationDays} dias",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "${(challenge.progress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (challenge.isCompleted) AccentYellow else NeonGreen
                        )
                    }
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(CircleShape),
                        color = if (challenge.isCompleted) AccentYellow else NeonGreen,
                        trackColor = CardBorder,
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            when {
                challenge.isCompleted -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentYellow.copy(0.08f))
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = AccentYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Desafio Concluído!",
                            fontSize = 14.sp,
                            color = AccentYellow,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                challenge.isActive -> {
                    Button(
                        onClick = onProgress,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.AddCircle,
                            contentDescription = null,
                            tint = Background,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Registrar Progresso (+1 dia)", color = Background, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    OutlinedButton(
                        onClick = onActivate,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen),
                        border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Aceitar Desafio", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DifficultyBadge(difficulty: ChallengeDifficulty) {
    val (color, icon) = when (difficulty) {
        ChallengeDifficulty.EASY   -> NeonGreen to Icons.Filled.SignalCellularAlt1Bar
        ChallengeDifficulty.MEDIUM -> AccentYellow to Icons.Filled.SignalCellularAlt2Bar
        ChallengeDifficulty.HARD   -> AccentRed to Icons.Filled.SignalCellularAlt
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(0.1f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(10.dp))
        Text(difficulty.label, fontSize = 9.sp, color = color, fontWeight = FontWeight.Bold)
    }
}

fun challengeIcon(challengeId: Long): ImageVector = when (challengeId) {
    1L  -> Icons.Filled.NoMeals
    2L  -> Icons.Filled.Savings
    3L  -> Icons.Filled.EditNote
    4L  -> Icons.Filled.Flag
    5L  -> Icons.Filled.TrendingDown
    else -> Icons.Filled.EmojiEvents
}
