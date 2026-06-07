package com.cresup.app.presentation.ui.screens.metas

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cresup.app.domain.model.Goal
import com.cresup.app.domain.model.GoalPresets
import com.cresup.app.presentation.ui.components.CurrencyVisualTransformation
import com.cresup.app.presentation.ui.components.GlassCard
import com.cresup.app.presentation.ui.screens.gastos.dialogFieldColors
import com.cresup.app.presentation.ui.screens.dashboard.formatCurrency
import com.cresup.app.presentation.ui.theme.*
import com.cresup.app.presentation.viewmodel.MetasViewModel

@Composable
fun MetasScreen(viewModel: MetasViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage, state.successMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
        state.successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = NeonGreen,
                contentColor = Background,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nova meta")
            }
        },
        containerColor = Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Metas",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = "${state.goals.count { it.isCompleted }}/${state.goals.size} concluídas",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            if (state.goals.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎯", fontSize = 56.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Nenhuma meta ainda",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                "Crie sua primeira meta financeira",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            } else {
                items(state.goals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        onContribute = { viewModel.showContributeDialog(goal) },
                        onDelete = { viewModel.deleteGoal(goal) }
                    )
                }
            }
        }
    }

    if (state.isAddingGoal) {
        AddGoalDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { title, emoji, target -> viewModel.addGoal(title, emoji, target) }
        )
    }

    state.contributingToGoal?.let { goal ->
        ContributeDialog(
            goal = goal,
            onDismiss = { viewModel.hideContributeDialog() },
            onConfirm = { amount -> viewModel.contribute(goal.id, amount) }
        )
    }
}

@Composable
private fun GoalCard(goal: Goal, onContribute: () -> Unit, onDelete: () -> Unit) {
    val animatedProgress by animateFloatAsState(
        targetValue = goal.progress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "goal_progress"
    )

    GlassCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = goal.emoji, fontSize = 28.sp)
                    Column {
                        Text(
                            text = goal.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${formatCurrency(goal.currentAmount)} de ${formatCurrency(goal.targetAmount)}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (goal.isCompleted) {
                        Text(text = "✅", fontSize = 20.sp)
                    } else {
                        Text(
                            text = "${goal.progressPercent}%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.Close, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = if (goal.isCompleted) AccentYellow else NeonGreen,
                trackColor = CardBorder,
                strokeCap = StrokeCap.Round
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (goal.isCompleted) "Meta concluída! 🎉"
                    else "Faltam ${formatCurrency(goal.remaining)}",
                    fontSize = 12.sp,
                    color = if (goal.isCompleted) AccentYellow else TextSecondary
                )
                if (!goal.isCompleted) {
                    TextButton(onClick = onContribute) {
                        Text("+ Contribuir", color = NeonGreen, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("🎯") }
    var target by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceVariant,
        title = { Text("Nova Meta", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Escolha um preset:", fontSize = 12.sp, color = TextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(GoalPresets) { (emoji, label) ->
                        FilterChip(
                            selected = selectedEmoji == emoji,
                            onClick = { selectedEmoji = emoji; if (title.isEmpty()) title = label },
                            label = { Text("$emoji $label", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonGreen,
                                selectedLabelColor = Background
                            )
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título da meta") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = dialogFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it.filter(Char::isDigit).trimStart('0').ifEmpty { "" } },
                    label = { Text("Valor alvo (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = CurrencyVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = dialogFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, selectedEmoji, target) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text("Criar Meta", color = Background, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = TextMuted) }
        }
    )
}

@Composable
private fun ContributeDialog(goal: Goal, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceVariant,
        title = {
            Text("Contribuir para ${goal.emoji} ${goal.title}", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter(Char::isDigit).trimStart('0').ifEmpty { "" } },
                label = { Text("Valor (R$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = CurrencyVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = dialogFieldColors(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(amount) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text("Confirmar", color = Background, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = TextMuted) }
        }
    )
}
