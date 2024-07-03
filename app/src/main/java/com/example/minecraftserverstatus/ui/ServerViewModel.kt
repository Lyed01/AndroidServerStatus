package com.example.minecraftserverstatus.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.minecraftserverstatus.data.ServerRepository
import com.example.minecraftserverstatus.data.dbLocal.ServerLocal
import com.example.minecraftserverstatus.data.dbLocal.toServer
import com.example.minecraftserverstatus.model.Server
import kotlinx.coroutines.launch

class ServerViewModel(application: Application) : AndroidViewModel(application) {

    private val serverRepository: ServerRepository = ServerRepository(application)
    private var originalServers: List<Server> = emptyList() // Lista original de servidores
    private var currentFilter: String? = null // Filtro actual aplicado

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
                val serverroom = serverRepository.getServer(ip)
                val serverLocal = serverroom?.toServerLocal()
                if (serverLocal != null) {
                    serverRepository.addServerToRoom(serverLocal)
                }

                val server = serverRepository.getServer(ip)
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

    // Función para eliminar un servidor
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


    fun filterServersByHostname(query: String?) {
        val filteredList = if (query.isNullOrBlank()) {
            originalServers.toList() // Si no hay filtro, mostrar todos los servidores originales
        } else {
            originalServers.filter { server ->
                server.hostname?.contains(query, ignoreCase = true) == true ||
                        server.ip?.contains(query, ignoreCase = true) == true
            }
        }
        _servers.postValue(filteredList)
    }

    fun filterAllServers() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val currentServers = originalServers
                _servers.postValue(currentServers)
            } catch (e: Exception) {
                Log.e("ServerViewModel", "Error filtering all servers", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun filterFavoriteServers() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val favoriteServers = originalServers.filter { isServerFavorite(it.ip ?: "") }
                _servers.postValue(favoriteServers)
            } catch (e: Exception) {
                Log.e("ServerViewModel", "Error filtering favorite servers", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    //IMPLEMENTAR BUSQUEDA Y FILTRO A LA VEZ Y FILTROS SOBRE SERVERS QUE NO ESTEN EN ROOM NI FAVORITOS


}