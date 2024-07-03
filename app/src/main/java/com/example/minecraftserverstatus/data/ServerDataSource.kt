package com.example.minecraftserverstatus.data

import android.content.Context
import android.util.Log
import com.example.minecraftserverstatus.data.dbLocal.AppDataBase
import com.example.minecraftserverstatus.data.dbLocal.ServerDAO
import com.example.minecraftserverstatus.data.dbLocal.ServerLocal
import com.example.minecraftserverstatus.model.Server
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ServerDataSource(private val context: Context) {

    private val serverDao: ServerDAO by lazy { AppDataBase.getInstance(context).serverDao() }

    companion object {
        private const val API_BASE_URL = "https://api.mcsrvstat.us/"
        private val api: ServerAPI
        private val db = FirebaseFirestore.getInstance()
        private val auth = FirebaseAuth.getInstance()

        init {
            api = Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ServerAPI::class.java)
        }

        private fun getUserId(): String? {
            return auth.currentUser?.uid
        }

        suspend fun getServer(ip: String, context: Context): Server? {
            return try {
                val result = api.getServer(ip)
                Log.d("Demo_API_MINECRAFT", "Server DataSource exitoso")
                result
            } catch (e: Exception) {
                Log.e("Demo_API_MINECRAFT", "Error llamando a la API: ${e.message}")
                null
            }
        }

        suspend fun addServerToFavorites(server: Server, context: Context): Boolean {
            val userId = getUserId() ?: return false
            return try {
                db.collection("users").document(userId).collection("favorite_servers")
                    .document(server.ip ?: "").set(server).await()
                true
            } catch (e: Exception) {
                Log.e("ServerDataSource", "Error adding server to favorites", e)
                false
            }
        }

        suspend fun removeServerFromFavorites(server: Server, context: Context): Boolean {
            val userId = getUserId() ?: return false
            return try {
                db.collection("users").document(userId).collection("favorite_servers")
                    .document(server.ip ?: "").delete().await()
                true
            } catch (e: Exception) {
                Log.e("ServerDataSource", "Error removing server from favorites", e)
                false
            }
        }

        suspend fun getFavoriteServers(context: Context): List<Server> {
            val userId = getUserId() ?: return emptyList()
            val favoriteServers = mutableListOf<Server>()

            try {
                val snapshot = db.collection("users").document(userId)
                    .collection("favorite_servers").get().await()
                for (document in snapshot.documents) {
                    val server = document.toObject(Server::class.java)
                    server?.let { favoriteServers.add(it) }
                }
            } catch (e: Exception) {
                Log.e("ServerDataSource", "Error retrieving favorite servers", e)
            }

            return favoriteServers
        }

        suspend fun getAllServersFromRoom(context: Context): List<ServerLocal> {
            return withContext(Dispatchers.IO) {
                val serverDao = AppDataBase.getInstance(context).serverDao()
                serverDao.getAll()
            }
        }

        suspend fun addServerToRoom(context: Context, server: ServerLocal) {
            withContext(Dispatchers.IO) {
                val serverDao = AppDataBase.getInstance(context).serverDao()
                serverDao.insert(server)
            }
        }

        suspend fun deleteServerFromRoom(context: Context, ip: String) {
            withContext(Dispatchers.IO) {
                val serverDao = AppDataBase.getInstance(context).serverDao()
                serverDao.delete(ServerLocal(ip))
            }
        }
    }
}

