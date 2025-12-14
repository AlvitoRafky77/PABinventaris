package com.example.pab_inventaris

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.pab_inventaris.databinding.ActivityProfileBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = -1
    private var hasPassword = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        val sharedPreferences = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("USER_ID", -1)

        if (userId == -1) {
            finish()
            return
        }

        setSupportActionBar(binding.toolbarProfile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarProfile.setNavigationOnClickListener { onBackPressed() }

        val userName = sharedPreferences.getString("USER_NAME", "-")
        val userEmail = sharedPreferences.getString("EMAIL", "-")
        val jenisKelamin = sharedPreferences.getString("JENIS_KELAMIN", "-")
        val tanggalLahir = sharedPreferences.getString("TANGGAL_LAHIR", "-")
        val fotoUrl = sharedPreferences.getString("FOTO_URL", null)

        binding.tvProfileName.text = userName
        binding.tvProfileEmail.text = userEmail
        binding.tvProfileGender.text = jenisKelamin
        binding.tvProfileDob.text = tanggalLahir

        binding.ivProfilePicture.load(fotoUrl) {
            crossfade(true)
            placeholder(R.mipmap.ic_launcher_round)
            error(R.drawable.ic_profile_error)
        }

        checkUserPassword()

        binding.btnEditPassword.setOnClickListener {
            showPasswordDialog()
        }

        binding.btnProfileLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun checkUserPassword() {
        lifecycleScope.launch {
            val user = dbHelper.getUserById(userId)
            if (user != null) {
                val password = user["password"] as? String
                hasPassword = !password.isNullOrEmpty()
                
                if (hasPassword) {
                    binding.tietPasswordProfile.setText("********")
                } else {
                    binding.tietPasswordProfile.setText("")
                    binding.tietPasswordProfile.hint = "Belum ada password"
                }
            }
        }
    }

    private fun showPasswordDialog() {
        val title = if (hasPassword) "Ganti Password" else "Buat Password"
        val message = if (hasPassword) "Masukkan password baru Anda:" else "Masukkan password untuk akun ini:"

        val input = EditText(this)
        input.hint = "Password Baru"
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        
        val container = android.widget.FrameLayout(this)
        val params = android.widget.FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val margin = (24 * resources.displayMetrics.density).toInt()
        params.leftMargin = margin
        params.rightMargin = margin
        input.layoutParams = params
        container.addView(input)

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setView(container)
            .setPositiveButton("Simpan") { _, _ ->
                val newPass = input.text.toString()
                if (newPass.isNotEmpty()) {
                    updatePassword(newPass)
                } else {
                    Toast.makeText(this, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updatePassword(newPass: String) {
        lifecycleScope.launch {
            val result = dbHelper.updatePassword(userId, newPass)
            if (result > 0) {
                Toast.makeText(this@ProfileActivity, "Password berhasil disimpan", Toast.LENGTH_SHORT).show()
                checkUserPassword()
            } else {
                Toast.makeText(this@ProfileActivity, "Gagal menyimpan password", Toast.LENGTH_SHORT).show()
            }
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