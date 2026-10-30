package com.nramosds.khipora.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nramosds.khipora.utils.AppTheme

private val AppScreen.icon: ImageVector
    get() = when (this) {
        AppScreen.HOME -> Icons.Outlined.Home
        AppScreen.CLOSING -> Icons.Outlined.AssignmentTurnedIn
        AppScreen.STATISTICS -> Icons.Outlined.BarChart
        AppScreen.HISTORY -> Icons.Outlined.History
        AppScreen.SETTINGS -> Icons.Outlined.Settings
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KhiporaSidebar(
    currentScreen: AppScreen,
    onClose: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    ModalDrawerSheet(
        modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(0.9f),
        drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
        drawerContainerColor = colors.surface,
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(shape = RoundedCornerShape(16.dp), color = colors.primary) {
                    Icon(
                        Icons.Outlined.Storefront,
                        contentDescription = null,
                        tint = colors.onPrimary,
                        modifier = Modifier.padding(12.dp).size(28.dp),
                    )
                }
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("Khipora", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Cada día cuenta",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "Cerrar menú",
                        tint = colors.onSurfaceVariant,
                    )
                }
            }
            Text(
                "MI NEGOCIO",
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
            AppScreen.entries.filter { it != AppScreen.SETTINGS }.forEach { item ->
                SidebarItem(item, selected = currentScreen == item, onClick = { onNavigate(item) })
            }
            HorizontalDivider(
                Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                color = colors.outlineVariant,
            )
            Text(
                "PREFERENCIAS",
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
            SidebarItem(
                AppScreen.SETTINGS,
                selected = currentScreen == AppScreen.SETTINGS,
                onClick = { onNavigate(AppScreen.SETTINGS) },
            )
        }
    }
}

@Composable
private fun SidebarItem(item: AppScreen, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    NavigationDrawerItem(
        selected = selected,
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.padding(vertical = 2.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = colors.primaryContainer,
            selectedIconColor = colors.onPrimary,
            selectedTextColor = colors.onPrimaryContainer,
            unselectedContainerColor = colors.surface,
            unselectedIconColor = colors.onSurfaceVariant,
            unselectedTextColor = colors.onSurface,
        ),
        icon = {
            Box(
                modifier = Modifier
                    .background(
                        if (selected) colors.primary else colors.surfaceVariant,
                        RoundedCornerShape(12.dp),
                    )
                    .padding(10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(item.icon, contentDescription = null, modifier = Modifier.size(22.dp))
            }
        },
        label = {
            Column(
                Modifier.padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    item.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                )
            }
        },
    )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun KhiporaSidebarPreview() {
    AppTheme {
        KhiporaSidebar(currentScreen = AppScreen.HOME, onClose = {}, onNavigate = {})
    }
}
