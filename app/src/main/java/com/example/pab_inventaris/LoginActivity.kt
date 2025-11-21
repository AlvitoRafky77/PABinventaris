package com.example.pab_inventaris

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pab_inventaris.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var dbHelper: DatabaseHelper
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dbHelper = DatabaseHelper(this)

        val sharedPreferences = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("IS_LOGGED_IN", false)) {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.signInButton.setOnClickListener {
            signInWithGoogle()
        }

        binding.btnLogin.setOnClickListener {
            loginBiasa()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginBiasa() {
        val email = binding.tietEmail.text.toString().trim()
        val password = binding.tietPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val user = dbHelper.checkUser(email, password)
            if (user != null) {
                saveSessionAndNavigate(user)
            } else {
                Toast.makeText(this@LoginActivity, "Email atau Password Salah", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account != null && account.id != null) {
                lifecycleScope.launch {
                    val user = dbHelper.findUserByGoogleId(account.id!!)
                    if (user != null) {
                        saveSessionAndNavigate(user)
                    } else {
                        val intent = Intent(this@LoginActivity, LengkapiDataActivity::class.java).apply {
                            putExtra("GOOGLE_ID", account.id)
                            putExtra("EMAIL", account.email)
                            putExtra("NAMA", account.displayName)
                            putExtra("FOTO_URL", account.photoUrl?.toString())
                        }
                        startActivity(intent)
                    }
                }
            } else {
                Toast.makeText(this, "Gagal mendapatkan akun Google", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            mGoogleSignInClient.signOut()
            Toast.makeText(this, "Login Google Gagal: " + e.statusCode, Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveSessionAndNavigate(user: Map<String, Any?>) {
        val sharedPreferences = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("IS_LOGGED_IN", true)
        editor.putInt("USER_ID", user["id"] as Int)
        editor.putString("USER_NAME", user["nama"] as? String)
        editor.putString("EMAIL", user["email"] as? String)
        editor.putString("FOTO_URL", user["foto_url"] as? String)
        editor.putString("JENIS_KELAMIN", user["jenis_kelamin"] as? String)
        editor.putString("TANGGAL_LAHIR", user["tanggal_lahir"] as? String)
        editor.apply()

        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}