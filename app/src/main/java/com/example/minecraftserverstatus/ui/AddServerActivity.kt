package com.example.minecraftserverstatus.ui

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.minecraftserverstatus.R

class AddServerActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.CustomDialogTheme)
        setContentView(R.layout.activity_add_server)
        val width = (resources.displayMetrics.widthPixels * 0.8).toInt()

        // Inicializar el MediaPlayer
        mediaPlayer = MediaPlayer.create(this@AddServerActivity, R.raw.minecraft_click)

        // Configurar el botón para agregar el servidor
        val addServerButton: ImageButton = findViewById(R.id.addServerButton)
        addServerButton.setOnClickListener {
            // Reproducir sonido
            playClickSound()

            val ipEditText = findViewById<EditText>(R.id.ipEditText)
            val ip = ipEditText.text.toString().trim()

            if (ip.isNotEmpty()) {
                val resultIntent = Intent().apply {
                    putExtra("ip", ip)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                // Mostrar un mensaje de error si falta la IP
                ipEditText.error = "IP Address cannot be empty"
            }
        }

        // Ajustar el tamaño del diálogo
        window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun playClickSound() {
        mediaPlayer.seekTo(0) // Reiniciar el sonido si ya está en curso
        mediaPlayer.start()
    }

    override fun onDestroy() {
        mediaPlayer.release()
        super.onDestroy()
    }
}
