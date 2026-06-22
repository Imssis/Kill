package com.example.data.api

import com.example.data.model.ProjectVersion
import com.example.data.model.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface ModrinthService {
    @Headers("User-Agent: CSLauncherV2/1.0.0 (imissyoumaa5@gmail.com)")
    @GET("search")
    suspend fun search(
        @Query("query") query: String? = null,
        @Query("facets") facets: String? = null,
        @Query("index") index: String = "relevance",
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20
    ): SearchResponse

    @Headers("User-Agent: CSLauncherV2/1.0.0 (imissyoumaa5@gmail.com)")
    @GET("project/{id}/version")
    suspend fun getProjectVersions(
        @Path("id") projectId: String,
        @Query("loaders") loaders: String? = null,
        @Query("game_versions") gameVersions: String? = null
    ): List<ProjectVersion>
}
