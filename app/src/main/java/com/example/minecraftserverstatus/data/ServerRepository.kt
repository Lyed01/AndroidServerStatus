package com.example.minecraftserverstatus.data

import com.example.minecraftserverstatus.model.Server

class ServerRepository {
    suspend fun getServer(ip: String): Server? {
        return ServerDataSource.getServer(ip)
    }
    suspend fun getServerbyhostname(hostname: String): Server? {
        return ServerDataSource.getServer(hostname)
    }
}
