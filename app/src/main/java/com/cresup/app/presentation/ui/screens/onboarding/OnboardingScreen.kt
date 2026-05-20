package com.cresup.app.presentation.ui.screens.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cresup.app.presentation.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingPage(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val gradient: List<androidx.compose.ui.graphics.Color>
)

val onboardingPages = listOf(
    OnboardingPage(
        emoji = "📊",
        title = "Controle seus gastos",
        subtitle = "Registre receitas e despesas com categorias modernas e visualize para onde seu dinheiro vai.",
        gradient = listOf(NeonBlue.copy(0.2f), Background)
    ),
    OnboardingPage(
        emoji = "🎯",
        title = "Crie metas financeiras",
        subtitle = "Defina objetivos — viagem, setup gamer, iPhone — e acompanhe seu progresso em tempo real.",
        gradient = listOf(NeonGreen.copy(0.15f), Background)
    ),
    OnboardingPage(
        emoji = "🏆",
        title = "Evolua e ganhe XP",
        subtitle = "Ganhe experiência ao registrar gastos, bater metas e manter streaks. Suba de nível financeiramente!",
        gradient = listOf(AccentYellow.copy(0.15f), Background)
    ),
    OnboardingPage(
        emoji = "⚡",
        title = "Aceite desafios",
        subtitle = "Complete desafios financeiros, desbloqueie conquistas e transforme sua relação com o dinheiro.",
        gradient = listOf(AccentOrange.copy(0.15f), Background)
    )
)

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pagerState = rememberPagerState { onboardingPages.size }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val data = onboardingPages[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(data.gradient))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = data.emoji, fontSize = 80.sp)
                    Spacer(Modifier.height(32.dp))
                    Text(
                        text = data.title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = data.subtitle,
                        fontSize = 16.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(onboardingPages.size) { i ->
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(if (i == pagerState.currentPage) 20.dp else 8.dp, 8.dp)
                            .background(if (i == pagerState.currentPage) NeonGreen else TextMuted)
                    )
                }
            }

            if (pagerState.currentPage < onboardingPages.size - 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onComplete) {
                        Text("Pular", color = TextMuted)
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Próximo", color = Background, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = onComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Vamos Começar! 🚀",
                        color = Background,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
