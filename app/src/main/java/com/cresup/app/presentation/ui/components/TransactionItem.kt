package com.cresup.app.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cresup.app.domain.model.Transaction
import com.cresup.app.domain.model.TransactionType
import com.cresup.app.presentation.ui.screens.dashboard.formatCurrency
import com.cresup.app.presentation.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionItem(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == TransactionType.INCOME

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(transaction.category.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = transaction.category.emoji, fontSize = 20.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = transaction.category.label + " · " + formatDate(transaction.date),
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        Text(
            text = "${if (isIncome) "+" else "-"}${formatCurrency(transaction.amount)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isIncome) NeonGreen else AccentRed
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM", Locale("pt", "BR"))
    return sdf.format(Date(timestamp))
}
