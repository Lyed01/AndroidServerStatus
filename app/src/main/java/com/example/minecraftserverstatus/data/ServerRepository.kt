package com.example.minecraftserverstatus.data

import android.content.Context
import androidx.compose.ui.text.rememberTextMeasurer
import com.example.minecraftserverstatus.data.dbLocal.ServerLocal
import com.example.minecraftserverstatus.model.Server

class ServerRepository(private val context: Context) {


    suspend fun getServer(ip: String): Server? {
        return ServerDataSource.getServer(ip, context)
    }

    suspend fun fetchFavoriteServers(): List<Server> {
        return ServerDataSource.getFavoriteServers(context)
    }

    suspend fun addServerToFavorites(server: Server): Boolean {
        return ServerDataSource.addServerToFavorites(server, context)
    }

    suspend fun removeServerFromFavorites(server: Server): Boolean {
        return ServerDataSource.removeServerFromFavorites(server, context)
    }

    suspend fun getAllServersFromRoom(): List<ServerLocal> {
        return ServerDataSource.getAllServersFromRoom(context)
    }

    suspend fun addServerToRoom(server: ServerLocal) {
        ServerDataSource.addServerToRoom(context, server)
    }

    suspend fun deleteServerFromRoom(ip: String) {
        ServerDataSource.deleteServerFromRoom(context, ip)
    }
}

