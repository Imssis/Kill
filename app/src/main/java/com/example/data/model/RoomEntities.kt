package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "modpacks")
data class ModpackEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val minecraftVersion: String,
    val loader: String,
    val selectedProjectIds: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class ModItem(
    val projectId: String,
    val title: String,
    val iconUrl: String?,
    val author: String,
    val projectType: String, // mod, resourcepack, shader
    val latestVersionId: String? = null,
    val filename: String? = null,
    val downloadUrl: String? = null
)
