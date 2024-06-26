package com.example.minecraftserverstatus.data

import com.example.minecraftserverstatus.model.Server
import retrofit2.http.GET
import retrofit2.http.Path

interface ServerAPI {
    @GET("3/{ip}")
    suspend fun getServer(
        @Path("ip") ip: String
    ): Server
}
