package com.example.data.database

import androidx.room.*
import com.example.data.model.ModpackEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Dao
interface ModpackDao {
    @Query("SELECT * FROM modpacks ORDER BY timestamp DESC")
    fun getAllModpacks(): Flow<List<ModpackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModpack(modpack: ModpackEntity)

    @Query("DELETE FROM modpacks WHERE id = :id")
    suspend fun deleteModpack(id: Int)

    @Query("SELECT * FROM modpacks WHERE id = :id")
    suspend fun getModpackById(id: Int): ModpackEntity?
}

@Database(entities = [ModpackEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun modpackDao(): ModpackDao
}

class Converters {
    @TypeConverter
    fun fromList(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun toList(value: String): List<String> = Json.decodeFromString(value)
}
