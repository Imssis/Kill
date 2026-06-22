package com.example.data.repository

import com.example.data.api.ModrinthService
import com.example.data.database.ModpackDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class ModpackRepository(
    private val modrinthService: ModrinthService,
    private val modpackDao: ModpackDao
) {
    val allSavedModpacks: Flow<List<ModpackEntity>> = modpackDao.getAllModpacks()

    suspend fun search(
        query: String?,
        minecraftVersion: String,
        loader: String,
        projectType: String? = "mod"
    ): SearchResponse {
        val facetsList = mutableListOf<String>()
        facetsList.add("[\"versions:$minecraftVersion\"]")
        facetsList.add("[\"loaders:${loader.lowercase()}\"]")
        if (projectType != null) {
            facetsList.add("[\"project_type:$projectType\"]")
        }
        val facetsString = "[${facetsList.joinToString(",")}]"
        
        // Clean up query if empty
        val finalQuery = if (query.isNullOrBlank()) null else query
        
        return modrinthService.search(finalQuery, facetsString)
    }

    suspend fun getLatestVersion(
        projectId: String,
        minecraftVersion: String,
        loader: String
    ): ProjectVersion? {
        val versions = modrinthService.getProjectVersions(
            projectId,
            loaders = "[\"${loader.lowercase()}\"]",
            gameVersions = "[\"$minecraftVersion\"]"
        )
        // Find the first version that matches our criteria (case-insensitive for loaders)
        return versions.firstOrNull { version ->
            version.gameVersions.contains(minecraftVersion) && 
            version.loaders.any { it.equals(loader, ignoreCase = true) }
        }
    }

    suspend fun resolveDependencies(
        version: ProjectVersion
    ): List<ProjectDependency> {
        return version.dependencies?.filter { it.dependencyType == "required" } ?: emptyList()
    }

    suspend fun saveModpack(modpack: ModpackEntity) {
        modpackDao.insertModpack(modpack)
    }

    suspend fun deleteModpack(id: Int) {
        modpackDao.deleteModpack(id)
    }
}
