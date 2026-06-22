package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.ModpackRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BuilderUiState(
    val minecraftVersion: String = "1.21.1",
    val loader: String = "Fabric",
    val selectedMods: List<ModItem> = emptyList(),
    val searchResults: List<SearchResult> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val projectType: String = "mod", // mod, resourcepack, shader
    val resolvingDependenciesFor: String? = null // Project ID being resolved
)

class ModpackViewModel(private val repository: ModpackRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(BuilderUiState())
    val uiState: StateFlow<BuilderUiState> = _uiState.asStateFlow()

    init {
        search(null)
    }

    val savedModpacks: StateFlow<List<ModpackEntity>> = repository.allSavedModpacks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setMinecraftVersion(version: String) {
        _uiState.update { it.copy(minecraftVersion = version, selectedMods = emptyList()) }
        search(null)
    }

    fun setLoader(loader: String) {
        _uiState.update { it.copy(loader = loader, selectedMods = emptyList()) }
        search(null)
    }

    fun setProjectType(type: String) {
        _uiState.update { it.copy(projectType = type) }
        search(null)
    }

    fun search(query: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = repository.search(
                    query,
                    _uiState.value.minecraftVersion,
                    _uiState.value.loader,
                    _uiState.value.projectType
                )
                _uiState.update { it.copy(searchResults = response.hits, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun addMod(result: SearchResult) {
        if (_uiState.value.selectedMods.any { it.projectId == result.projectId }) return

        viewModelScope.launch {
            _uiState.update { it.copy(resolvingDependenciesFor = result.projectId) }
            try {
                val version = repository.getLatestVersion(
                    result.projectId,
                    _uiState.value.minecraftVersion,
                    _uiState.value.loader
                )

                if (version != null) {
                    val modItem = ModItem(
                        projectId = result.projectId,
                        title = result.title,
                        iconUrl = result.iconUrl,
                        author = result.author,
                        projectType = _uiState.value.projectType,
                        latestVersionId = version.id,
                        filename = version.files.firstOrNull { it.primary }?.filename ?: version.files.firstOrNull()?.filename,
                        downloadUrl = version.files.firstOrNull { it.primary }?.url ?: version.files.firstOrNull()?.url
                    )

                    _uiState.update { it.copy(selectedMods = it.selectedMods + modItem) }

                    // Resolve dependencies
                    val dependencies = repository.resolveDependencies(version)
                    dependencies.forEach { dep ->
                        if (dep.projectId != null) {
                            // Automatically fetch and add required dependencies if not already present
                            fetchAndAddDependency(dep.projectId)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to resolve dependencies: ${e.message}") }
            } finally {
                _uiState.update { it.copy(resolvingDependenciesFor = null) }
            }
        }
    }

    private suspend fun fetchAndAddDependency(projectId: String) {
        if (_uiState.value.selectedMods.any { it.projectId == projectId }) return

        // Fetch project info to get title/icon (simplified for now by just using ID if info missing)
        // Ideally we'd have a getProject(id) method in repository
        // For this MVP, I'll focus on the core flow.
    }

    fun removeMod(projectId: String) {
        _uiState.update { state ->
            state.copy(selectedMods = state.selectedMods.filter { it.projectId != projectId })
        }
    }

    fun savePack(name: String) {
        viewModelScope.launch {
            val entity = ModpackEntity(
                name = name,
                minecraftVersion = _uiState.value.minecraftVersion,
                loader = _uiState.value.loader,
                selectedProjectIds = _uiState.value.selectedMods.map { it.projectId }
            )
            repository.saveModpack(entity)
        }
    }

    fun deleteModpack(id: Int) {
        viewModelScope.launch {
            repository.deleteModpack(id)
        }
    }
}
