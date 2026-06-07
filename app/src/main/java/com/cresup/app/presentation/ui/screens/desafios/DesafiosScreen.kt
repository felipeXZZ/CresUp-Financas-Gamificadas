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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cresup.app.domain.model.Challenge
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
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Desafios",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = "${state.challenges.count { it.isCompleted }} concluídos de ${state.challenges.size}",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
            }

            item {
                // Active challenges summary
                val active = state.challenges.filter { it.isActive && !it.isCompleted }
                if (active.isNotEmpty()) {
                    GlassCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Em Progresso 🔥", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentOrange)
                            active.forEach { ch ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${ch.emoji} ${ch.title}", fontSize = 13.sp, color = TextPrimary)
                                    Text(
                                        "${ch.progressCurrent}/${ch.durationDays}d",
                                        fontSize = 13.sp,
                                        color = NeonGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
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
                                    challenge.isCompleted -> AccentYellow.copy(0.2f)
                                    challenge.isActive -> NeonGreen.copy(0.15f)
                                    else -> CardBorder
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = challenge.emoji, fontSize = 22.sp)
                    }
                    Column {
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
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "+${challenge.xpReward}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentYellow
                    )
                    Text(text = "XP", fontSize = 10.sp, color = TextMuted)
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
                            .height(8.dp)
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏆 Desafio Concluído!", fontSize = 14.sp, color = AccentYellow, fontWeight = FontWeight.Bold)
                    }
                }
                challenge.isActive -> {
                    Button(
                        onClick = onProgress,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Registrar Progresso (+1 dia)", color = Background, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    OutlinedButton(
                        onClick = onActivate,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen),
                        border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.5f))
                    ) {
                        Text("Aceitar Desafio ⚡")
                    }
                }
            }
        }
    }
}
