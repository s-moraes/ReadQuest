package com.saulo.readquest.ui.screens.extra

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saulo.readquest.ui.model.ExtraCategory
import com.saulo.readquest.ui.model.ExtraItemUiModel
import com.saulo.readquest.ui.navigation.AppRoute
import com.saulo.readquest.ui.screens.common.PillBadge
import com.saulo.readquest.ui.screens.common.SectionHeader

@Composable
fun ExtraScreen(
    items: List<ExtraItemUiModel>,
    onNavigate: (AppRoute) -> Unit,
    onShowLockedSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(ExtraCategory.ALL) }

    val groupedSections = remember(items, selectedCategory) {
        if (selectedCategory == ExtraCategory.ALL) {
            ExtraCategory.entries
                .filter { it != ExtraCategory.ALL }
                .mapNotNull { cat ->
                    val catItems = items.filter { it.category == cat }
                    if (catItems.isNotEmpty()) cat to catItems else null
                }
        } else {
            val catItems = items.filter { it.category == selectedCategory }
            if (catItems.isNotEmpty()) listOf(selectedCategory to catItems) else emptyList()
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header Section
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Real-world use cases, curiosities, history & external links",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Category Filter Chips with Semantic Icons
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ExtraCategory.entries.toTypedArray()) { category ->
                        val isSelected = category == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            leadingIcon = {
                                Icon(
                                    imageVector = getCategoryIcon(category),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            label = {
                                Text(
                                    text = category.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }
                }
            }
        }

        // Sectioned or Filtered Content Items
        groupedSections.forEach { (category, sectionItems) ->
            if (selectedCategory == ExtraCategory.ALL) {
                item(key = "section_${category.name}") {
                    SectionHeader(
                        title = category.displayName,
                        icon = getCategoryIcon(category),
                        badgeText = "${sectionItems.size} ${if (sectionItems.size == 1) "item" else "itens"}"
                    )
                }
            }

            items(sectionItems, key = { it.id }) { item ->
                ExtraItemCard(
                    item = item,
                    onClick = {
                        if (item.isLocked) {
                            val req = item.unlockRequirementTitle?.let { " \"$it\"" } ?: ""
                            onShowLockedSnackbar("Complete the required Learning Path$req to unlock this content.")
                        } else {
                            onNavigate(AppRoute.ContentReader(item.id, isExtra = true))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ExtraItemCard(
    item: ExtraItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (badgeBg, badgeFg) = getCategoryBadgeColors(item.category)

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Category Micro-Badge + Status Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PillBadge(
                    text = item.category.displayName.uppercase(),
                    containerColor = badgeBg,
                    contentColor = badgeFg,
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 7.dp, vertical = 2.5.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (item.isCompleted) {
                        PillBadge(
                            text = "Read",
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Read",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        )
                    }

                    if (item.isLocked) {
                        PillBadge(
                            text = item.tierBadge ?: "LOCKED",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        )
                    }

                    if (item.externalUrl != null) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "External Link",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body: Title
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Body: Summary
            Text(
                text = item.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3
            )

            if (item.isLocked) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Complete \"${item.unlockRequirementTitle ?: "Learning Path"}\" to unlock",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Fluid FlowRow for Tags + Circular Trailing Forward Affordance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                if (item.tags.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        item.tags.forEach { tag ->
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun getCategoryIcon(category: ExtraCategory): ImageVector = when (category) {
    ExtraCategory.ALL -> Icons.Default.Category
    ExtraCategory.USE_CASE -> Icons.Default.Psychology
    ExtraCategory.CURIOSITY -> Icons.Default.Lightbulb
    ExtraCategory.HISTORY -> Icons.Default.History
    ExtraCategory.EXTERNAL_LINK -> Icons.Default.Link
}

@Composable
private fun getCategoryBadgeColors(category: ExtraCategory): Pair<Color, Color> {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return when (category) {
        ExtraCategory.USE_CASE -> {
            if (isDark) Color(0xFF004D40).copy(alpha = 0.6f) to Color(0xFF80CBC4)
            else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) to MaterialTheme.colorScheme.primary
        }
        ExtraCategory.CURIOSITY -> {
            if (isDark) Color(0xFFE65100).copy(alpha = 0.35f) to Color(0xFFFFB74D)
            else Color(0xFFFFF3E0) to Color(0xFFE65100)
        }
        ExtraCategory.HISTORY -> {
            if (isDark) Color(0xFF7B1FA2).copy(alpha = 0.35f) to Color(0xFFCE93D8)
            else Color(0xFFF3E5F5) to Color(0xFF7B1FA2)
        }
        ExtraCategory.EXTERNAL_LINK -> {
            if (isDark) Color(0xFF00838F).copy(alpha = 0.35f) to Color(0xFF80DEEA)
            else Color(0xFFE0F7FA) to Color(0xFF00838F)
        }
        ExtraCategory.ALL -> {
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        }
    }
}
