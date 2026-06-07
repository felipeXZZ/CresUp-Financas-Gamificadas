package com.cresup.app.presentation.ui.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.cresup.app.R
import com.cresup.app.domain.model.*
import com.cresup.app.presentation.ui.components.*
import com.cresup.app.presentation.ui.theme.*
import com.cresup.app.presentation.viewmodel.DashboardViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val user = state.user

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            DashboardHeader(user = user, balance = state.totalBalance)
        }
        item {
            XPLevelCard(user = user)
        }
        item {
            MonthSummaryRow(
                income = state.monthlyIncome,
                expenses = state.monthlyExpenses,
                savings = state.savings
            )
        }
        if (!state.quote.isNullOrBlank() && !state.isLoadingQuote) {
            item {
                QuoteCard(quote = state.quote)
            }
        }
        if (state.activeGoal != null) {
            item {
                ActiveGoalCard(goal = state.activeGoal!!)
            }
        }
        if (state.recentTransactions.isNotEmpty()) {
            item {
                SectionHeader(title = "Últimas Transações")
            }
            items(state.recentTransactions) { tx ->
                TransactionItem(transaction = tx)
            }
        } else {
            item {
                EmptyTransactionsCard()
            }
        }
    }
}

@Composable
private fun DashboardHeader(user: User, balance: Double) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GreenDark.copy(alpha = 0.25f), Background),
                    endY = 450f
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StreakBadge(streakDays = user.streakDays)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Olá, ${user.name} ${user.avatarEmoji}",
                fontSize = 14.sp,
                color = TextSecondary
            )
            Text(
                text = "Seu Saldo",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = formatCurrency(balance),
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = if (balance >= 0) NeonGreen else AccentRed
            )
            if (balance > 0) {
                Text(
                    text = "Você está no positivo! 🎉",
                    fontSize = 12.sp,
                    color = NeonGreen.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun XPLevelCard(user: User) {
    val animatedProgress by animateFloatAsState(
        targetValue = user.levelProgress(),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "xp_progress"
    )

    GlassCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularLevelIndicator(progress = animatedProgress, level = user.level)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.levelName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = levelColor(user.level)
                )
                Text(
                    text = "${user.xp} / ${user.xpToNextLevel} XP",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = levelColor(user.level),
                    trackColor = CardBorder,
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun CircularLevelIndicator(progress: Float, level: Int) {
    Box(
        modifier = Modifier.size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = CardBorder,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(NeonGreen, NeonBlue, NeonGreen)),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "$level",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = levelColor(level)
        )
    }
}

@Composable
private fun MonthSummaryRow(income: Double, expenses: Double, savings: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryChip(
            label = "Receitas",
            value = formatCurrency(income),
            color = NeonGreen,
            emoji = "📈",
            modifier = Modifier.weight(1f)
        )
        SummaryChip(
            label = "Gastos",
            value = formatCurrency(expenses),
            color = AccentRed,
            emoji = "📉",
            modifier = Modifier.weight(1f)
        )
        SummaryChip(
            label = "Economia",
            value = formatCurrency(savings),
            color = NeonCyan,
            emoji = "💰",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryChip(
    label: String,
    value: String,
    color: Color,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = emoji, fontSize = 18.sp)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 10.sp, color = TextMuted)
    }
}

@Composable
private fun QuoteCard(quote: String) {
    GlassCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(text = "💡", fontSize = 20.sp)
            Text(
                text = "\"$quote\"",
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun ActiveGoalCard(goal: Goal) {
    val animatedProgress by animateFloatAsState(
        targetValue = goal.progress,
        animationSpec = tween(1000),
        label = "goal_progress"
    )

    GlassCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = goal.emoji, fontSize = 22.sp)
                    Column {
                        Text(
                            text = "Meta Ativa",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = goal.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
                Text(
                    text = "${goal.progressPercent}%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen
                )
            }
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = NeonGreen,
                trackColor = CardBorder,
                strokeCap = StrokeCap.Round
            )
            Text(
                text = "Faltam ${formatCurrency(goal.remaining)} de ${formatCurrency(goal.targetAmount)}",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
private fun EmptyTransactionsCard() {
    GlassCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "💳", fontSize = 40.sp)
            Text(
                text = "Nenhuma transação ainda",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "Vá em Gastos para registrar receitas e despesas",
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun StreakBadge(streakDays: Int) {
    if (streakDays == 0) return
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(AccentOrange.copy(alpha = 0.15f))
            .border(1.dp, AccentOrange.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = "🔥", fontSize = 14.sp)
        Text(
            text = "$streakDays",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = AccentOrange
        )
    }
}

fun levelColor(level: Int): Color = when (level) {
    1 -> LevelRookie
    2 -> LevelBuilder
    3 -> LevelPro
    else -> LevelElite
}

fun formatCurrency(value: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return format.format(value)
}
