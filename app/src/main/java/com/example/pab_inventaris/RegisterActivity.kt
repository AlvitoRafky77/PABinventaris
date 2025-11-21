package com.example.pab_inventaris

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pab_inventaris.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        binding.btnRegister.setOnClickListener {
            val namaLengkap = binding.tietNamaLengkapReg.text.toString().trim()
            val email = binding.tietEmailReg.text.toString().trim()
            val password = binding.tietPasswordReg.text.toString().trim()

            val selectedJenisKelaminId = binding.rgJenisKelaminReg.checkedRadioButtonId

            if (namaLengkap.isEmpty() || email.isEmpty() || password.isEmpty() || selectedJenisKelaminId == -1) {
                Toast.makeText(this, "Semua kolom wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val jenisKelamin = findViewById<RadioButton>(selectedJenisKelaminId).text.toString()

            val dp = binding.dpTanggalLahirReg
            val tanggalLahir = "${dp.dayOfMonth}/${dp.month + 1}/${dp.year}"

            lifecycleScope.launch {
                val newUserId = dbHelper.addUser(email, password, namaLengkap, jenisKelamin, tanggalLahir)
                if (newUserId != -1L) {
                    Toast.makeText(this@RegisterActivity, "Pendaftaran berhasil! Silakan login.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@RegisterActivity, "Pendaftaran gagal. Email mungkin sudah digunakan.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
