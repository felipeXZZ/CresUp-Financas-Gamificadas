package com.cresup.app.presentation.ui.screens.gastos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cresup.app.domain.model.Transaction
import com.cresup.app.domain.model.TransactionCategory
import com.cresup.app.domain.model.TransactionType
import com.cresup.app.presentation.ui.components.CurrencyVisualTransformation
import com.cresup.app.presentation.ui.components.GlassCard
import com.cresup.app.presentation.ui.components.TransactionItem
import com.cresup.app.presentation.ui.screens.dashboard.formatCurrency
import com.cresup.app.presentation.ui.theme.*
import com.cresup.app.presentation.viewmodel.GastosViewModel

@Composable
fun GastosScreen(viewModel: GastosViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage, state.successMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
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
                Icon(Icons.Filled.Add, contentDescription = "Adicionar")
            }
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gastos",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Text(
                    text = "${state.filteredTransactions.size} transações",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            // Search
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearch(it) },
                placeholder = { Text("Buscar transações...", color = TextMuted, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = TextMuted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = CardBackground,
                    focusedContainerColor = CardBackground,
                    unfocusedBorderColor = CardBorder,
                    focusedBorderColor = NeonGreen,
                    unfocusedTextColor = TextPrimary,
                    focusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            // Type filters
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.selectedType == null,
                        onClick = { viewModel.setTypeFilter(null) },
                        label = { Text("Todos") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonGreen,
                            selectedLabelColor = Background
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = state.selectedType == TransactionType.INCOME,
                        onClick = { viewModel.setTypeFilter(TransactionType.INCOME) },
                        label = { Text("Receitas") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonGreen,
                            selectedLabelColor = Background
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = state.selectedType == TransactionType.EXPENSE,
                        onClick = { viewModel.setTypeFilter(TransactionType.EXPENSE) },
                        label = { Text("Despesas") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentRed,
                            selectedLabelColor = TextPrimary
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (state.filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💸", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Nenhuma transação encontrada",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                    items(state.filteredTransactions, key = { it.id }) { tx ->
                        SwipeToDeleteTransaction(
                            transaction = tx,
                            onDelete = { viewModel.deleteTransaction(tx) }
                        )
                    }
                }
            }
        }
    }

    if (state.isAddingTransaction) {
        AddTransactionDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { title, amount, type, category, note ->
                viewModel.addTransaction(title, amount, type, category, note)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteTransaction(
    transaction: Transaction,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AccentRed.copy(alpha = 0.3f)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = null,
                    tint = AccentRed,
                    modifier = Modifier.padding(end = 20.dp)
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        TransactionItem(transaction = transaction)
    }
}

@Composable
private fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, TransactionType, TransactionCategory, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var category by remember { mutableStateOf(TransactionCategory.FOOD) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceVariant,
        title = {
            Text(
                text = "Nova Transação",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Type toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransactionType.values().forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = {
                                Text(if (t == TransactionType.EXPENSE) "Despesa" else "Receita")
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (t == TransactionType.EXPENSE) AccentRed else NeonGreen,
                                selectedLabelColor = if (t == TransactionType.EXPENSE) TextPrimary else Background
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = dialogFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

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

                // Category picker
                Text("Categoria", fontSize = 12.sp, color = TextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val cats = if (type == TransactionType.EXPENSE)
                        TransactionCategory.values().filter { it != TransactionCategory.SALARY }
                    else listOf(TransactionCategory.SALARY, TransactionCategory.INVESTMENTS)

                    items(cats) { cat ->
                        CategoryChip(
                            category = cat,
                            selected = category == cat,
                            onClick = { category = cat }
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Observação (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = dialogFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, amount, type, category, note) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text("Adicionar", color = Background, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextMuted)
            }
        }
    )
}

@Composable
private fun CategoryChip(
    category: TransactionCategory,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) category.color.copy(0.3f) else CardBackground)
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = category.emoji, fontSize = 18.sp)
        Text(
            text = category.label,
            fontSize = 9.sp,
            color = if (selected) category.color else TextMuted
        )
    }
}

@Composable
fun dialogFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = CardBackground,
    focusedContainerColor = CardBackground,
    unfocusedBorderColor = CardBorder,
    focusedBorderColor = NeonGreen,
    unfocusedTextColor = TextPrimary,
    focusedTextColor = TextPrimary,
    unfocusedLabelColor = TextMuted,
    focusedLabelColor = NeonGreen
)
