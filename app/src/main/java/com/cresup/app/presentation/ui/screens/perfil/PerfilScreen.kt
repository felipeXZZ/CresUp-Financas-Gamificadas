package com.cresup.app.presentation.ui.screens.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cresup.app.domain.model.Achievement
import com.cresup.app.domain.model.AchievementRarity
import com.cresup.app.domain.model.User
import com.cresup.app.domain.model.levelProgress
import com.cresup.app.presentation.ui.components.GlassCard
import com.cresup.app.presentation.ui.screens.dashboard.formatCurrency
import com.cresup.app.presentation.ui.screens.dashboard.levelColor
import com.cresup.app.presentation.ui.screens.gastos.dialogFieldColors
import com.cresup.app.presentation.ui.theme.*
import com.cresup.app.presentation.viewmodel.PerfilViewModel

@Composable
fun PerfilScreen(
    onLogout: () -> Unit = {},
    viewModel: PerfilViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessage() }
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
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item { ProfileHeader(user = state.user, onEditName = { viewModel.showEditName() }) }
            item { StatsRow(user = state.user) }

            val unlocked = state.achievements.count { it.isUnlocked }
            val total = state.achievements.size
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Conquistas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            "$unlocked / $total desbloqueadas",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    Icon(
                        Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = AccentYellow,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            items(state.achievements.chunked(3)) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { achievement ->
                        AchievementChip(achievement = achievement, modifier = Modifier.weight(1f))
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Filled.Logout, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Sair da Conta", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = SurfaceVariant,
            title = { Text("Sair da conta?", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = { Text("Você precisará fazer login novamente.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = { showLogoutDialog = false; onLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Sair", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }

    if (state.isEditingName) {
        EditNameDialog(
            currentName = state.user.name,
            onDismiss = { viewModel.hideEditName() },
            onConfirm = { viewModel.updateName(it) }
        )
    }
}

@Composable
private fun ProfileHeader(user: User, onEditName: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NeonGreen.copy(0.10f), Background),
                    endY = 520f
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(CardBackground)
                    .border(3.dp, levelColor(user.level), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = levelColor(user.level),
                    modifier = Modifier.size(44.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = user.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = onEditName, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Edit, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LevelBadge(user = user)
                if (user.coins > 0) {
                    CoinsBadge(coins = user.coins)
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${user.xp} XP", fontSize = 12.sp, color = TextSecondary)
                    Text("${user.xpToNextLevel} XP", fontSize = 12.sp, color = TextMuted)
                }
                LinearProgressIndicator(
                    progress = { user.levelProgress() },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(8.dp)
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
private fun LevelBadge(user: User) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(levelColor(user.level).copy(0.15f))
            .border(1.dp, levelColor(user.level).copy(0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.ElectricBolt,
            contentDescription = null,
            tint = levelColor(user.level),
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = user.levelName,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = levelColor(user.level)
        )
    }
}

@Composable
private fun CoinsBadge(coins: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(AccentYellow.copy(0.12f))
            .border(1.dp, AccentYellow.copy(0.35f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.MonetizationOn,
            contentDescription = null,
            tint = AccentYellow,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = "$coins",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AccentYellow
        )
    }
}

@Composable
private fun StatsRow(user: User) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(Icons.Filled.LocalFireDepartment, AccentOrange, "${user.streakDays}", "Streak", Modifier.weight(1f))
        StatCard(Icons.Filled.EmojiEvents, AccentYellow, "${user.maxStreak}", "Recorde", Modifier.weight(1f))
        StatCard(Icons.Filled.Flag, NeonGreen, "${user.goalsCompleted}", "Metas", Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    iconColor: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = TextPrimary)
            Text(text = label, fontSize = 10.sp, color = TextMuted)
        }
    }
}

@Composable
private fun AchievementChip(achievement: Achievement, modifier: Modifier = Modifier) {
    val rarityColor = when (achievement.rarity) {
        AchievementRarity.COMMON    -> TextMuted
        AchievementRarity.RARE      -> NeonGreen
        AchievementRarity.EPIC      -> AccentPink
        AchievementRarity.LEGENDARY -> AccentYellow
    }
    val activeColor = if (achievement.isUnlocked) rarityColor else TextMuted.copy(alpha = 0.3f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (achievement.isUnlocked) rarityColor.copy(0.08f) else CardBackground
            )
            .border(
                1.dp,
                if (achievement.isUnlocked) rarityColor.copy(0.35f) else CardBorder,
                RoundedCornerShape(16.dp)
            )
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(activeColor.copy(0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                achievementIcon(achievement.id),
                contentDescription = null,
                tint = activeColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = achievement.title,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (achievement.isUnlocked) TextPrimary else TextMuted,
            maxLines = 2
        )
        if (achievement.isUnlocked) {
            Text(
                text = achievement.rarity.label,
                fontSize = 8.sp,
                color = rarityColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceVariant,
        title = { Text("Editar Nome", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth(),
                colors = dialogFieldColors(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text("Salvar", color = Background, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = TextMuted) }
        }
    )
}

fun achievementIcon(achievementId: Long): ImageVector = when (achievementId) {
    1L  -> Icons.Filled.Payments
    2L  -> Icons.Filled.Savings
    3L  -> Icons.Filled.LocalFireDepartment
    4L  -> Icons.Filled.Flag
    5L  -> Icons.Filled.EmojiEvents
    6L  -> Icons.Filled.TrendingUp
    7L  -> Icons.Filled.Bolt
    8L  -> Icons.Filled.Stars
    9L  -> Icons.Filled.NoDrinks
    10L -> Icons.Filled.Diamond
    11L -> Icons.Filled.Construction
    12L -> Icons.Filled.Done
    13L -> Icons.Filled.AccountBalance
    14L -> Icons.Filled.MilitaryTech
    15L -> Icons.Filled.DirectionsRun
    else -> Icons.Filled.EmojiEvents
}
