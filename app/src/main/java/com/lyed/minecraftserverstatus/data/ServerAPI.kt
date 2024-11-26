package com.lyed.minecraftserverstatus.data

import com.lyed.minecraftserverstatus.model.Server
import retrofit2.http.GET
import retrofit2.http.Path

interface ServerAPI {
    @GET("3/{ip}")
    suspend fun getServer(
        @Path("ip") ip: String
    ): Server
}
