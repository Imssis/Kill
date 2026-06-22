package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchResponse(
    val hits: List<SearchResult>,
    val offset: Int,
    val limit: Int,
    @Json(name = "total_hits") val totalHits: Int
)

@JsonClass(generateAdapter = true)
data class SearchResult(
    @Json(name = "project_id") val projectId: String,
    val title: String,
    val description: String,
    val author: String,
    @Json(name = "icon_url") val iconUrl: String?,
    val categories: List<String>?,
    val versions: List<String>?,
    val downloads: Int,
    val follows: Int,
    @Json(name = "date_created") val dateCreated: String,
    @Json(name = "date_modified") val dateModified: String,
    @Json(name = "latest_version") val latestVersion: String?,
    @Json(name = "project_type") val projectType: String,
    val gallery: List<String>?
)

@JsonClass(generateAdapter = true)
data class ProjectVersion(
    val id: String,
    @Json(name = "project_id") val projectId: String,
    @Json(name = "author_id") val authorId: String,
    val name: String,
    @Json(name = "version_number") val versionNumber: String,
    val changelog: String?,
    val dependencies: List<ProjectDependency>?,
    @Json(name = "game_versions") val gameVersions: List<String>,
    @Json(name = "version_type") val versionType: String,
    val loaders: List<String>,
    val featured: Boolean,
    val files: List<ProjectFile>
)

@JsonClass(generateAdapter = true)
data class ProjectDependency(
    @Json(name = "version_id") val versionId: String?,
    @Json(name = "project_id") val projectId: String?,
    @Json(name = "file_name") val fileName: String?,
    @Json(name = "dependency_type") val dependencyType: String // "required", "optional", "incompatible", "embedded"
)

@JsonClass(generateAdapter = true)
data class ProjectFile(
    val hashes: Map<String, String>,
    val url: String,
    val filename: String,
    val primary: Boolean,
    val size: Int,
    @Json(name = "file_type") val fileType: String?
)
