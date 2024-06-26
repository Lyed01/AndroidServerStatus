package com.example.minecraftserverstatus.ui

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.minecraftserverstatus.R
import com.example.minecraftserverstatus.databinding.ActivityManageServersBinding
import com.google.firebase.auth.FirebaseAuth
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageServersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        // Inicializar SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            // Aquí manejas la lógica de actualización al arrastrar hacia abajo
            viewModel.refreshServers()
        }

        // Inicializar GifImageView y cargar el GIF de carga
        loadingImageView = findViewById(R.id.loadingImageView)
        loadingGifDrawable = GifDrawable(resources, R.drawable.squid)
        loadingGifDrawable.setSpeed(2.0f) // Aumentar la velocidad del GIF
        loadingImageView.setImageDrawable(loadingGifDrawable)

        // Configurar el RecyclerView con el adaptador y el ViewModel
        serverAdapter = ServerAdapter(emptyList(), viewModel)
        binding.serversRecyclerView.apply {
            adapter = serverAdapter
            layoutManager = LinearLayoutManager(this@ManageServersActivity)
        }

        // Configurar el botón para agregar servidor
        binding.addServerButton.setOnClickListener {
            mediaPlayer = MediaPlayer.create(this@ManageServersActivity, R.raw.minecraft_click)
            mediaPlayer.start()

            val intent = Intent(this, AddServerActivity::class.java)
            startActivityForResult(intent, ADD_SERVER_REQUEST_CODE)
        }

        // Observar cambios en la lista de servidores en el ViewModel y actualizar el adaptador
        viewModel.servers.observe(this, Observer { servers ->
            serverAdapter.updateData(servers)
            // Detener el indicador de actualización
            swipeRefreshLayout.isRefreshing = false
        })

        // Observar estado de carga
        viewModel.isLoading.observe(this, Observer { isLoading ->
            if (isLoading) {
                // Mostrar GIF de carga
                loadingImageView.visibility = View.VISIBLE
                loadingGifDrawable.start()
            } else {
                // Ocultar GIF de carga
                loadingImageView.visibility = View.GONE
                loadingGifDrawable.stop()
            }
        })

        // Verificar la autenticación del usuario al iniciar la actividad
        checkUser()
    }

    private fun checkUser() {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            // Si el usuario no está autenticado, redirigir a com.example.minecraftserverstatus.ui.LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // Manejar el resultado de la actividad AddServerActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_SERVER_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.let {
                val ip = it.getStringExtra("ip") ?: return@let
                val name = it.getStringExtra("name") ?: "Server"

                // Mostrar GIF de carga
                loadingImageView.visibility = View.VISIBLE
                loadingGifDrawable.start()

                // Agregar el servidor al ViewModel
                viewModel.addServer(ip) {
                    // Ocultar GIF de carga al completar
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
