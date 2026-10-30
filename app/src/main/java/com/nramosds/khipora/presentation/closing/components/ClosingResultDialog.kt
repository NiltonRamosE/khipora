package com.nramosds.khipora.presentation.closing.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nramosds.khipora.utils.AppTheme

@Composable
internal fun ClosingResultDialog(
    isSuccess: Boolean,
    message: String,
    onConfirm: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val accentColor = if (isSuccess) colors.primary else colors.error
    val iconBackground = if (isSuccess) colors.primaryContainer else colors.errorContainer

    Dialog(
        onDismissRequest = { if (!isSuccess) onConfirm() },
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = !isSuccess,
        ),
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = colors.surface,
            tonalElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(iconBackground.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(shape = CircleShape, color = iconBackground) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Outlined.Check else Icons.Outlined.Info,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.padding(14.dp).size(28.dp),
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = if (isSuccess) "Tu día quedó registrado" else "Revisemos lo que pasó",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(if (isSuccess) "Volver al inicio" else "Revisar mi cierre")
                    if (isSuccess) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Outlined.ArrowForward, null, Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ClosingSuccessPreview() {
    AppTheme {
        ClosingResultDialog(
            isSuccess = true,
            message = "El cierre del 29 de octubre y sus pagos se guardaron correctamente.",
            onConfirm = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ClosingErrorPreview() {
    AppTheme {
        ClosingResultDialog(
            isSuccess = false,
            message = "No pudimos conectarnos. Tus datos siguen en el formulario. " +
                "Revisa tu conexión y vuelve a intentarlo.",
            onConfirm = {},
        )
    }
}
