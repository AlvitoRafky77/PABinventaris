package com.example.pab_inventaris

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.pab_inventaris.adapter.BarangAdapter
import com.example.pab_inventaris.databinding.ActivityDashboardBinding
import com.example.pab_inventaris.model.Barang
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapter: BarangAdapter
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private var userId: Int = -1

    private val formResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loadDataBarang()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        sharedPreferences = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("USER_ID", -1)

        if (userId == -1) {
            logout()
            return
        }

        setSupportActionBar(binding.toolbarDashboard)
        setupRecyclerView()
        setupListeners()
        loadDataBarang()
    }

    private fun setupRecyclerView() {
        adapter = BarangAdapter()
        // Menggunakan StaggeredGridLayoutManager untuk tampilan yang lebih modern (grid masonry)
        binding.rvBarang.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.rvBarang.adapter = adapter

        adapter.setOnEditClickListener { barang ->
            val intent = Intent(this, FormBarangActivity::class.java).apply {
                putExtra("IS_EDIT", true)
                putExtra("BARANG_ID", barang.id)
                putExtra("NAMA", barang.nama)
                putExtra("JUMLAH", barang.jumlah)
                putExtra("HARGA", barang.harga)
            }
            formResultLauncher.launch(intent)
        }

        adapter.setOnDeleteClickListener { barang ->
            showDeleteConfirmation(barang)
        }
    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, FormBarangActivity::class.java).apply {
                putExtra("IS_EDIT", false)
                putExtra("USER_ID", userId)
            }
            formResultLauncher.launch(intent)
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadDataBarang()
        }
    }

    private fun loadDataBarang() {
        binding.swipeRefreshLayout.isRefreshing = true
        lifecycleScope.launch {
            val barangList = dbHelper.getBarangForUser(userId)
            adapter.submitList(barangList)
            binding.swipeRefreshLayout.isRefreshing = false

            if (barangList.isEmpty()) {
                binding.llEmptyState.visibility = View.VISIBLE
                binding.rvBarang.visibility = View.GONE
            } else {
                binding.llEmptyState.visibility = View.GONE
                binding.rvBarang.visibility = View.VISIBLE
            }
        }
    }

    private fun showDeleteConfirmation(barang: Barang) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Barang")
            .setMessage("Apakah Anda yakin ingin menghapus '${barang.nama}'?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteBarang(barang.id)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteBarang(idBarang: Int) {
        lifecycleScope.launch {
            val result = dbHelper.deleteBarang(idBarang)
            if (result > 0) {
                Toast.makeText(this@DashboardActivity, "Barang berhasil dihapus", Toast.LENGTH_SHORT).show()
                loadDataBarang()
            } else {
                Toast.makeText(this@DashboardActivity, "Gagal menghapus barang", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu_profile, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            R.id.menu_logout -> {
                showLogoutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
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
        mGoogleSignInClient.signOut().addOnCompleteListener(this) { 
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