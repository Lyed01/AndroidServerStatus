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
    var info: Info? = null,
    var isFavorite: Boolean = false
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
    ) {
        constructor() : this(false, false, false, false, false, false, false, false, 0L, 0L, 0)
    }

    data class Protocol(
        val version: Int,
        val name: String?
    ) {
        constructor() : this(0, null)
    }

    data class Map(
        val raw: String,
        val clean: String,
        val html: String
    ) {
        constructor() : this("", "", "")
    }

    data class Motd(
        val raw: List<String>,
        val clean: List<String>,
        val html: List<String>
    ) {
        constructor() : this(emptyList(), emptyList(), emptyList())
    }

    data class Players(
        val online: Int,
        val max: Int,
        val list: List<Player>?
    ) {
        constructor() : this(0, 0, null)
    }

    data class Player(
        val name: String,
        val uuid: String
    ) {
        constructor() : this("", "")
    }

    data class Plugin(
        val name: String,
        val version: String
    ) {
        constructor() : this("", "")
    }

    data class Mod(
        val name: String,
        val version: String
    ) {
        constructor() : this("", "")
    }

    data class Info(
        val raw: List<String>,
        val clean: List<String>,
        val html: List<String>
    ) {
        constructor() : this(emptyList(), emptyList(), emptyList())
    }

    // Constructor sin argumentos para la clase Server principal
    constructor() : this("", false)
}
