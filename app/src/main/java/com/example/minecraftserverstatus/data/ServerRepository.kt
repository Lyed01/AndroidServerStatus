package com.example.minecraftserverstatus.data

import com.example.minecraftserverstatus.model.Server

class ServerRepository {
    suspend fun getServer(ip: String): Server? {
        return ServerDataSource.getServer(ip)
    }
    suspend fun fetchFavoriteServers(): List<Server> {
        return ServerDataSource.getFavoriteServers()
    }

    suspend fun addServerToFavorites(server: Server): Boolean {
        return ServerDataSource.addServerToFavorites(server)
    }
    suspend fun removeServerFromFavorites(server: Server): Boolean {
        return ServerDataSource.removeServerFromFavorites(server)
    }
}
