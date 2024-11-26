package com.example.minecraftserverstatus.ui

import android.app.Dialog
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.minecraftserverstatus.model.Server
import com.lyed.minecraftserverstatus.R
import com.lyed.minecraftserverstatus.databinding.DialogServerDetailBinding

class ServerDetailDialog(context: Context, private val server: Server) : Dialog(context) {
    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var binding: DialogServerDetailBinding


    init {
        // Crear el diálogo sin bordes
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogServerDetailBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        // Mostrar los detalles del servidor en el TextView
        val serverDetails = StringBuilder().apply {
            append("IP: ${server.ip}\n")
            append("Jugadores: ${server.players?.online}/${server.players?.max}\n")
            append("Motd:\n${server.motd?.clean?.joinToString("\n") ?: "N/A"}\n")
            append("Versión: ${server.version ?: "N/A"}\n")
            append("Online: ${server.online}\n")
            append("Hostname: ${server.hostname ?: "N/A"}\n")
            append("Gamemode: ${server.gamemode ?: "N/A"}\n")
            append("Mapa: ${server.map?.clean ?: "N/A"}\n")
            append("Software: ${server.software ?: "N/A"}\n")
            append("Protocolo: ${server.protocol?.name ?: "N/A"}\n")
            append("Plugins:\n${server.plugins?.joinToString("\n") { "${it.name} - ${it.version}" } ?: "N/A"}\n")
            append("Mods:\n${server.mods?.joinToString("\n") { "${it.name} - ${it.version}" } ?: "N/A"}\n")
            append("Información:\n${server.info?.clean?.joinToString("\n") ?: "N/A"}\n")
            append("isFav:\n${server.isFavorite.toString() ?: "N/A"}")



        }

        binding.serverDetailsTextview.text = serverDetails.toString().trim()

        // Decodificar y mostrar el icono del servidor si está disponible
        server.icon?.let { iconBase64 ->
            try {
                val imageBytes = Base64.decode(iconBase64.split(",")[1], Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                binding.serverIconImageview.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                binding.serverIconImageview.setImageResource(R.drawable.default_server_icon)
            }
        } ?: run {
            binding.serverIconImageview.setImageResource(R.drawable.default_server_icon)
        }

        // Configurar botón Cerrar
        binding.closeButton.setOnClickListener {
            mediaPlayer = MediaPlayer.create(context, R.raw.minecraft_click)
            mediaPlayer.start()
            dismiss()
        }

    }}

