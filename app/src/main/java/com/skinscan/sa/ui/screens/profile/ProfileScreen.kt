package com.skinscan.sa.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.skinscan.sa.data.ml.SkinAnalysisInference.SkinConcern
import com.skinscan.sa.ui.theme.Coral400
import com.skinscan.sa.ui.theme.DarkBackground
import com.skinscan.sa.ui.theme.ErrorRed
import com.skinscan.sa.ui.theme.GlassBorder
import com.skinscan.sa.ui.theme.GlassSurface
import com.skinscan.sa.ui.theme.RoseGold
import com.skinscan.sa.ui.theme.Spacing
import com.skinscan.sa.ui.theme.SurfaceBlack
import com.skinscan.sa.ui.theme.Teal600
import com.skinscan.sa.ui.theme.TealAccent
import com.skinscan.sa.ui.theme.TextSecondary
import com.skinscan.sa.ui.theme.TextWhite
import com.skinscan.sa.ui.theme.glassSurface
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Profile Management Screen (Story 4.4)
 *
 * View and edit user profile, skin concerns, budget, allergies
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onDataDeleted: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showDeleteConfirmation by viewModel.deleteConfirmation.collectAsState()

    // Edit sheet states
    var showConcernsSheet by remember { mutableStateOf(false) }
    var showBudgetSheet by remember { mutableStateOf(false) }
    var showLocationSheet by remember { mutableStateOf(false) }
    var showAllergiesSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile Settings",
                        color = TextWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackground)
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TealAccent)
                }
            }

            is ProfileUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackground)
                        .padding(paddingValues)
                        .padding(Spacing.l),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        textAlign = TextAlign.Center,
                        color = TextWhite
                    )
                }
            }

            is ProfileUiState.Success -> {
                ProfileContent(
                    state = state,
                    onEditConcerns = { showConcernsSheet = true },
                    onEditBudget = { showBudgetSheet = true },
                    onEditLocation = { showLocationSheet = true },
                    onEditAllergies = { showAllergiesSheet = true },
                    onDeleteData = { viewModel.showDeleteConfirmation() },
                    modifier = Modifier.padding(paddingValues)
                )

                // Edit bottom sheets
                if (showConcernsSheet) {
                    ConcernsEditSheet(
                        currentConcerns = state.currentConcerns,
                        onDismiss = { showConcernsSheet = false },
                        onSave = { concerns ->
                            viewModel.updateConcerns(concerns)
                            showConcernsSheet = false
                        }
                    )
                }

                if (showBudgetSheet) {
                    BudgetEditSheet(
                        currentBudget = state.budgetRange,
                        onDismiss = { showBudgetSheet = false },
                        onSave = { budget ->
                            viewModel.updateBudget(budget)
                            showBudgetSheet = false
                        }
                    )
                }

                if (showLocationSheet) {
                    LocationEditSheet(
                        currentLocation = state.location,
                        onDismiss = { showLocationSheet = false },
                        onSave = { location ->
                            viewModel.updateLocation(location)
                            showLocationSheet = false
                        }
                    )
                }

                if (showAllergiesSheet) {
                    AllergiesEditSheet(
                        currentAllergies = state.allergies,
                        onDismiss = { showAllergiesSheet = false },
                        onSave = { allergies ->
                            viewModel.updateAllergies(allergies)
                            showAllergiesSheet = false
                        }
                    )
                }
            }
        }

        // Delete confirmation dialog
        if (showDeleteConfirmation) {
            DeleteConfirmationDialog(
                onDismiss = { viewModel.hideDeleteConfirmation() },
                onConfirm = { viewModel.deleteAllData(onDataDeleted) }
            )
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileUiState.Success,
    onEditConcerns: () -> Unit,
    onEditBudget: () -> Unit,
    onEditLocation: () -> Unit,
    onEditAllergies: () -> Unit,
    onDeleteData: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.m),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        item {
            Spacer(modifier = Modifier.height(Spacing.s))

            // User avatar placeholder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Teal600),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "U",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Skin Profile Section
        item {
            Text(
                text = "Skin Profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            ProfileEditCard(
                title = "Current Concerns",
                value = if (state.currentConcerns.isEmpty())
                    "None selected"
                else
                    state.currentConcerns.joinToString(", ") { it.displayName },
                onEdit = onEditConcerns
            )
        }

        item {
            ProfileEditCard(
                title = "Budget Range",
                value = "${state.budgetRange.displayName} (${state.budgetRange.priceRange})",
                onEdit = onEditBudget
            )
        }

        item {
            ProfileEditCard(
                title = "Location/Climate",
                value = state.location ?: "Not set",
                onEdit = onEditLocation
            )
        }

        item {
            ProfileEditCard(
                title = "Known Allergies",
                value = state.allergies ?: "None",
                onEdit = onEditAllergies
            )
        }

        // Scan Statistics Section
        item {
            Spacer(modifier = Modifier.height(Spacing.s))
            Text(
                text = "Scan Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            ScanStatsCard(stats = state.scanStats)
        }

        // Privacy Section
        item {
            Spacer(modifier = Modifier.height(Spacing.s))
            Text(
                text = "Privacy & Data",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.m)
                ) {
                    Row(
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
                            text = "POPIA Consent Given",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    state.popiaConsentDate?.let { date ->
                        Text(
                            text = "Consented on ${dateFormat.format(date)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 28.dp)
                        )
                    }
                }
            }
        }

        // Delete Data Button
        item {
            OutlinedButton(
                onClick = onDeleteData,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Coral400
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.s))
                Text("Delete All My Data")
            }
        }

        item {
            Text(
                text = "Your data is stored locally on your device only. Deleting will remove all scan history, profile data, and preferences.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.m)
            )
        }

        item {
            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun ProfileEditCard(
    title: String,
    value: String,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.m),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = Teal600,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ScanStatsCard(stats: ScanStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.m),
            verticalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            StatRow("Total Scans", stats.totalScans.toString())
            stats.mostCommonConcern?.let {
                StatRow("Most Common Concern", "${it.displayName} (${stats.concernOccurrences}/${stats.totalScans} scans)")
            }
            StatRow("Scan Frequency", stats.scanFrequency)
            stats.daysSinceLastScan?.let {
                StatRow("Last Scan", if (it == 0) "Today" else "$it days ago")
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ConcernsEditSheet(
    currentConcerns: List<SkinConcern>,
    onDismiss: () -> Unit,
    onSave: (List<SkinConcern>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val selectedConcerns = remember { mutableStateListOf(*currentConcerns.toTypedArray()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.m)
        ) {
            Text(
                text = "Select Your Skin Concerns",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(Spacing.m))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                verticalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                SkinConcern.entries.forEach { concern ->
                    FilterChip(
                        selected = selectedConcerns.contains(concern),
                        onClick = {
                            if (selectedConcerns.contains(concern)) {
                                selectedConcerns.remove(concern)
                            } else {
                                selectedConcerns.add(concern)
                            }
                        },
                        label = { Text(concern.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Teal600,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.l))

            Button(
                onClick = { onSave(selectedConcerns.toList()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetEditSheet(
    currentBudget: BudgetRange,
    onDismiss: () -> Unit,
    onSave: (BudgetRange) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var selectedBudget by remember { mutableStateOf(currentBudget) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.m)
        ) {
            Text(
                text = "Select Budget Range",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(Spacing.m))

            BudgetRange.entries.forEach { budget ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedBudget = budget }
                        .padding(vertical = Spacing.s),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedBudget == budget,
                        onClick = { selectedBudget = budget }
                    )
                    Spacer(modifier = Modifier.width(Spacing.s))
                    Column {
                        Text(
                            text = budget.displayName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = budget.priceRange,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.l))

            Button(
                onClick = { onSave(selectedBudget) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationEditSheet(
    currentLocation: String?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var location by remember { mutableStateOf(currentLocation ?: "") }

    val provinces = listOf(
        "Gauteng",
        "Western Cape",
        "KwaZulu-Natal",
        "Eastern Cape",
        "Free State",
        "Mpumalanga",
        "Limpopo",
        "North West",
        "Northern Cape"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.m)
        ) {
            Text(
                text = "Select Province",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(Spacing.m))

            provinces.forEach { province ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { location = province }
                        .padding(vertical = Spacing.s),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = location == province,
                        onClick = { location = province }
                    )
                    Spacer(modifier = Modifier.width(Spacing.s))
                    Text(text = province)
                }
            }

            Spacer(modifier = Modifier.height(Spacing.l))

            Button(
                onClick = { onSave(location) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllergiesEditSheet(
    currentAllergies: String?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var allergies by remember { mutableStateOf(currentAllergies ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.m)
        ) {
            Text(
                text = "Known Allergies",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Products containing these ingredients will be flagged",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.m))

            OutlinedTextField(
                value = allergies,
                onValueChange = { allergies = it },
                label = { Text("Allergies (comma-separated)") },
                placeholder = { Text("e.g., Fragrance, Parabens, Sulfates") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(Spacing.l))

            Button(
                onClick = { onSave(allergies) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Coral400
            )
        },
        title = {
            Text("Delete All Data?")
        },
        text = {
            Text(
                "This will permanently delete all your scan history, profile data, and preferences. This action cannot be undone."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Coral400
                )
            ) {
                Text("Delete Everything")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
