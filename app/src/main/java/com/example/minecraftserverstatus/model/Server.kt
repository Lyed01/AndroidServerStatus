package com.example.minecraftserverstatus.model

import com.google.gson.annotations.SerializedName

data class Server(
    val ip: String,
    var online: Boolean = false,
    var port: Int? = null,
    var hostname: String? = null,
    var debug: Debug? = null,
    var version: String? = null,
    var protocol: Protocol? = null,
    var icon: String? = null,
    var software: String? = null,
    var map: Map? = null,
    var gamemode: String? = null,
    var serverid: String? = null,
    @SerializedName("eula_blocked") var eulaBlocked: Boolean? = null,
    var motd: Motd? = null,
    var players: Players? = null,
    var plugins: List<Plugin>? = null,
    var mods: List<Mod>? = null,
    var info: Info? = null
) {
    data class Debug(
        val ping: Boolean,
        val query: Boolean,
        val srv: Boolean,
        val querymismatch: Boolean,
        val ipinsrv: Boolean,
        val cnameinsrv: Boolean,
        val animatedmotd: Boolean,
        val cachehit: Boolean,
        val cachetime: Long,
        val cacheexpire: Long,
        val apiversion: Int
    )

    data class Protocol(
        val version: Int,
        val name: String?
    )

    data class Map(
        val raw: String,
        val clean: String,
        val html: String
    )

    data class Motd(
        val raw: List<String>,
        val clean: List<String>,
        val html: List<String>
    )

    data class Players(
        val online: Int,
        val max: Int,
        val list: List<Player>?
    )

    data class Player(
        val name: String,
        val uuid: String
    )

    data class Plugin(
        val name: String,
        val version: String
    )

    data class Mod(
        val name: String,
        val version: String
    )

    data class Info(
        val raw: List<String>,
        val clean: List<String>,
        val html: List<String>
    )

}