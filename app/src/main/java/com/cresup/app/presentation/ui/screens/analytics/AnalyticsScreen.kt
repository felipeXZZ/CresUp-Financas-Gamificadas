package com.cresup.app.presentation.ui.screens.analytics

import androidx.compose.animation.core.*
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
import com.cresup.app.domain.model.TransactionCategory
import com.cresup.app.presentation.ui.components.GlassCard
import com.cresup.app.presentation.ui.screens.dashboard.formatCurrency
import com.cresup.app.presentation.ui.theme.*
import com.cresup.app.presentation.viewmodel.AnalyticsViewModel
import com.cresup.app.presentation.viewmodel.CategorySpend
import com.cresup.app.presentation.viewmodel.FinancialInsight
import com.cresup.app.presentation.viewmodel.InsightType
import kotlin.math.abs

@Composable
fun AnalyticsScreen(
    onBack: () -> Unit = {},
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item { AnalyticsHeader(onBack = onBack) }

        item {
            SummaryCards(
                income = state.monthlyIncome,
                expenses = state.monthlyExpenses,
                savingsRate = state.savingsRate,
                dailyAverage = state.dailyAverage,
                trendPercent = state.trendPercent
            )
        }

        if (state.categoryBreakdown.isNotEmpty()) {
            item {
                CategoryBreakdownCard(categories = state.categoryBreakdown)
            }
        }

        if (state.insights.isNotEmpty()) {
            item {
                Text(
                    text = "Insights Financeiros",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
            items(state.insights) { insight ->
                InsightCard(insight = insight)
            }
        }

        item {
            StatsFooter(
                totalTransactions = state.totalTransactions,
                streakDays = state.streakDays,
                topCategory = state.topCategory
            )
        }
    }
}

@Composable
private fun AnalyticsHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NeonGreen.copy(alpha = 0.12f), Background),
                    endY = 300f
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CardBackground)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar", tint = TextPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Análises",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
            Text(
                text = "Visão detalhada das suas finanças",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun SummaryCards(
    income: Double,
    expenses: Double,
    savingsRate: Double,
    dailyAverage: Double,
    trendPercent: Double
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard(
                icon = Icons.Filled.TrendingUp,
                iconColor = NeonGreen,
                label = "Receita",
                value = formatCurrency(income),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                icon = Icons.Filled.TrendingDown,
                iconColor = AccentRed,
                label = "Gastos",
                value = formatCurrency(expenses),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard(
                icon = Icons.Filled.Savings,
                iconColor = NeonGreen,
                label = "Taxa de Economia",
                value = "${savingsRate.toInt()}%",
                subtitle = if (savingsRate >= 20) "Excelente!" else "Meta: 20%+",
                subtitleColor = if (savingsRate >= 20) NeonGreen else AccentYellow,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                icon = Icons.Filled.CalendarToday,
                iconColor = NeonGreenDim,
                label = "Média Diária",
                value = formatCurrency(dailyAverage),
                subtitle = if (trendPercent == 0.0) "—"
                           else "${if (trendPercent > 0) "+" else ""}${trendPercent.toInt()}% vs mês ant.",
                subtitleColor = when {
                    trendPercent > 10 -> AccentRed
                    trendPercent < -5 -> NeonGreen
                    else -> TextMuted
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    subtitle: String? = null,
    subtitleColor: Color = TextMuted,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextPrimary)
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
        if (subtitle != null) {
            Text(text = subtitle, fontSize = 10.sp, color = subtitleColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CategoryBreakdownCard(categories: List<CategorySpend>) {
    GlassCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gastos por Categoria",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Icon(
                    Icons.Filled.PieChart,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            categories.forEach { spend ->
                val animatedFraction by animateFloatAsState(
                    targetValue = spend.fraction,
                    animationSpec = tween(800, easing = FastOutSlowInEasing),
                    label = "bar_${spend.label}"
                )
                val color = categoryColor(spend.colorKey)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Text(spend.label, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${(spend.fraction * 100).toInt()}%",
                                fontSize = 11.sp,
                                color = color,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                formatCurrency(spend.amount),
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    LinearProgressIndicator(
                        progress = { animatedFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = color,
                        trackColor = CardBorder,
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightCard(insight: FinancialInsight) {
    val (bgColor, iconColor, icon) = when (insight.type) {
        InsightType.POSITIVE -> Triple(NeonGreen.copy(0.08f), NeonGreen, Icons.Filled.CheckCircle)
        InsightType.WARNING  -> Triple(AccentRed.copy(0.08f),  AccentRed,  Icons.Filled.Warning)
        InsightType.NEUTRAL  -> Triple(CardBorder.copy(0.5f),  TextMuted,  Icons.Filled.Info)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, iconColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        Text(
            text = insight.message,
            fontSize = 13.sp,
            color = TextSecondary,
            lineHeight = 19.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatsFooter(totalTransactions: Int, streakDays: Int, topCategory: String) {
    GlassCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Estatísticas Gerais", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatRow(icon = Icons.Filled.Receipt, label = "Total de transações", value = "$totalTransactions")
                StatRow(icon = Icons.Filled.LocalFireDepartment, label = "Streak atual", value = "${streakDays}d")
            }
            if (topCategory.isNotEmpty()) {
                StatRow(
                    icon = Icons.Filled.Category,
                    label = "Maior gasto",
                    value = topCategory
                )
            }
        }
    }
}

@Composable
private fun StatRow(icon: ImageVector, label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
        Column {
            Text(label, fontSize = 11.sp, color = TextMuted)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}

fun categoryColor(colorKey: String): Color = when (colorKey) {
    "FOOD"        -> ColorFood
    "DELIVERY"    -> ColorDelivery
    "TRANSPORT"   -> ColorTransport
    "STUDIES"     -> ColorStudies
    "GYM"         -> ColorGym
    "LEISURE"     -> ColorLeisure
    "STREAMING"   -> ColorStreaming
    "INVESTMENTS" -> ColorInvestments
    "SHOPPING"    -> ColorShopping
    "SALARY"      -> NeonGreen
    else          -> TextMuted
}
