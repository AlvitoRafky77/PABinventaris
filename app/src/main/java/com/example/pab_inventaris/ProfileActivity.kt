package com.example.pab_inventaris

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.pab_inventaris.databinding.ActivityProfileBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("USER_NAME", "-")
        val userEmail = sharedPreferences.getString("EMAIL", "-")
        val jenisKelamin = sharedPreferences.getString("JENIS_KELAMIN", "-")
        val tanggalLahir = sharedPreferences.getString("TANGGAL_LAHIR", "-")
        val fotoUrl = sharedPreferences.getString("FOTO_URL", null)

        binding.tvProfileName.text = userName
        binding.tvProfileEmail.text = userEmail
        binding.tvProfileGender.text = "Jenis Kelamin: $jenisKelamin"
        binding.tvProfileDob.text = "Tanggal Lahir: $tanggalLahir"

        binding.ivProfilePicture.load(fotoUrl) {
            crossfade(true)
            placeholder(R.mipmap.ic_launcher_round)
            error(R.drawable.ic_profile_error)
        }

        binding.btnProfileLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin logout?")
            .setPositiveButton("Logout") { _, _ ->
                logout()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun logout() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut().addOnCompleteListener(this) { 
             Toast.makeText(this, "Anda telah logout", Toast.LENGTH_SHORT).show()
        }

        val sharedPreferences = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}