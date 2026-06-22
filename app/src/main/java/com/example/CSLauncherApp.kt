package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.api.ModrinthService
import com.example.data.database.AppDatabase
import com.example.data.repository.ModpackRepository
import com.example.ui.viewmodel.ModpackViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class CSLauncherApp : Application() {

    lateinit var repository: ModpackRepository

    override fun onCreate() {
        super.onCreate()

        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "cslauncher.db"
        ).build()

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.modrinth.com/v2/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        val service = retrofit.create(ModrinthService::class.java)

        repository = ModpackRepository(service, database.modpackDao())
    }
}
