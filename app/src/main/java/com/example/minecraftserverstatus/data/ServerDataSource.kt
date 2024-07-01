package com.example.minecraftserverstatus.data

import android.util.Log
import com.example.minecraftserverstatus.model.Server
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ServerDataSource {
    companion object {
        private const val API_BASE_URL = "https://api.mcsrvstat.us/"
        private val api: ServerAPI
        private val db = FirebaseFirestore.getInstance()

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

        suspend fun addServerToFavorites(server: Server): Boolean {
            return try {
                db.collection("favorite_servers").document(server.ip ?: "").set(server).await()
                true
            } catch (e: Exception) {
                Log.e("ServerDataSource", "Error adding server to favorites", e)
                false
            }
        }

        suspend fun removeServerFromFavorites(server: Server): Boolean {
            return try {
                db.collection("favorite_servers").document(server.ip ?: "").delete().await()
                true
            } catch (e: Exception) {
                Log.e("ServerDataSource", "Error removing server from favorites", e)
                false
            }
        }

        suspend fun getFavoriteServers(): List<Server> {
            val favoriteServers = mutableListOf<Server>()

            try {
                val snapshot = db.collection("favorite_servers").get().await()
                for (document in snapshot.documents) {
                    val server = document.toObject(Server::class.java)
                    server?.let { favoriteServers.add(it) }
                }
            } catch (e: Exception) {
                Log.e("ServerDataSource", "Error retrieving favorite servers", e)
            }

            return favoriteServers
        }
    }
}
