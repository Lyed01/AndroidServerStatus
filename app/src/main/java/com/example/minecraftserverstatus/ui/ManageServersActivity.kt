package com.example.minecraftserverstatus.ui

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.lyed.minecraftserverstatus.R
import com.lyed.minecraftserverstatus.databinding.ActivityManageServersBinding
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView

class ManageServersActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityManageServersBinding
    private val viewModel: ServerViewModel by viewModels()
    private lateinit var serverAdapter: ServerAdapter
    private lateinit var loadingImageView: GifImageView
    private lateinit var loadingGifDrawable: GifDrawable
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var filterSpinner: Spinner

    private var activeFilters: MutableList<Int> = mutableListOf() // Lista para almacenar las posiciones de los filtros activos
    private var toast: Toast? = null // Variable para almacenar el Toast activo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageServersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        // Initialize Spinner
        filterSpinner = binding.filterSpinner
        ArrayAdapter.createFromResource(
            this,
            R.array.filter_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            filterSpinner.adapter = adapter
        }

        // Handle Spinner item selection
        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateActiveFilters(position)
                showToastMessage() // Llama a showToastMessage() sin argumentos

                when (position) {
                    0 -> viewModel.filterAllServers()
                    1 -> viewModel.filterFavoriteServers()
                    2 -> viewModel.filterServersByPlayerCount(false)
                    3 -> viewModel.filterServersByPlayerCount(true)
                }

                // Si se selecciona un filtro de jugadores, verifica si también está activo el filtro de favoritos
                if ((position == 2 || position == 3) && activeFilters.contains(1)) {
                    viewModel.filterFavoriteServers() // Aplica filtro de favoritos también
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada
            }
        }

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshServers()
        }

        // Initialize GifImageView and load loading GIF
        loadingImageView = binding.loadingImageView
        loadingGifDrawable = GifDrawable(resources, R.drawable.squid)
        loadingGifDrawable.setSpeed(2.0f) // Aumentar velocidad del GIF
        loadingImageView.setImageDrawable(loadingGifDrawable)

        // Configure RecyclerView with adapter and ViewModel
        serverAdapter = ServerAdapter(emptyList(), viewModel)
        binding.serversRecyclerView.apply {
            adapter = serverAdapter
            layoutManager = LinearLayoutManager(this@ManageServersActivity)
        }

        // Configure search field
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()
                viewModel.filterServersByHostname(searchText)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Configure add server button
        binding.addServerButton.setOnClickListener {
            mediaPlayer = MediaPlayer.create(this@ManageServersActivity, R.raw.minecraft_click)
            mediaPlayer.start()

            val intent = Intent(this, AddServerActivity::class.java)
            startActivityForResult(intent, ADD_SERVER_REQUEST_CODE)
        }

        // Observe changes in server list from ViewModel
        viewModel.servers.observe(this, Observer { servers ->
            serverAdapter.updateData(servers)
            swipeRefreshLayout.isRefreshing = false
        })

        // Observe loading state
        viewModel.isLoading.observe(this, Observer { isLoading ->
            if (isLoading) {
                loadingImageView.visibility = View.VISIBLE
                loadingGifDrawable.start()
            } else {
                loadingImageView.visibility = View.GONE
                loadingGifDrawable.stop()
            }
        })

        // Check user authentication on activity start
        checkUser()
    }

    private fun checkUser() {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // Método para mostrar el mensaje Toast con los filtros activos
    // Método para mostrar el mensaje Toast con los filtros activos
    // Método para mostrar el mensaje Toast con los filtros activos
    private fun showToastMessage() {
        toast?.cancel() // Cancelar el Toast anterior si existe

        val activeFiltersNames = mutableListOf<String>()

        // Verificar y agregar al mensaje los filtros activos
        if (activeFilters.contains(1)) {
            activeFiltersNames.add("Favorites")
        }
        if (activeFilters.contains(2)) {
            activeFiltersNames.add("Player count ascending")
        }
        if (activeFilters.contains(3)) {
            activeFiltersNames.add("Player count descending")
        }

        // Construir el mensaje con todos los filtros activos
        val message = when {
            activeFiltersNames.isEmpty() -> "Cleaned Filters"
            else -> "Active Filters: ${activeFiltersNames.joinToString(", ")}"
        }

        toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        toast?.show()
    }

    // Método para actualizar las posiciones activas en el Spinner
    // Método para actualizar las posiciones activas en el Spinner
    private fun updateActiveFilters(position: Int) {
        // Limpiar todos los filtros activos si se selecciona "all"
        if (position == 0) {
            activeFilters.clear()
        } else {
            // Si se selecciona asc o desc, asegúrate de que solo uno esté activo a la vez
            if (position == 2 || position == 3) {
                if (activeFilters.contains(2) || activeFilters.contains(3)) {
                    activeFilters.remove(2)
                    activeFilters.remove(3)
                }
            }

            // Si la posición no está en la lista, agrégala
            if (!activeFilters.contains(position)) {
                activeFilters.add(position)
            } else {
                // Si ya está en la lista, quítala para desactivarla
                activeFilters.remove(position)
            }
        }

        // Mostrar el mensaje actualizado de filtros activos
        showToastMessage()
    }



    // Handle AddServerActivity result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_SERVER_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.let {
                val ip = it.getStringExtra("ip") ?: return@let

                loadingImageView.visibility = View.VISIBLE
                loadingGifDrawable.start()

                viewModel.addServer(ip) {
                    loadingImageView.visibility = View.GONE
                    loadingGifDrawable.stop()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }

    companion object {
        private const val ADD_SERVER_REQUEST_CODE = 1
    }
}
