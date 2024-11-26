package com.example.minecraftserverstatus.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.lyed.minecraftserverstatus.R
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val gifImageView = findViewById<GifImageView>(R.id.splashGifImageView)
        val gifDrawable = GifDrawable(resources, R.drawable.portal)
        gifImageView.setImageDrawable(gifDrawable)

        // Duración del splash screen
        val splashScreenDuration = 1000L // 3 segundos

        Handler(Looper.getMainLooper()).postDelayed({
            // Iniciar la actividad principal después de la duración del splash screen
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, splashScreenDuration)
    }
}
