package com.example.minecraftserverstatus.data.dbLocal

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.minecraftserverstatus.model.Server
import com.google.gson.annotations.SerializedName

@Entity(tableName = "servers" )
data class ServerLocal (

    @PrimaryKey val ip: String,
    var online: Boolean = false,
    var port: Int? = null,
    var hostname: String? = null,
    var version: String? = null,
    var icon: String? = null,
    var software: String? = null,
    var gamemode: String? = null,
    var serverid: String? = null,
    @SerializedName("eula_blocked") var eulaBlocked: Boolean? = null,

    var isFavorite: Boolean = false

)

fun ServerLocal.toServer(): Server {
    return Server(
        ip = this.ip,
        online = this.online,
        port = this.port,
        hostname = this.hostname,
        version = this.version,
        protocol = Server.Protocol(0, null),
        icon = this.icon,
        software = this.software,
        map = Server.Map("", "", ""),
        gamemode = this.gamemode,
        serverid = this.serverid,
        eulaBlocked = this.eulaBlocked,
        motd = Server.Motd(emptyList(), emptyList(), emptyList()),
        players = Server.Players(0, 0, null),
        plugins = emptyList(),
        mods = emptyList(),
        info = Server.Info(emptyList(), emptyList(), emptyList()),
        isFavorite = this.isFavorite ?: false // Default value for isFavorite
    )

}