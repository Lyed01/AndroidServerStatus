package com.example.minecraftserverstatus.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minecraftserverstatus.data.ServerRepository
import com.example.minecraftserverstatus.model.Server
import kotlinx.coroutines.launch

class ServerViewModel : ViewModel() {

    private val serverRepository = ServerRepository()

    private val _servers = MutableLiveData<List<Server>>()
    val servers: LiveData<List<Server>>
        get() = _servers

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    init {
        _servers.value = emptyList()
        _isLoading.value = false
    }

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

    private fun addServerToList(server: Server) {
        val currentServers = _servers.value?.toMutableList() ?: mutableListOf()
        currentServers.add(server)
        _servers.postValue(currentServers)
    }
}
