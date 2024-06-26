package com.example.minecraftserverstatus.data

import android.util.Log
import com.example.minecraftserverstatus.model.Server
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ServerDataSource {
    companion object {
        private const val API_BASE_URL = "https://api.mcsrvstat.us/"
        private val api: ServerAPI

        init {
            api = Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ServerAPI::class.java)
        }

        suspend fun getServer(ip: String): Server? {
            return try {
                val result = api.getServer(ip)
                Log.d("Demo_API_MINECRAFT", "Server DataSource exitoso")
                result
            } catch (e: Exception) {
                Log.e("Demo_API_MINECRAFT", "Error llamando a la API: ${e.message}")
                null
            }
        }
    }
}
