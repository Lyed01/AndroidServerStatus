package com.lyed.minecraftserverstatus.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lyed.minecraftserverstatus.data.ServerRepository
import com.lyed.minecraftserverstatus.data.dbLocal.toServer
import com.lyed.minecraftserverstatus.model.Server
import kotlinx.coroutines.launch
class ServerViewModel(application: Application) : AndroidViewModel(application) {

    private val serverRepository: ServerRepository = ServerRepository(application)
    private var originalServers: List<Server> = emptyList() // Lista original de servidores
    private var currentFilter: String? = null // Filtro actual aplicado
    var sortByPlayersAsc: Boolean? = null
        private set


    private val _servers = MutableLiveData<List<Server>>()
    val servers: LiveData<List<Server>>
        get() = _servers

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val favoriteServers = mutableMapOf<String, Boolean>()

    init {
        _servers.value = emptyList()
        _isLoading.value = false

        viewModelScope.launch {
            try {
                // Obtener todos los servidores favoritos de Firebase
                val favoriteServersFromFirebase = serverRepository.fetchFavoriteServers()
                favoriteServersFromFirebase.forEach { server ->
                    favoriteServers[server.ip] = true
                }

                // Obtener todos los servidores locales de la Room
                val localServers = serverRepository.getAllServersFromRoom()

                // Crear una lista para almacenar los servidores combinados
                val combinedServers = mutableListOf<Server>()

                // Agregar todos los servidores locales a la lista combinada
                localServers.forEach { localServer ->
                    val existingFavorite =
                        favoriteServersFromFirebase.find { it.ip == localServer.ip }
                    if (existingFavorite != null) {
                        // Si el servidor local también es un favorito, usar los datos de favoritos
                        combinedServers.add(existingFavorite.copy(isFavorite = true))
                    } else {
                        // Si el servidor local no es un favorito, agregarlo con isFavorite = false
                        combinedServers.add(localServer.toServer().copy(isFavorite = false))
                    }
                }

                // Agregar servidores favoritos que no están en la base de datos local
                favoriteServersFromFirebase.forEach { favoriteServer ->
                    if (combinedServers.none { it.ip == favoriteServer.ip }) {
                        combinedServers.add(favoriteServer.copy(isFavorite = true))
                    }
                }

                // Actualizar LiveData con la lista combinada y originalServers
                _servers.postValue(combinedServers)
                originalServers = combinedServers // Actualizar originalServers

            } catch (e: Exception) {
                Log.e("ServerViewModel", "Error fetching servers", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Función para agregar un servidor a favoritos
    fun addServerToFavorites(server: Server) {
        viewModelScope.launch {
            try {
                val success = serverRepository.addServerToFavorites(server)
                if (success) {
                    favoriteServers[server.ip ?: ""] = true
                    server.isFavorite = true
                    updateServerInList(server)
                    Log.d("ServerViewModel", "Server added to favorites successfully")
                } else {
                    Log.e("ServerViewModel", "Failed to add server to favorites")
                }
            } catch (e: Exception) {
                Log.e("ServerViewModel", "Error adding server to favorites", e)
            }
        }
    }

    // Función para eliminar un servidor de favoritos
    fun removeServerFromFavorites(server: Server) {
        viewModelScope.launch {
            try {
                val success = serverRepository.removeServerFromFavorites(server)
                if (success) {
                    favoriteServers.remove(server.ip)
                    server.isFavorite = false
                    updateServerInList(server)
                    Log.d("ServerViewModel", "Server removed from favorites successfully")
                } else {
                    Log.e("ServerViewModel", "Failed to remove server from favorites")
                }
            } catch (e: Exception) {
                Log.e("ServerViewModel", "Error removing server from favorites", e)
            }
        }
    }

    // Función para verificar si un servidor es favorito
    fun isServerFavorite(ip: String): Boolean {
        return favoriteServers[ip] ?: false
    }

    // Función para refrescar la lista de servidores
    fun refreshServers() {
        viewModelScope.launch {
            _isLoading.postValue(true)

            try {
                val currentServers = originalServers // Usar originalServers en lugar de _servers

                currentServers.forEachIndexed { index, server ->
                    val identifier =
                        if (!server.hostname.isNullOrEmpty()) server.hostname else server.ip ?: ""
                    val updatedServer = identifier?.let { serverRepository.getServer(it) }

                    updatedServer?.copy(
                        isFavorite = favoriteServers[updatedServer?.ip ?: ""] ?: false
                    ) ?: server

                    if (updatedServer != null) {
                        val updatedServerWithDefaults = server.copy(
                            online = updatedServer.online,
                            port = updatedServer.port ?: server.port,
                            hostname = updatedServer.hostname ?: server.hostname,
                            debug = updatedServer.debug ?: server.debug,
                            version = updatedServer.version ?: server.version,
                            protocol = updatedServer.protocol ?: server.protocol,
                            icon = updatedServer.icon ?: server.icon,
                            software = updatedServer.software ?: server.software,
                            map = updatedServer.map ?: server.map,
                            gamemode = updatedServer.gamemode ?: server.gamemode,
                            serverid = updatedServer.serverid ?: server.serverid,
                            eulaBlocked = updatedServer.eulaBlocked ?: server.eulaBlocked,
                            motd = updatedServer.motd ?: server.motd,
                            players = updatedServer.players ?: server.players,
                            plugins = updatedServer.plugins ?: server.plugins,
                            mods = updatedServer.mods ?: server.mods,
                            info = updatedServer.info ?: server.info
                        )

                        // Update Room database with the updated server
                        serverRepository.addServerToRoom(updatedServerWithDefaults.toServerLocal())

                        // Update _servers LiveData
                        val updatedList = _servers.value?.toMutableList() ?: mutableListOf()
                        updatedList[index] = updatedServerWithDefaults
                        _servers.postValue(updatedList)
                    } else {
                        Log.d("ServerViewModel", "Failed to fetch server data for: $identifier")
                    }
                }

            } catch (e: Exception) {
                Log.e("ServerViewModel", "Error refreshing servers", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }


    // Función para editar un servidor
    fun editServer(server: Server, newIp: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)

            deleteServer(server.ip ?: "")
            val editedServer = server.copy(ip = newIp)
            addServer(editedServer.ip ?: "") {
                _isLoading.postValue(false)
            }
        }
    }

    // Función para agregar un servidor
    fun addServer(ip: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.postValue(true)

            try {
                val server = serverRepository.getServer(ip)
                val serverLocal = server?.toServerLocal()
                if (serverLocal != null) {
                    serverRepository.addServerToRoom(serverLocal)
                }

                server?.let {
                    it.isFavorite = isServerFavorite(it.ip ?: "")
                    addServerToList(it)
                }
                onComplete()
            } catch (e: Exception) {
                Log.e("ServerViewModel", "Error adding server", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
    fun deleteServer(ip: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)

            try {
                // Eliminar el servidor de la lista original
                originalServers = originalServers.filter { it.ip != ip }

                // Actualizar _servers si es necesario
                val currentServers = _servers.value?.toMutableList() ?: mutableListOf()
                currentServers.removeAll { it.ip == ip }
                _servers.postValue(currentServers)

                // Eliminar el servidor de la base de datos local
                serverRepository.deleteServerFromRoom(ip)

                Log.d("ServerViewModel", "Server deleted successfully")
            } catch (e: Exception) {
                Log.e("ServerViewModel", "Error deleting server", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }



    // Función para actualizar un servidor en la lista
    // Función para actualizar un servidor en la lista
    private fun updateServerInList(server: Server) {
        val currentServers = _servers.value?.toMutableList() ?: return
        val index = currentServers.indexOfFirst { it.ip == server.ip }
        if (index != -1) {
            currentServers[index] = server
            _servers.postValue(currentServers)
        }
    }

    // Función para agregar un servidor a la lista
    private fun addServerToList(server: Server) {
        val currentServers = _servers.value?.toMutableList() ?: mutableListOf()
        currentServers.add(server)
        _servers.postValue(currentServers)
        originalServers = originalServers + server // Actualizar originalServers
    }


    fun filterServersByHostname(query: String?) {
        val filteredList = applyHostnameFilter(query)
        _servers.postValue(filteredList)
    }

    // Aplicar filtro de búsqueda por hostname
    private fun applyHostnameFilter(query: String?): List<Server> {
        return if (query.isNullOrBlank()) {
            originalServers // Si no hay consulta, devuelve la lista original
        } else {
            originalServers.filter { server ->
                val hostname = server.hostname?.toLowerCase() ?: ""
                hostname.contains(query.toLowerCase())
            }
        }
    }


    // Filtrar todos los servidores
    fun filterAllServers() {
        currentFilter = null
        applyCurrentFilters()
    }

    // Filtrar por servidores favoritos
    fun filterFavoriteServers() {
        currentFilter = "favorites"
        applyCurrentFilters()
    }

    // Filtrar por cantidad de jugadores ascendente o descendente
    fun filterServersByPlayerCount(ascending: Boolean?) {
        sortByPlayersAsc = ascending
        applyCurrentFilters()
    }

    private fun applyCurrentFilters() {
        var filteredList = originalServers.toList() // Empieza con la lista completa

        // Aplica filtro de favoritos si está activo
        if (currentFilter == "favorites") {
            filteredList = filteredList.filter { isServerFavorite(it.ip ?: "") }
        }

        // Aplica filtro ascendente o descendente de jugadores online si está activo
        sortByPlayersAsc?.let { ascending ->
            filteredList = if (ascending) {
                filteredList.sortedBy { it.players?.online }
            } else {
                filteredList.sortedByDescending { it.players?.online }
            }
        }



        // Actualiza la lista filtrada en LiveData
        _servers.postValue(filteredList)
    }
}
