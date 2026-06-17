package com.cresup.app.presentation.ui.screens.social

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cresup.app.domain.model.FeedItem
import com.cresup.app.domain.model.FeedItemType
import com.cresup.app.domain.model.FriendRequest
import com.cresup.app.domain.model.PublicProfile
import com.cresup.app.presentation.ui.components.GlassCard
import com.cresup.app.presentation.ui.screens.dashboard.levelColor
import com.cresup.app.presentation.ui.screens.gastos.dialogFieldColors
import com.cresup.app.presentation.ui.theme.*
import com.cresup.app.presentation.viewmodel.SocialViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SocialScreen(viewModel: SocialViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.successMessage, state.errorMessage) {
        state.successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
        state.errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    val ranking = remember(state.currentUser, state.friends) {
        (listOf(state.currentUser) + state.friends)
            .filter { it.name.isNotBlank() }
            .sortedByDescending { it.xp }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background,
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = NeonGreen,
                    contentColor = Background,
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = "Adicionar amigo")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item { SocialHeader(userCode = state.currentUser.userCode) }

            item {
                SocialTabRow(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    incomingCount = state.incomingRequests.size
                )
            }

            when (selectedTab) {
                0 -> {
                    val hasContent = state.incomingRequests.isNotEmpty() ||
                            state.sentRequests.isNotEmpty() ||
                            state.friends.isNotEmpty()

                    if (!hasContent) {
                        item { EmptyFriendsCard(onAdd = { showAddDialog = true }) }
                    } else {
                        if (state.incomingRequests.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = "Pedidos de Amizade",
                                    count = state.incomingRequests.size,
                                    color = AccentOrange
                                )
                            }
                            itemsIndexed(state.incomingRequests) { _, request ->
                                IncomingRequestCard(
                                    request = request,
                                    onAccept = { viewModel.acceptRequest(request) },
                                    onReject = { viewModel.rejectRequest(request) }
                                )
                            }
                        }

                        if (state.sentRequests.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = "Enviados",
                                    count = state.sentRequests.size,
                                    color = TextMuted
                                )
                            }
                            itemsIndexed(state.sentRequests) { _, request ->
                                SentRequestCard(request = request)
                            }
                        }

                        if (state.friends.isNotEmpty()) {
                            if (state.incomingRequests.isNotEmpty() || state.sentRequests.isNotEmpty()) {
                                item {
                                    SectionHeader(
                                        title = "Amigos",
                                        count = state.friends.size,
                                        color = NeonGreen
                                    )
                                }
                            }
                            itemsIndexed(state.friends) { _, friend ->
                                FriendCard(
                                    friend = friend,
                                    onRemove = { viewModel.removeFriend(friend.uid) }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    if (ranking.isEmpty()) {
                        item { EmptyRankingCard() }
                    } else {
                        itemsIndexed(ranking) { index, profile ->
                            RankingCard(
                                position = index + 1,
                                profile = profile,
                                isCurrentUser = profile.uid.isEmpty()
                            )
                        }
                    }
                }
                2 -> {
                    if (state.feed.isEmpty()) {
                        item { EmptyFeedCard() }
                    } else {
                        itemsIndexed(state.feed) { _, item ->
                            FeedItemCard(item = item)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddFriendDialog(
            searchCode = state.searchCode,
            onSearchCodeChange = viewModel::onSearchCodeChange,
            onSearch = viewModel::searchFriend,
            isSearching = state.isSearching,
            searchResult = state.searchResult,
            errorMessage = state.errorMessage,
            onSendRequest = { viewModel.sendFriendRequest(it); showAddDialog = false },
            onDismiss = { showAddDialog = false; viewModel.clearSearch() }
        )
    }
}

@Composable
private fun SocialHeader(userCode: String) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NeonGreen.copy(0.08f), Background),
                    endY = 300f
                )
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Social", fontSize = 28.sp, fontWeight = FontWeight.Black, color = TextPrimary)
            Text("Adicione amigos e veja quem está evoluindo mais", fontSize = 13.sp, color = TextSecondary)

            if (userCode.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBackground)
                        .border(1.dp, NeonGreen.copy(0.3f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.QrCode2, null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Seu código", fontSize = 10.sp, color = TextMuted)
                        Text(userCode, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                    }
                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(userCode))
                            copied = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (copied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                            null,
                            tint = if (copied) NeonGreen else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SocialTabRow(selectedTab: Int, onTabSelected: (Int) -> Unit, incomingCount: Int) {
    val tabs = listOf("Amigos", "Ranking", "Feed")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        tabs.forEachIndexed { index, label ->
            val selected = selectedTab == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selected) NeonGreen.copy(0.15f) else Color.Transparent)
                    .border(
                        width = if (selected) 1.dp else 0.dp,
                        color = if (selected) NeonGreen.copy(0.4f) else Color.Transparent,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(onClick = { onTabSelected(index) }, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            label,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) NeonGreen else TextMuted
                        )
                        if (index == 0 && incomingCount > 0) {
                            Badge(
                                containerColor = AccentOrange,
                                contentColor = Background
                            ) {
                                Text(
                                    incomingCount.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        Badge(containerColor = color.copy(0.2f), contentColor = color) {
            Text(count.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun IncomingRequestCard(
    request: FriendRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    GlassCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AccentOrange.copy(0.15f))
                            .border(2.dp, AccentOrange.copy(0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.PersonAdd, null, tint = AccentOrange, modifier = Modifier.size(22.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(request.fromName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(request.fromCode, fontSize = 11.sp, color = TextMuted)
                            Text("·", fontSize = 11.sp, color = TextMuted)
                            Text(request.fromLevelName, fontSize = 11.sp, color = levelColor(request.fromLevel))
                        }
                    }
                }
                Text("${request.fromXp} XP", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentYellow)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = Brush.linearGradient(listOf(CardBorder, CardBorder))),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Close, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Recusar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Check, null, tint = Background, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Aceitar", color = Background, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SentRequestCard(request: FriendRequest) {
    GlassCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(TextMuted.copy(0.1f))
                        .border(2.dp, TextMuted.copy(0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, null, tint = TextMuted, modifier = Modifier.size(22.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(request.toName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text(request.toCode, fontSize = 11.sp, color = TextMuted)
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Schedule, null, tint = AccentYellow, modifier = Modifier.size(14.dp))
                Text("Pendente", fontSize = 12.sp, color = AccentYellow, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun FriendCard(friend: PublicProfile, onRemove: () -> Unit) {
    GlassCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(levelColor(friend.level).copy(0.15f))
                        .border(2.dp, levelColor(friend.level).copy(0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, null, tint = levelColor(friend.level), modifier = Modifier.size(22.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(friend.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(friend.userCode, fontSize = 11.sp, color = TextMuted)
                        Text("·", fontSize = 11.sp, color = TextMuted)
                        Text(friend.levelName, fontSize = 11.sp, color = levelColor(friend.level))
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${friend.xp} XP", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentYellow)
                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.PersonRemove, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun RankingCard(position: Int, profile: PublicProfile, isCurrentUser: Boolean) {
    val positionColor = when (position) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> TextMuted
    }
    val medal = when (position) {
        1, 2, 3 -> Icons.Filled.EmojiEvents
        else -> null
    }

    GlassCard(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .then(
                if (isCurrentUser) Modifier.border(1.dp, NeonGreen.copy(0.4f), RoundedCornerShape(20.dp))
                else Modifier
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                    if (medal != null) {
                        Icon(medal, null, tint = positionColor, modifier = Modifier.size(28.dp))
                    } else {
                        Text("#$position", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        if (isCurrentUser) "${profile.name} (você)" else profile.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCurrentUser) NeonGreen else TextPrimary
                    )
                    Text(
                        "Nível ${profile.level} · ${profile.levelName}",
                        fontSize = 11.sp,
                        color = levelColor(profile.level)
                    )
                }
            }
            Text(
                "${profile.xp} XP",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = positionColor.takeIf { position <= 3 } ?: AccentYellow
            )
        }
    }
}

@Composable
private fun FeedItemCard(item: FeedItem) {
    val (icon, color) = when (item.type) {
        FeedItemType.CHALLENGE -> Icons.Filled.Bolt to NeonGreen
        FeedItemType.ACHIEVEMENT -> Icons.Filled.EmojiEvents to AccentYellow
        FeedItemType.LEVEL_UP -> Icons.Filled.ElectricBolt to AccentPink
        FeedItemType.STREAK -> Icons.Filled.LocalFireDepartment to AccentOrange
    }

    GlassCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.actorName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(item.message, fontSize = 13.sp, color = TextSecondary)
                }
                Text(formatTimestamp(item.timestamp), fontSize = 10.sp, color = TextMuted)
            }
        }
    }
}

@Composable
private fun EmptyFriendsCard(onAdd: () -> Unit) {
    GlassCard(modifier = Modifier.padding(16.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(NeonGreen.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Group, null, tint = NeonGreen, modifier = Modifier.size(32.dp))
            }
            Text("Nenhum amigo ainda", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(
                "Compartilhe seu código com amigos e compita no ranking",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.PersonAdd, null, tint = Background, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Adicionar Amigo", color = Background, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EmptyRankingCard() {
    GlassCard(modifier = Modifier.padding(16.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Filled.EmojiEvents, null, tint = AccentYellow, modifier = Modifier.size(40.dp))
            Text("Adicione amigos para ver o ranking", fontSize = 14.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun EmptyFeedCard() {
    GlassCard(modifier = Modifier.padding(16.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Filled.DynamicFeed, null, tint = TextMuted, modifier = Modifier.size(40.dp))
            Text("Nenhuma atividade ainda", fontSize = 14.sp, color = TextSecondary)
            Text("Conclua desafios para aparecer no feed!", fontSize = 12.sp, color = TextMuted)
        }
    }
}

@Composable
private fun AddFriendDialog(
    searchCode: String,
    onSearchCodeChange: (String) -> Unit,
    onSearch: () -> Unit,
    isSearching: Boolean,
    searchResult: PublicProfile?,
    errorMessage: String?,
    onSendRequest: (PublicProfile) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceVariant,
        title = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.PersonSearch, null, tint = NeonGreen, modifier = Modifier.size(22.dp))
                Text("Adicionar Amigo", fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Digite o código do amigo (ex: CRES-AB12)", fontSize = 13.sp, color = TextSecondary)
                OutlinedTextField(
                    value = searchCode,
                    onValueChange = onSearchCodeChange,
                    placeholder = { Text("CRES-XXXX", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = dialogFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                    trailingIcon = {
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = NeonGreen, strokeWidth = 2.dp)
                        }
                    }
                )
                if (errorMessage != null) {
                    Text(errorMessage, fontSize = 12.sp, color = AccentRed)
                }
                if (searchResult != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeonGreen.copy(0.08f))
                            .border(1.dp, NeonGreen.copy(0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(searchResult.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Nível ${searchResult.level} · ${searchResult.userCode}", fontSize = 11.sp, color = TextSecondary)
                        }
                        Button(
                            onClick = { onSendRequest(searchResult) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.Send, null, tint = Background, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Enviar", color = Background, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSearch,
                enabled = searchCode.isNotBlank() && !isSearching,
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text("Buscar", color = Background, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = TextMuted) }
        }
    )
}

private fun formatTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "agora"
        diff < 3_600_000 -> "${diff / 60_000}min atrás"
        diff < 86_400_000 -> "${diff / 3_600_000}h atrás"
        else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(timestamp))
    }
}
