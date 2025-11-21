package com.example.pab_inventaris

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pab_inventaris.databinding.ActivityLengkapiDataBinding
import kotlinx.coroutines.launch

class LengkapiDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLengkapiDataBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLengkapiDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        val googleId = intent.getStringExtra("GOOGLE_ID")
        val email = intent.getStringExtra("EMAIL")
        val namaDariGoogle = intent.getStringExtra("NAMA")
        val fotoUrl = intent.getStringExtra("FOTO_URL")

        if (googleId == null || email == null) {
            Toast.makeText(this, "Error: Data Sesi Google Tidak Ditemukan", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if (!namaDariGoogle.isNullOrEmpty()) {
            binding.tietNamaLengkapLengkapi.setText(namaDariGoogle)
        }

        binding.btnSimpanProfil.setOnClickListener {
            simpanDataProfil(googleId, email, fotoUrl)
        }
    }

    private fun simpanDataProfil(googleId: String, email: String, fotoUrl: String?) {
        val namaLengkap = binding.tietNamaLengkapLengkapi.text.toString().trim()
        val password = binding.tietPasswordLengkapi.text.toString().trim()

        val selectedJenisKelaminId = binding.rgJenisKelamin.checkedRadioButtonId
        if (namaLengkap.isEmpty() || selectedJenisKelaminId == -1) {
            Toast.makeText(this, "Nama Lengkap dan Jenis Kelamin wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val jenisKelamin = findViewById<RadioButton>(selectedJenisKelaminId).text.toString()

        val dp = binding.dpTanggalLahir
        val tanggalLahir = "${dp.dayOfMonth}/${dp.month + 1}/${dp.year}"

        lifecycleScope.launch {
            val newUserId = dbHelper.addGoogleUser(googleId, email, namaLengkap, jenisKelamin, tanggalLahir, password, fotoUrl)
            if (newUserId != -1L) {
                val user = dbHelper.findUserByGoogleId(googleId)
                if (user != null) {
                    val userId = user["id"] as Int
                    val userName = user["nama"] as String
                    val userFotoUrl = user["foto_url"] as? String
                    saveSessionAndNavigate(userId, userName, userFotoUrl)
                } else {
                     Toast.makeText(this@LengkapiDataActivity, "Gagal memulai sesi setelah registrasi", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@LengkapiDataActivity, "Gagal menyimpan profil. Email mungkin sudah terdaftar.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveSessionAndNavigate(userId: Int, userName: String, fotoUrl: String?) {
        val sharedPreferences = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("IS_LOGGED_IN", true)
        editor.putInt("USER_ID", userId)
        editor.putString("USER_NAME", userName)
        editor.putString("FOTO_URL", fotoUrl)
        editor.apply()

        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
