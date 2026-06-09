package com.cresup.app.presentation.ui.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cresup.app.domain.model.*
import com.cresup.app.presentation.ui.components.*
import com.cresup.app.presentation.ui.screens.analytics.categoryColor
import com.cresup.app.presentation.ui.theme.*
import com.cresup.app.presentation.viewmodel.DashboardViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    onNavigateToAnalytics: () -> Unit = {},
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
        if (state.categoryBreakdown.isNotEmpty()) {
            item {
                SpendingBreakdownCard(
                    breakdown = state.categoryBreakdown,
                    onViewAnalytics = onNavigateToAnalytics
                )
            }
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Últimas Transações",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Icon(
                        Icons.Filled.Receipt,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
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
                    colors = listOf(GreenDark.copy(alpha = 0.20f), Background),
                    endY = 480f
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NeonGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Olá, ${user.name}",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = user.levelName,
                            fontSize = 11.sp,
                            color = NeonGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                StreakBadge(streakDays = user.streakDays)
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Patrimônio Total",
                fontSize = 13.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = formatCurrency(balance),
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                color = if (balance >= 0) NeonGreen else AccentRed
            )
            if (balance >= 0) {
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Filled.TrendingUp,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Saldo positivo",
                        fontSize = 12.sp,
                        color = NeonGreen.copy(alpha = 0.8f)
                    )
                }
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
            CircularLevelIndicator(progress = animatedProgress, level = user.level)

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.levelName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = levelColor(user.level)
                    )
                    Text(
                        text = "Nível ${user.level}",
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${user.xp} / ${user.xpToNextLevel} XP",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.height(8.dp))
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
        modifier = Modifier.size(60.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = CardBorder,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 7f, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(NeonGreen, NeonGreenDim, NeonGreen)),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = 7f, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "$level",
            fontSize = 22.sp,
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
            icon = Icons.Filled.TrendingUp,
            modifier = Modifier.weight(1f)
        )
        SummaryChip(
            label = "Gastos",
            value = formatCurrency(expenses),
            color = AccentRed,
            icon = Icons.Filled.TrendingDown,
            modifier = Modifier.weight(1f)
        )
        SummaryChip(
            label = "Economia",
            value = formatCurrency(savings),
            color = NeonGreenDim,
            icon = Icons.Filled.Savings,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryChip(
    label: String,
    value: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(text = value, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color, maxLines = 1)
        Text(text = label, fontSize = 9.sp, color = TextMuted)
    }
}

@Composable
private fun SpendingBreakdownCard(
    breakdown: Map<String, Double>,
    onViewAnalytics: () -> Unit
) {
    val total = breakdown.values.sum().takeIf { it > 0 } ?: return
    val top4 = breakdown.entries.sortedByDescending { it.value }.take(4)

    GlassCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gastos por Categoria",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                TextButton(
                    onClick = onViewAnalytics,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Ver análise",
                        fontSize = 12.sp,
                        color = NeonGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            top4.forEach { (catLabel, amount) ->
                val fraction = (amount / total).toFloat()
                val animatedFraction by animateFloatAsState(
                    targetValue = fraction,
                    animationSpec = tween(700, easing = FastOutSlowInEasing),
                    label = "cat_$catLabel"
                )
                val catKey = TransactionCategory.values()
                    .firstOrNull { it.label == catLabel }?.name ?: "OTHER"
                val color = categoryColor(catKey)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Text(
                        text = catLabel,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.width(88.dp),
                        maxLines = 1
                    )
                    LinearProgressIndicator(
                        progress = { animatedFraction },
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(CircleShape),
                        color = color,
                        trackColor = CardBorder,
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        text = "${(fraction * 100).toInt()}%",
                        fontSize = 11.sp,
                        color = color,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuoteCard(quote: String) {
    GlassCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Filled.Lightbulb,
                contentDescription = null,
                tint = AccentYellow,
                modifier = Modifier.size(20.dp)
            )
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(NeonGreen.copy(0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Flag,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
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
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
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
private fun EmptyTransactionsCard() {
    GlassCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(CardBorder),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.CreditCard,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(32.dp)
                )
            }
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
            .background(AccentOrange.copy(alpha = 0.12f))
            .border(1.dp, AccentOrange.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            Icons.Filled.LocalFireDepartment,
            contentDescription = null,
            tint = AccentOrange,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "${streakDays}d",
            fontSize = 13.sp,
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
