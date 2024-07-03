package com.example.minecraftserverstatus.data.dbLocal

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.minecraftserverstatus.model.Server
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "servers")
@TypeConverters(Converters::class)
data class ServerLocal(
    @PrimaryKey val ip: String,
    var online: Boolean = false,
    var port: Int? = null,
    var hostname: String? = null,
    var debug: Server.Debug? = null,
    var version: String? = null,
    var protocol: Server.Protocol? = null,
    var icon: String? = null,
    var software: String? = null,
    var map: Server.Map? = null,
    var gamemode: String? = null,
    var serverid: String? = null,
    var eulaBlocked: Boolean? = null,
    var motd: Server.Motd? = null,
    var players: Server.Players? = null,
    var plugins: List<Server.Plugin>? = null,
    var mods: List<Server.Mod>? = null,
    var info: Server.Info? = null,
    var isFavorite: Boolean = false
)

fun ServerLocal.toServer(): Server {
    return Server(
        ip = this.ip,
        online = this.online,
        port = this.port,
        hostname = this.hostname,
        debug = this.debug,
        version = this.version,
        protocol = this.protocol,
        icon = this.icon,
        software = this.software,
        map = this.map,
        gamemode = this.gamemode,
        serverid = this.serverid,
        eulaBlocked = this.eulaBlocked,
        motd = this.motd,
        players = this.players,
        plugins = this.plugins,
        mods = this.mods,
        info = this.info,
        isFavorite = this.isFavorite
    )
}

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromDebug(value: String?): Server.Debug? {
        return gson.fromJson(value, object : TypeToken<Server.Debug?>() {}.type)
    }

    @TypeConverter
    fun toDebug(debug: Server.Debug?): String {
        return gson.toJson(debug)
    }

    @TypeConverter
    fun fromProtocol(value: String?): Server.Protocol? {
        return gson.fromJson(value, object : TypeToken<Server.Protocol?>() {}.type)
    }

    @TypeConverter
    fun toProtocol(protocol: Server.Protocol?): String {
        return gson.toJson(protocol)
    }

    @TypeConverter
    fun fromMap(value: String?): Server.Map? {
        return gson.fromJson(value, object : TypeToken<Server.Map?>() {}.type)
    }

    @TypeConverter
    fun toMap(map: Server.Map?): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun fromMotd(value: String?): Server.Motd? {
        return gson.fromJson(value, object : TypeToken<Server.Motd?>() {}.type)
    }

    @TypeConverter
    fun toMotd(motd: Server.Motd?): String {
        return gson.toJson(motd)
    }

    @TypeConverter
    fun fromPlayers(value: String?): Server.Players? {
        return gson.fromJson(value, object : TypeToken<Server.Players?>() {}.type)
    }

    @TypeConverter
    fun toPlayers(players: Server.Players?): String {
        return gson.toJson(players)
    }

    @TypeConverter
    fun fromPlugins(value: String?): List<Server.Plugin>? {
        return gson.fromJson(value, object : TypeToken<List<Server.Plugin>?>() {}.type)
    }

    @TypeConverter
    fun toPlugins(plugins: List<Server.Plugin>?): String {
        return gson.toJson(plugins)
    }

    @TypeConverter
    fun fromMods(value: String?): List<Server.Mod>? {
        return gson.fromJson(value, object : TypeToken<List<Server.Mod>?>() {}.type)
    }

    @TypeConverter
    fun toMods(mods: List<Server.Mod>?): String {
        return gson.toJson(mods)
    }

    @TypeConverter
    fun fromInfo(value: String?): Server.Info? {
        return gson.fromJson(value, object : TypeToken<Server.Info?>() {}.type)
    }

    @TypeConverter
    fun toInfo(info: Server.Info?): String {
        return gson.toJson(info)
    }
}
