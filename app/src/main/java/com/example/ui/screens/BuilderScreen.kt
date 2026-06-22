package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.ModResultItem
import com.example.ui.components.SelectedModItem
import com.example.ui.viewmodel.ModpackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuilderScreen(viewModel: ModpackViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showPackSummary by remember { mutableStateOf(false) }

    val mcVersions = listOf("1.21.1", "1.21", "1.20.4", "1.20.1", "1.19.2", "1.18.2", "1.16.5")
    val loaders = listOf("Fabric", "Forge", "NeoForge", "Quilt")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CS Modpack Builder", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showPackSummary = true }) {
                        BadgedBox(
                            badge = {
                                if (uiState.selectedMods.isNotEmpty()) {
                                    Badge { Text(uiState.selectedMods.size.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Inventory, contentDescription = "Summary")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Configuration Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Version Selector
                    var versionExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = versionExpanded,
                        onExpandedChange = { versionExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        TextField(
                            value = uiState.minecraftVersion,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Version") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = versionExpanded) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = versionExpanded,
                            onDismissRequest = { versionExpanded = false }
                        ) {
                            mcVersions.forEach { version ->
                                DropdownMenuItem(
                                    text = { Text(version) },
                                    onClick = {
                                        viewModel.setMinecraftVersion(version)
                                        versionExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Loader Selector
                    var loaderExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = loaderExpanded,
                        onExpandedChange = { loaderExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        TextField(
                            value = uiState.loader,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Loader") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = loaderExpanded) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = loaderExpanded,
                            onDismissRequest = { loaderExpanded = false }
                        ) {
                            loaders.forEach { loader ->
                                DropdownMenuItem(
                                    text = { Text(loader) },
                                    onClick = {
                                        viewModel.setLoader(loader)
                                        loaderExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Tabs for content types
            TabRow(
                selectedTabIndex = when (uiState.projectType) {
                    "mod" -> 0
                    "resourcepack" -> 1
                    "shader" -> 2
                    else -> 0
                }
            ) {
                Tab(
                    selected = uiState.projectType == "mod",
                    onClick = { viewModel.setProjectType("mod") },
                    text = { Text("Mods") }
                )
                Tab(
                    selected = uiState.projectType == "resourcepack",
                    onClick = { viewModel.setProjectType("resourcepack") },
                    text = { Text("Resources") }
                )
                Tab(
                    selected = uiState.projectType == "shader",
                    onClick = { viewModel.setProjectType("shader") },
                    text = { Text("Shaders") }
                )
            }

            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.search(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search Modrinth...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            // Results List
            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.error != null) {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Failed to fetch mods",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            uiState.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Button(
                            onClick = { viewModel.search(searchQuery) },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                } else if (uiState.searchResults.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            "No results for current version/loader",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(uiState.searchResults) { result ->
                            ModResultItem(
                                result = result,
                                isSelected = uiState.selectedMods.any { it.projectId == result.projectId },
                                onAdd = { viewModel.addMod(result) }
                            )
                        }
                    }
                }

                // Compatibility Warning / Dependency resolving indicator
                uiState.resolvingDependenciesFor?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Resolving dependencies...")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPackSummary) {
        PackSummaryDialog(
            viewModel = viewModel,
            onDismiss = { showPackSummary = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackSummaryDialog(
    viewModel: ModpackViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var packName by remember { mutableStateOf("My Custom Pack") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
        ) {
            Text(
                "Modpack Summary",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = packName,
                onValueChange = { packName = it },
                label = { Text("Pack Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Minecraft", style = MaterialTheme.typography.labelSmall)
                        Text(uiState.minecraftVersion, fontWeight = FontWeight.SemiBold)
                    }
                    Column {
                        Text("Loader", style = MaterialTheme.typography.labelSmall)
                        Text(uiState.loader, fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Items", style = MaterialTheme.typography.labelSmall)
                        Text(uiState.selectedMods.size.toString(), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(uiState.selectedMods) { mod ->
                    SelectedModItem(
                        mod = mod,
                        onRemove = { viewModel.removeMod(mod.projectId) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.savePack(packName)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = uiState.selectedMods.isNotEmpty()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate & Save Modpack")
            }
        }
    }
}
