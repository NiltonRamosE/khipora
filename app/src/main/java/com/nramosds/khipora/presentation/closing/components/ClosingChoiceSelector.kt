
package com.nramosds.khipora.presentation.closing.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

internal data class ClosingChoice(
    val value: String,
    val label: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ClosingChoiceSelector(
    title: String,
    options: List<ClosingChoice>,
    selectedValue: String,
    enabled: Boolean,
    onSelected: (String) -> Unit,
    @DrawableRes iconRes: Int? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Icono a la izquierda del título
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            iconRes?.let { resId ->
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Opciones sin iconos individuales
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { option ->
                val selected = selectedValue == option.value

                FilterChip(
                    selected = selected,
                    onClick = {
                        onSelected(
                            if (selected) "" else option.value
                        )
                    },
                    enabled = enabled,
                    label = {
                        Text(option.label)
                    },
                    leadingIcon = if (selected) {
                        {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor =
                            MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor =
                            MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor =
                            MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}
