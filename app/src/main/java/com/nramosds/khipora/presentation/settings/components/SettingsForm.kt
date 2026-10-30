package com.nramosds.khipora.presentation.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nramosds.khipora.presentation.settings.SettingsUiState
import com.nramosds.khipora.utils.AppTheme
import com.nramosds.khipora.utils.components.AppCard
import com.nramosds.khipora.utils.components.AppTextField

@Composable
internal fun SettingsContent(
    businessId: Long,
    uiState: SettingsUiState,
    onSave: (String) -> Unit,
    onLinkBusiness: (Long) -> Unit
) {
    var name by rememberSaveable(businessId) { mutableStateOf(uiState.currentName) }
    var existingBusinessId by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(uiState.currentName) {
        if (uiState.currentName.isNotBlank()) name = uiState.currentName
    }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            if (businessId <= 0) "Configura tu negocio" else "Configuración",
            style = MaterialTheme.typography.headlineSmall
        )
        AppCard {
            AppTextField("Nombre del negocio", name, { name = it }, placeholder = "Ej. Mi tienda")
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                onClick = { onSave(name) },
            ) { Text(if (businessId <= 0) "Crear negocio" else "Actualizar nombre") }
            if (businessId > 0) Text(
                "ID del negocio: $businessId",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (businessId <= 0) {
            AppCard {
                Text("¿Ya creaste un negocio?", style = MaterialTheme.typography.titleMedium)
                Text("Para pruebas puedes vincular su ID. Como el acceso es público, este ID no proporciona seguridad.")
                AppTextField(
                    "ID existente",
                    existingBusinessId,
                    { existingBusinessId = it },
                    integerOnly = true
                )
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading && (existingBusinessId.toLongOrNull() ?: 0L) > 0L,
                    onClick = { existingBusinessId.toLongOrNull()?.let(onLinkBusiness) }
                ) { Text("Vincular negocio") }
            }
        }
        if (uiState.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
        uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 650)
@Composable
private fun SettingsPreview() {
    AppTheme { SettingsContent(1, SettingsUiState(currentName = "Café Los Olivos"), {}, {}) }
}
