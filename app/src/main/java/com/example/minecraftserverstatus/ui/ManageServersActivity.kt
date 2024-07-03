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
    private lateinit var filterSpinner: Spinner

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
                when (position) {
                    0 -> viewModel.filterAllServers()
                    1 -> viewModel.filterFavoriteServers()
                    // Agrega más casos según tus opciones de Spinner
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
