// SoundPlayer.kt
package com.example.minecraftserverstatus.util

import android.content.Context
import android.media.MediaPlayer
import com.example.minecraftserverstatus.R

object SoundPlayer {

    private var mediaPlayer: MediaPlayer? = null

    fun init(context: Context) {
        mediaPlayer = MediaPlayer.create(context, R.raw.minecraft_click)
    }

    fun play() {
        mediaPlayer?.start()
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
