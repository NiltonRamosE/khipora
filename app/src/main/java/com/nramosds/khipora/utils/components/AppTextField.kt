package com.nramosds.khipora.utils.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AppTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    numeric: Boolean = false,
    singleLine: Boolean = true,
    placeholder: String? = null,
    integerOnly: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    @DrawableRes leadingIconRes: Int? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { candidate ->
            val valid = when {
                integerOnly -> candidate.all { it in '0'..'9' }
                numeric -> Regex("[0-9]*([.,][0-9]{0,2})?").matches(candidate)
                else -> true
            }

            if (valid) onValueChange(candidate)
        },
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        enabled = enabled,
        isError = isError,

        leadingIcon = when {
            leadingIconRes != null -> {
                {
                    Image(
                        painter = painterResource(id = leadingIconRes),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            leadingIcon != null -> {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null
                    )
                }
            }

            else -> null
        },

        supportingText = supportingText?.let { text ->
            { Text(text) }
        },
        minLines = if (singleLine) 1 else 3,
        keyboardOptions = KeyboardOptions(
            keyboardType = when {
                integerOnly -> KeyboardType.Number
                numeric -> KeyboardType.Decimal
                else -> KeyboardType.Text
            }
        ),
        shape = RoundedCornerShape(14.dp),
    )
}
