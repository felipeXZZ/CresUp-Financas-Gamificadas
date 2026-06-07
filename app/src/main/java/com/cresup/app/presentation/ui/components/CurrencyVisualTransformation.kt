package com.cresup.app.presentation.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = formatCurrencyDisplay(digits)
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = formatted.length
            override fun transformedToOriginal(offset: Int): Int = digits.length
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

fun formatCurrencyDisplay(digits: String): String {
    if (digits.isEmpty()) return ""
    val number = digits.toLongOrNull() ?: return digits
    val intPart = number / 100
    val decPart = number % 100
    val intStr = intPart.toString()
    val intFormatted = buildString {
        intStr.forEachIndexed { i, c ->
            if (i > 0 && (intStr.length - i) % 3 == 0) append('.')
            append(c)
        }
    }
    return "$intFormatted,${decPart.toString().padStart(2, '0')}"
}
