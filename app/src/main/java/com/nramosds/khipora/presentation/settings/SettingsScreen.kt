package com.nramosds.khipora.presentation.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nramosds.khipora.AppContainer
import com.nramosds.khipora.presentation.settings.components.SettingsContent
import com.nramosds.khipora.utils.viewModelFactory

@Composable
fun SettingsScreen(
    businessId: Long,
    container: AppContainer,
    onBusinessSaved: (Long, String) -> Unit
) {
    val viewModel: SettingsViewModel =
        viewModel(factory = viewModelFactory { SettingsViewModel(container.businessRepository) })
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(businessId) { viewModel.load(businessId) }
    LaunchedEffect(uiState.savedId) {
        uiState.savedId?.let { id ->
            viewModel.consumeSavedResult()
            onBusinessSaved(id, uiState.currentName)
        }
    }
    SettingsContent(
        businessId,
        uiState,
        onSave = { viewModel.save(businessId, it) },
        onLinkBusiness = viewModel::linkBusiness
    )
}
