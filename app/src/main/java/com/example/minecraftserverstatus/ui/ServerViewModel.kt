package com.example.minecraftserverstatus.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minecraftserverstatus.data.ServerRepository
import com.example.minecraftserverstatus.model.Server
import kotlinx.coroutines.launch

class ServerViewModel(private val serverRepository: ServerRepository) : ViewModel() {

    constructor() : this(ServerRepository()) {
        // Puedes inicializar el repositorio con un valor predeterminado si es necesario
    }

    private val _servers = MutableLiveData<List<Server>>()
    val servers: LiveData<List<Server>>
        get() = _servers

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    // Mapa mutable para almacenar los servidores favoritos
    private val favoriteServers = mutableMapOf<String, Boolean>()

    init {
        _servers.value = emptyList()
        _isLoading.value = false

        viewModelScope.launch {
            try {
                val favoriteServers = serverRepository.fetchFavoriteServers()
                addFavoriteServersToList(favoriteServers)
            } catch (e: Exception) {
                Log.e("ServerViewModel", "Error fetching favorite servers", e)
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
                val currentServers = _servers.value ?: emptyList()

                currentServers.forEachIndexed { index, server ->
                    val identifier = if (!server.hostname.isNullOrEmpty()) server.hostname else server.ip ?: ""
                    val updatedServer = identifier?.let { serverRepository.getServer(it) }

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
                server?.let { addServerToList(it) }
                onComplete()
            } catch (e: Exception) {
                Log.e("ServerViewModel", "Error adding server", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Función para eliminar un servidor
    fun deleteServer(ip: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)

            try {
                val currentServers = _servers.value?.toMutableList() ?: return@launch
                currentServers.removeAll { it.ip == ip }
                _servers.postValue(currentServers)
            } catch (e: Exception) {
                Log.e("ServerViewModel", "Error deleting server", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

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
    }

    // Función para agregar servidores favoritos a la lista
    private fun addFavoriteServersToList(servers: List<Server>) {
        val currentServers = _servers.value?.toMutableList() ?: mutableListOf()
        currentServers.addAll(servers)
        _servers.postValue(currentServers)
    }
}
