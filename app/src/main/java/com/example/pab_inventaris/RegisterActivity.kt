// File: com/example/pab_inventaris/RegisterActivity.kt

package com.example.pab_inventaris // <--- DIPERBAIKI

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.gson.JsonObject
import com.example.pab_inventaris.databinding.ActivityRegisterBinding // <--- DIPERBAIKI
import com.example.pab_inventaris.network.RetrofitClient // <--- DIPERBAIKI
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Event Listener
        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.tvToLogin.setOnClickListener {
            // Kembali ke LoginActivity
            finish()
        }
    }

    private fun registerUser() {
        val username = binding.etUsernameRegister.text.toString().trim()
        val password = binding.etPasswordRegister.text.toString().trim()

        // Validasi
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password minimal harus 6 karakter", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.instance.register(username, password)
            .enqueue(object : Callback<JsonObject> {

                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.isSuccessful) {
                        val json = response.body()

                        if (json != null && json.get("status").asString == "success") {
                            // Registrasi Berhasil
                            Toast.makeText(this@RegisterActivity, "Registrasi berhasil! Silakan login.", Toast.LENGTH_LONG).show()
                            finish()

                        } else {
                            // Registrasi Gagal
                            val message = json?.get("message")?.asString ?: "Registrasi Gagal"
                            Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this@RegisterActivity, "Gagal: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Toast.makeText(this@RegisterActivity, "Gagal terhubung ke server: ${t.message}", Toast.LENGTH_LONG).show()
                    Log.e("RegisterActivity", "onFailure: ", t)
                }
            })
    }
}