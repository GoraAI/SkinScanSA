package com.skinscan.sa.ui.screens.recommendations

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.skinscan.sa.data.db.entity.ProductEntity
import com.skinscan.sa.domain.usecase.GetRecommendationsUseCase.ProductRecommendation
import com.skinscan.sa.ui.theme.Coral400
import com.skinscan.sa.ui.theme.DarkBackground
import com.skinscan.sa.ui.theme.ErrorRed
import com.skinscan.sa.ui.theme.GlassSurface
import com.skinscan.sa.ui.theme.Green600
import com.skinscan.sa.ui.theme.RoseGold
import com.skinscan.sa.ui.theme.Spacing
import com.skinscan.sa.ui.theme.SuccessGreen
import com.skinscan.sa.ui.theme.SurfaceBlack
import com.skinscan.sa.ui.theme.Teal600
import com.skinscan.sa.ui.theme.TealAccent
import com.skinscan.sa.ui.theme.TextSecondary
import com.skinscan.sa.ui.theme.TextWhite
import com.skinscan.sa.ui.theme.WarningYellow
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Recommendations Screen (Story 3.4)
 *
 * Displays personalized product recommendations grouped by category
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationsScreen(
    onNavigateBack: () -> Unit,
    viewModel: RecommendationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val expandedProductId by viewModel.expandedProductId.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Personalized Routine") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is RecommendationsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Teal600)
                        Spacer(modifier = Modifier.height(Spacing.m))
                        Text("Finding your perfect products...")
                    }
                }
            }

            is RecommendationsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(Spacing.l),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(Spacing.m))
                        Button(onClick = onNavigateBack) {
                            Text("Go Back")
                        }
                    }
                }
            }

            is RecommendationsUiState.Success -> {
                RecommendationsContent(
                    state = state,
                    selectedCategory = selectedCategory,
                    expandedProductId = expandedProductId,
                    onCategorySelected = { viewModel.selectCategory(it) },
                    onProductExpanded = { viewModel.toggleProductExpansion(it) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun RecommendationsContent(
    state: RecommendationsUiState.Success,
    selectedCategory: String?,
    expandedProductId: String?,
    onCategorySelected: (String?) -> Unit,
    onProductExpanded: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.m),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        // Header
        item {
            Column {
                Spacer(modifier = Modifier.height(Spacing.s))
                Text(
                    text = "Based on your scan from ${dateFormat.format(state.scanDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Spacing.m))

                // Category filter chips
                CategoryFilterChips(
                    selectedCategory = selectedCategory,
                    onCategorySelected = onCategorySelected
                )
            }
        }

        // Display products by category or filtered
        val categories = listOf(
            "CLEANSER" to state.recommendations.cleansers,
            "SERUM" to state.recommendations.serums,
            "MOISTURIZER" to state.recommendations.moisturizers,
            "SUNSCREEN" to state.recommendations.sunscreens,
            "TREATMENT" to state.recommendations.treatments
        )

        val filteredCategories = if (selectedCategory != null) {
            categories.filter { it.first == selectedCategory }
        } else {
            categories
        }

        filteredCategories.forEach { (category, products) ->
            if (products.isNotEmpty()) {
                item {
                    Text(
                        text = getCategoryDisplayName(category),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = Spacing.s)
                    )
                }

                items(products.take(2)) { recommendation ->
                    ProductCard(
                        recommendation = recommendation,
                        isExpanded = expandedProductId == recommendation.product.productId,
                        onExpandToggle = { onProductExpanded(recommendation.product.productId) }
                    )
                }
            }
        }

        // Privacy footer
        item {
            Spacer(modifier = Modifier.height(Spacing.m))
            Text(
                text = "Recommendations based on 100% on-device analysis. Your data stays private.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun CategoryFilterChips(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    val categories = listOf(
        "All" to null,
        "Cleansers" to "CLEANSER",
        "Serums" to "SERUM",
        "Moisturizers" to "MOISTURIZER",
        "Sunscreens" to "SUNSCREEN",
        "Treatments" to "TREATMENT"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s)
    ) {
        categories.forEach { (label, value) ->
            FilterChip(
                selected = selectedCategory == value,
                onClick = { onCategorySelected(value) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Teal600,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductCard(
    recommendation: ProductRecommendation,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit
) {
    val product = recommendation.product
    val context = LocalContext.current
    val priceFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "ZA")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(Spacing.m)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.brand,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Compatibility score badge
                CompatibilityBadge(score = recommendation.compatibilityScore)
            }

            Spacer(modifier = Modifier.height(Spacing.s))

            // Price and rating row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = priceFormat.format(product.priceZar),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Teal600
                )

                product.rating?.let { rating ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%.1f".format(rating),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.s))

            // Matching concerns chips
            if (recommendation.matchingConcerns.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    recommendation.matchingConcerns.forEach { concern ->
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = concern.replace("_", " ").lowercase()
                                        .replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Green600.copy(alpha = 0.1f),
                                labelColor = Green600,
                                leadingIconContentColor = Green600
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }

            // Expand/collapse button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onExpandToggle)
                    .padding(top = Spacing.s),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Show less" else "View details",
                    style = MaterialTheme.typography.labelMedium,
                    color = Teal600
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Teal600,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Expanded content (Story 3.5)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                ProductDetailExpanded(
                    product = product,
                    recommendation = recommendation,
    onBuyClicked = {
                        // Deep link to product on Clicks website
                        val slug = product.name
                            .lowercase()
                            .replace(Regex("[^a-z0-9]+"), "-")
                            .trim('-')
                        val url = "https://clicks.co.za/p/${product.productId}/$slug"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
private fun CompatibilityBadge(score: Int) {
    val color = when {
        score >= 70 -> Green600
        score >= 50 -> Color(0xFFFFA726)
        else -> Coral400
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$score%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductDetailExpanded(
    product: ProductEntity,
    recommendation: ProductRecommendation,
    onBuyClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.m)
    ) {
        // Description
        Text(
            text = product.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Spacing.m))

        // Key ingredients
        if (recommendation.matchingIngredients.isNotEmpty()) {
            Text(
                text = "Key Ingredients",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                recommendation.matchingIngredients.forEach { ingredient ->
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = ingredient.replace("_", " ").lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Teal600.copy(alpha = 0.1f),
                            labelColor = Teal600
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.m))

        // Score breakdown
        Text(
            text = "Match Breakdown",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        ScoreBreakdownRow("Ingredients", recommendation.ingredientScore, 50f)
        ScoreBreakdownRow("Skin Type", recommendation.fitzpatrickScore, 20f)
        ScoreBreakdownRow("Concerns", recommendation.concernScore, 20f)
        if (recommendation.melaninBonus > 0) {
            ScoreBreakdownRow("Melanin Bonus", recommendation.melaninBonus, 10f)
        }

        Spacer(modifier = Modifier.height(Spacing.m))

        // Melanin optimized badge
        if (product.isMelaninOptimized) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Teal600.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(Spacing.s),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Teal600,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.s))
                Text(
                    text = "Optimized for melanin-rich skin",
                    style = MaterialTheme.typography.bodySmall,
                    color = Teal600
                )
            }
            Spacer(modifier = Modifier.height(Spacing.m))
        }

        // Buy button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            OutlinedButton(
                onClick = onBuyClicked,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text("Buy at Clicks")
            }
        }
    }
}

@Composable
private fun ScoreBreakdownRow(
    label: String,
    score: Float,
    maxScore: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${score.toInt()}/${maxScore.toInt()}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun getCategoryDisplayName(category: String): String {
    return when (category) {
        "CLEANSER" -> "Step 1: Cleanse"
        "SERUM" -> "Step 2: Treat"
        "MOISTURIZER" -> "Step 3: Moisturize"
        "SUNSCREEN" -> "Step 4: Protect"
        "TREATMENT" -> "Targeted Treatments"
        else -> category
    }
}
