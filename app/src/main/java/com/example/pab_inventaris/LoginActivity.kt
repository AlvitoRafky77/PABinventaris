// File: com/example/pab_inventaris/LoginActivity.kt

package com.example.pab_inventaris // <--- DIPERBAIKI

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.gson.JsonObject
import com.example.pab_inventaris.databinding.ActivityLoginBinding // <--- DIPERBAIKI
import com.example.pab_inventaris.network.RetrofitClient // <--- DIPERBAIKI
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)

        // Cek Sesi Login
        if (sharedPreferences.getBoolean("IS_LOGGED_IN", false)) {
            goToDashboard()
            return
        }

        // Event Listener
        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        binding.tvToRegister.setOnClickListener {
            // Pindah ke RegisterActivity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.instance.login(username, password)
            .enqueue(object : Callback<JsonObject> {

                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.isSuccessful) {
                        val json = response.body()

                        if (json != null && json.get("status").asString == "success") {
                            // Login Berhasil
                            val data = json.getAsJsonObject("data")
                            val userId = data.get("user_id").asInt
                            val loggedInUsername = data.get("username").asString

                            // Simpan sesi
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("IS_LOGGED_IN", true)
                            editor.putInt("USER_ID", userId)
                            editor.putString("USERNAME", loggedInUsername)
                            editor.apply()

                            Toast.makeText(this@LoginActivity, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                            goToDashboard()

                        } else {
                            // Login Gagal
                            val message = json?.get("message")?.asString ?: "Login Gagal"
                            Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Gagal: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Gagal terhubung ke server: ${t.message}", Toast.LENGTH_LONG).show()
                    Log.e("LoginActivity", "onFailure: ", t)
                }
            })
    }

    private fun goToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}