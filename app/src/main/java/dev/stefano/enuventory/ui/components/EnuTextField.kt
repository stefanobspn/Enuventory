package dev.stefano.enuventory.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.stefano.enuventory.R
import dev.stefano.enuventory.ui.theme.EnuTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnuTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    isRequired: Boolean = false,
    leadingIcon: Int? = null,
    trailingIcon: Int? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    // Dipakai buat field readOnly yang perannya cuma nampilin nilai & buka picker lain
    // (mis. DatePicker) saat di-tap -- OutlinedTextField readOnly tetap butuh interactionSource
    // sendiri karena gak ada callback onClick bawaan.
    val tapInteractionSource = remember { MutableInteractionSource() }
    if (onClick != null) {
        LaunchedEffect(tapInteractionSource) {
            tapInteractionSource.interactions.collect { interaction ->
                if (interaction is PressInteraction.Release) onClick()
            }
        }
    }
    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Row {
                Text(
                    text = label,
                    style = EnuTheme.typography.ui.labels.normalCase.small,
                    color = EnuTheme.colors.contentDefaultPrimary
                )
                if (isRequired) {
                    Text(
                        text = " *",
                        style = EnuTheme.typography.ui.labels.normalCase.small,
                        color = EnuTheme.colors.contentSignalErrorDefault
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            readOnly = readOnly,
            interactionSource = tapInteractionSource,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            textStyle = EnuTheme.typography.ui.labels.normalCase.base.copy(
                color = EnuTheme.colors.contentDefaultPrimary
            ),
            placeholder = {
                Text(
                    text = placeholder,
                    style = EnuTheme.typography.ui.labels.normalCase.base,
                    color = EnuTheme.colors.contentDefaultSubtle
                )
            },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        painter = painterResource(id = it),
                        contentDescription = null,
                        tint = EnuTheme.colors.contentDefaultPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            trailingIcon = trailingIcon?.let {
                {
                    Icon(
                        painter = painterResource(id = it),
                        contentDescription = null,
                        tint = EnuTheme.colors.contentDefaultPrimary,
                        modifier = Modifier
                            .size(24.dp)
                            .let { baseModifier ->
                                if (onTrailingIconClick != null) {
                                    baseModifier.clickable { onTrailingIconClick() }
                                } else {
                                    baseModifier
                                }
                            }
                    )
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EnuTheme.colors.borderDefaultMedium,
                unfocusedBorderColor = EnuTheme.colors.borderDefaultMedium,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
    }
}

@Preview(showBackground = true, name = "Light")
@Composable
fun EnuTextFieldPreviewLight() {
    EnuTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EnuTextField(
                value = "",
                onValueChange = {},
                placeholder = "Placeholder",
                label = "Label"
            )
            EnuTextField(
                value = "",
                onValueChange = {},
                placeholder = "Placeholder",
                label = "Label",
                leadingIcon = R.drawable.ic_info
            )
            EnuTextField(
                value = "",
                onValueChange = {},
                placeholder = "Placeholder",
                label = "Label",
                trailingIcon = R.drawable.ic_info
            )
            EnuTextField(
                value = "",
                onValueChange = {},
                placeholder = "Placeholder",
                label = "Label",
                leadingIcon = R.drawable.ic_info,
                trailingIcon = R.drawable.ic_info
            )
            EnuTextField(
                value = "",
                onValueChange = {},
                placeholder = "Placeholder",
                label = "Label",
                isRequired = true
            )
        }
    }
}

@Preview(name = "Dark")
@Composable
fun EnuTextFieldPreviewDark() {
    EnuTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EnuTextField(
                value = "",
                onValueChange = {},
                placeholder = "Placeholder",
                label = "Label"
            )
            EnuTextField(
                value = "",
                onValueChange = {},
                placeholder = "Placeholder",
                label = "Label",
                leadingIcon = R.drawable.ic_info
            )
            EnuTextField(
                value = "",
                onValueChange = {},
                placeholder = "Placeholder",
                label = "Label",
                trailingIcon = R.drawable.ic_info
            )
            EnuTextField(
                value = "",
                onValueChange = {},
                placeholder = "Placeholder",
                label = "Label",
                leadingIcon = R.drawable.ic_info,
                trailingIcon = R.drawable.ic_info
            )
            EnuTextField(
                value = "",
                onValueChange = {},
                placeholder = "Placeholder",
                label = "Label",
                isRequired = true
            )
        }
    }
}