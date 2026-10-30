package com.nramosds.khipora.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.nramosds.khipora.AppContainer
import com.nramosds.khipora.presentation.closing.ClosingScreen
import com.nramosds.khipora.presentation.history.HistoryScreen
import com.nramosds.khipora.presentation.home.HomeScreen
import com.nramosds.khipora.presentation.settings.SettingsScreen
import com.nramosds.khipora.presentation.statistics.StatisticsScreen
import com.nramosds.khipora.utils.AppTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun KhiporaApp(container: AppContainer) {
    val businessId by container.businessPreferences.businessId.collectAsState(initial = -1L)
    val name by container.businessPreferences.businessName.collectAsState(initial = "")
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.HOME) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var homeVersion by rememberSaveable { mutableIntStateOf(0) }
    val saveBusiness: (Long, String) -> Unit = { id, newName ->
        scope.launch { container.businessPreferences.saveBusiness(id, newName) }
    }

    LaunchedEffect(businessId) {
        if (businessId > 0) {
            try {
                container.businessRepository.getById(businessId)?.let {
                    container.businessPreferences.updateName(it.id, it.name)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) { }
        }
    }
    if (businessId == -1L) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
        return
    }
    if (businessId == 0L) {
        SettingsScreen(0L, container, saveBusiness)
        return
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            KhiporaSidebar(currentScreen, onClose = { scope.launch { drawerState.close() } }) {
                currentScreen = it
                scope.launch { drawerState.close() }
            }
        },
    ) {
        Scaffold(
            topBar = {
                BusinessTopBar(name.ifBlank { "Mi negocio" }, currentScreen.label) {
                    scope.launch { drawerState.open() }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (currentScreen) {
                    AppScreen.HOME -> key(homeVersion) {
                        HomeScreen(businessId, container) { currentScreen = AppScreen.CLOSING }
                    }

                    AppScreen.CLOSING -> ClosingScreen(businessId, container) {
                        homeVersion++
                        currentScreen = AppScreen.HOME
                    }

                    AppScreen.STATISTICS -> StatisticsScreen(businessId, container)
                    AppScreen.SETTINGS -> SettingsScreen(businessId, container, saveBusiness)
                    AppScreen.HISTORY -> HistoryScreen(
                        businessId = businessId,
                        container = container
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BusinessTopBar(name: String, section: String, onMenu: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    section,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenu) {
                Icon(
                    Icons.Outlined.Menu,
                    contentDescription = "Abrir menú"
                )
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun BusinessTopBarPreview() {
    AppTheme { BusinessTopBar("Café Los Olivos", "Estadísticas", {}) }
}
