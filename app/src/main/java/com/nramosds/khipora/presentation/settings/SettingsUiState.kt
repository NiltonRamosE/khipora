package com.nramosds.khipora.presentation.settings


internal data class SettingsUiState(
    val isLoading: Boolean = false,
    val currentName: String = "",
    val error: String? = null,
    val savedId: Long? = null,
)
