package com.saulo.readquest.ui.screens.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design tokens and dimensions for persistent bottom action bars.
 */
object BottomActionBarDefaults {
    val ButtonHeight: Dp = 40.dp
    val VerticalPadding: Dp = 7.dp
    val HorizontalPadding: Dp = 16.dp
    val ItemSpacing: Dp = 10.dp
    val ButtonCornerRadius: Dp = 10.dp
    val ButtonShape: RoundedCornerShape = RoundedCornerShape(ButtonCornerRadius)
    val ButtonContentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
    val TonalElevation: Dp = 4.dp
    val ShadowElevation: Dp = 4.dp
}

/**
 * Standard container surface for bottom action bars with proper insets and consistent spacing.
 */
@Composable
fun BottomActionBarContainer(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = BottomActionBarDefaults.TonalElevation,
        shadowElevation = BottomActionBarDefaults.ShadowElevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = BottomActionBarDefaults.HorizontalPadding,
                    vertical = BottomActionBarDefaults.VerticalPadding
                ),
            horizontalArrangement = Arrangement.spacedBy(BottomActionBarDefaults.ItemSpacing),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * High-level reusable dual-action bottom bar supporting 1 or 2 actions.
 * - If both [primaryText] and [secondaryText] are provided, renders secondary (outlined) on the left
 *   and primary (filled) on the right, each with weight(1f).
 * - If only one action is provided, renders it as full-width.
 */
@Composable
fun DualActionBottomBar(
    modifier: Modifier = Modifier,
    primaryText: String? = null,
    onPrimaryClick: (() -> Unit)? = null,
    primaryEnabled: Boolean = true,
    primaryLeadingIcon: (@Composable () -> Unit)? = null,
    primaryTrailingIcon: (@Composable () -> Unit)? = null,
    primaryColors: ButtonColors? = null,
    secondaryText: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    secondaryEnabled: Boolean = true,
    secondaryLeadingIcon: (@Composable () -> Unit)? = null,
    secondaryTrailingIcon: (@Composable () -> Unit)? = null,
) {
    BottomActionBarContainer(modifier = modifier) {
        val hasSecondary = secondaryText != null && onSecondaryClick != null
        val hasPrimary = primaryText != null && onPrimaryClick != null

        if (hasSecondary) {
            OutlinedButton(
                onClick = onSecondaryClick!!,
                enabled = secondaryEnabled,
                modifier = if (hasPrimary) {
                    Modifier.weight(1f).height(BottomActionBarDefaults.ButtonHeight)
                } else {
                    Modifier.fillMaxWidth().height(BottomActionBarDefaults.ButtonHeight)
                },
                contentPadding = BottomActionBarDefaults.ButtonContentPadding,
                shape = BottomActionBarDefaults.ButtonShape
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (secondaryLeadingIcon != null) {
                        secondaryLeadingIcon()
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = secondaryText!!,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (secondaryTrailingIcon != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        secondaryTrailingIcon()
                    }
                }
            }
        }

        if (hasPrimary) {
            Button(
                onClick = onPrimaryClick!!,
                enabled = primaryEnabled,
                modifier = if (hasSecondary) {
                    Modifier.weight(1f).height(BottomActionBarDefaults.ButtonHeight)
                } else {
                    Modifier.fillMaxWidth().height(BottomActionBarDefaults.ButtonHeight)
                },
                contentPadding = BottomActionBarDefaults.ButtonContentPadding,
                shape = BottomActionBarDefaults.ButtonShape,
                colors = primaryColors ?: ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (primaryLeadingIcon != null) {
                        primaryLeadingIcon()
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = primaryText!!,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (primaryTrailingIcon != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        primaryTrailingIcon()
                    }
                }
            }
        }
    }
}
