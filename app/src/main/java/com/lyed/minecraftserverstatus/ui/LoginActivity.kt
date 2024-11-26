package com.lyed.minecraftserverstatus.ui

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lyed.minecraftserverstatus.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


class LoginActivity : AppCompatActivity() {
    private lateinit var loginButton: ImageButton
    private lateinit var googleSignInButton: ImageButton
    private lateinit var rickyButton: ImageButton
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar FirebaseApp
        FirebaseApp.initializeApp(this)

        // Obtener instancia de FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Configurar GoogleSignInClient
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        googleSignInButton = findViewById(R.id.googleSignInButton)
        googleSignInButton.setOnClickListener {
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, 100)
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.ricky)
        rickyButton = findViewById(R.id.rickybutton)

        rickyButton.setOnClickListener {
            playClickSound() // Reproducir sonido al hacer clic en el botón
        }


        // Configurar GoogleSignInButton

    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release() // Liberar recursos del MediaPlayer
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100) {
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = accountTask.getResult(ApiException::class.java)
                firebaseAuthWithGoogleAccount(account)
            } catch (e: Exception) {
                Log.e("DEMO-API", "onActivityResult: ${e.message}")
            }
        }
    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val firebaseUser = firebaseAuth.currentUser
                val uid = firebaseUser!!.uid
                val email = firebaseUser.email

                if (authResult.additionalUserInfo?.isNewUser == true) {
                    Toast.makeText(this@LoginActivity, "Cuenta creada...", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@LoginActivity, "Cuenta existente...", Toast.LENGTH_LONG).show()
                }

                startActivity(Intent(this@LoginActivity, ManageServersActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@LoginActivity, "Error en el login: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Método para reproducir el sonido
    private fun playClickSound() {
        mediaPlayer.start()
    }
}
