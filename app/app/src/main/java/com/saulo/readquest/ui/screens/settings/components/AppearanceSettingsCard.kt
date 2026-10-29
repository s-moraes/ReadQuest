package com.saulo.readquest.ui.screens.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saulo.readquest.ui.theme.EyeComfortPresets
import com.saulo.readquest.ui.theme.ThemeMode
import com.saulo.readquest.ui.theme.parseHexColor

@Composable
fun AppearanceSettingsCard(
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    customBackgroundHex: String,
    customTextHex: String,
    onUpdateCustomColors: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select your preferred color theme mode.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            val themeOptions = listOf(
                ThemeMode.SYSTEM to "System",
                ThemeMode.LIGHT to "Light",
                ThemeMode.DARK to "Dark",
                ThemeMode.CUSTOM to "Custom"
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                themeOptions.forEachIndexed { index, (mode, label) ->
                    SegmentedButton(
                        selected = themeMode == mode,
                        onClick = { onThemeModeChanged(mode) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = themeOptions.size
                        )
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (themeMode == mode) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Expandable Custom Eye-Comfort Palette Section
            AnimatedVisibility(
                visible = themeMode == ThemeMode.CUSTOM,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // 1. Pre-defined eye-comfort modes
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Modos Pré-definidos (Eye Comfort)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cores calibradas para reduzir a fadiga visual e o excesso de luz azul.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Preset 1: Sepia
                            val isSepiaSelected = customBackgroundHex.equals(EyeComfortPresets.SEPIA_BG, ignoreCase = true)
                            OutlinedCard(
                                onClick = {
                                    onUpdateCustomColors(
                                        EyeComfortPresets.SEPIA_BG,
                                        EyeComfortPresets.SEPIA_TEXT,
                                        EyeComfortPresets.SEPIA_ID
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = Color(0xFFF4ECD8)
                                ),
                                border = BorderStroke(
                                    width = if (isSepiaSelected) 2.dp else 1.dp,
                                    color = if (isSepiaSelected) MaterialTheme.colorScheme.primary else Color(0xFFD4C7B0)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "☀️ Sépia",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2C2416)
                                        )
                                        if (isSepiaSelected) {
                                            Spacer(modifier = Modifier.weight(1f))
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Papel quente, luz natural",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF5C4E3D)
                                    )
                                }
                            }

                            // Preset 2: Night Owl
                            val isNightOwlSelected = customBackgroundHex.equals(EyeComfortPresets.NIGHT_OWL_BG, ignoreCase = true)
                            OutlinedCard(
                                onClick = {
                                    onUpdateCustomColors(
                                        EyeComfortPresets.NIGHT_OWL_BG,
                                        EyeComfortPresets.NIGHT_OWL_TEXT,
                                        EyeComfortPresets.NIGHT_OWL_ID
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = Color(0xFF1E1D1B)
                                ),
                                border = BorderStroke(
                                    width = if (isNightOwlSelected) 2.dp else 1.dp,
                                    color = if (isNightOwlSelected) MaterialTheme.colorScheme.primary else Color(0xFF383531)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "🌙 Noturno",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE8DFD1)
                                        )
                                        if (isNightOwlSelected) {
                                            Spacer(modifier = Modifier.weight(1f))
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Cinza escuro sem ofuscar",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFA69E90)
                                    )
                                }
                            }
                        }
                    }

                    // 2. Custom Color Adjusters
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Ajustar Cor de Fundo",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val bgSwatches = listOf(
                            "#F4ECD8" to "Sépia",
                            "#FAF0E6" to "Linho",
                            "#E8F0E6" to "Menta",
                            "#F7EBE8" to "Rosé",
                            "#1E1D1B" to "Carvão",
                            "#141A29" to "Crepúsculo",
                            "#121212" to "Preto"
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(bgSwatches) { (hex, name) ->
                                val isChosen = customBackgroundHex.equals(hex, ignoreCase = true)
                                val swatchColor = parseHexColor(hex, Color.Gray)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(swatchColor)
                                        .border(
                                            width = if (isChosen) 2.5.dp else 1.dp,
                                            color = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            onUpdateCustomColors(hex, customTextHex, "CUSTOM")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isChosen) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = name,
                                            tint = if (swatchColor.luminance() < 0.5f) Color.White else Color.Black,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Ajustar Cor do Texto",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val textSwatches = listOf(
                            "#2C2416" to "Café",
                            "#1C1C1E" to "Carvão",
                            "#233827" to "Floresta",
                            "#E8DFD1" to "Areia",
                            "#D1D5DB" to "Cinza",
                            "#FFFFFF" to "Branco"
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(textSwatches) { (hex, name) ->
                                val isChosen = customTextHex.equals(hex, ignoreCase = true)
                                val swatchColor = parseHexColor(hex, Color.Black)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(swatchColor)
                                        .border(
                                            width = if (isChosen) 2.5.dp else 1.dp,
                                            color = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            onUpdateCustomColors(customBackgroundHex, hex, "CUSTOM")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isChosen) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = name,
                                            tint = if (swatchColor.luminance() < 0.5f) Color.White else Color.Black,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Live Reading Preview Box
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Visualização de Leitura",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val previewBg = parseHexColor(customBackgroundHex, Color(0xFFF4ECD8))
                        val previewText = parseHexColor(customTextHex, Color(0xFF2C2416))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = previewBg,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Exemplo de Leitura Confortável",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = previewText
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Este texto demonstra o contraste suave para leitura prolongada sem fadiga visual.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = previewText.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
