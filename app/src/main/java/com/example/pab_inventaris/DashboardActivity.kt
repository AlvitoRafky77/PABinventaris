// File: com/example/pab_inventaris/DashboardActivity.kt
package com.example.pab_inventaris

// Import-import penting
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pab_inventaris.adapter.BarangAdapter
import com.example.pab_inventaris.databinding.ActivityDashboardBinding
import com.example.pab_inventaris.model.Barang
import com.example.pab_inventaris.network.RetrofitClient
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardActivity : AppCompatActivity() {

    // --- Deklarasi Variabel ---
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapter: BarangAdapter
    private var userId: Int = -1 // Variabel untuk menyimpan User ID

    // --- ActivityResultLauncher ---
    // Ini adalah cara modern untuk 'startActivityForResult'
    // Kita gunakan ini agar list bisa auto-refresh setelah menambah/mengedit data
    private val formResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Cek apakah FormBarangActivity mengirim balasan "OK"
        if (result.resultCode == Activity.RESULT_OK) {
            // Jika ya, muat ulang data barang
            loadDataBarang()
        }
    }

    // --- onCreate (Fungsi Utama) ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Setup SharedPreferences dan ambil User ID
        sharedPreferences = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("USER_ID", -1)

        // Jika karena alasan tertentu user_id tidak ada, paksa logout
        if (userId == -1) {
            logout()
            return
        }

        // 2. Setup Toolbar
        setSupportActionBar(binding.toolbarDashboard)

        // 3. Setup RecyclerView
        setupRecyclerView()

        // 4. Setup Tombol & Refresh Listener
        setupListeners()

        // 5. Muat data barang untuk pertama kali
        loadDataBarang()
    }

    // --- Fungsi untuk Setup RecyclerView ---
    private fun setupRecyclerView() {
        adapter = BarangAdapter()
        binding.rvBarang.layoutManager = LinearLayoutManager(this)
        binding.rvBarang.adapter = adapter

        // --- Ini PENTING: Menangani klik Edit & Hapus DARI ADAPTER ---

        // a. Tombol Edit diklik
        adapter.setOnEditClickListener { barang ->
            // Buka FormBarangActivity (Mode Edit)
            val intent = Intent(this, FormBarangActivity::class.java).apply {
                // Kirim data barang yang akan diedit ke FormActivity
                putExtra("IS_EDIT", true)
                putExtra("BARANG_ID", barang.id)
                putExtra("NAMA", barang.nama)
                putExtra("JUMLAH", barang.jumlah)
                putExtra("HARGA", barang.harga)
            }
            // Jalankan Activity dan tunggu hasilnya (via formResultLauncher)
            formResultLauncher.launch(intent)
        }

        // b. Tombol Hapus diklik
        adapter.setOnDeleteClickListener { barang ->
            // Tampilkan dialog konfirmasi
            showDeleteConfirmation(barang)
        }
    }

    // --- Fungsi untuk Setup Listener Lainnya ---
    private fun setupListeners() {
        // Tombol (+) Tambah diklik
        binding.fabAdd.setOnClickListener {
            // Buka FormBarangActivity (Mode Tambah)
            val intent = Intent(this, FormBarangActivity::class.java).apply {
                putExtra("IS_EDIT", false)
                putExtra("USER_ID", userId) // Kirim User ID untuk data baru
            }
            // Jalankan Activity dan tunggu hasilnya
            formResultLauncher.launch(intent)
        }

        // Swipe-to-Refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadDataBarang() // Muat ulang data
        }
    }

    // --- Fungsi Inti: Mengambil Data dari API ---
    private fun loadDataBarang() {
        // Tampilkan loading spinner
        binding.swipeRefreshLayout.isRefreshing = true

        // Panggil API tampil_barang.php
        RetrofitClient.instance.getBarang(userId)
            .enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    // Sembunyikan loading spinner
                    binding.swipeRefreshLayout.isRefreshing = false

                    if (response.isSuccessful) {
                        val json = response.body()
                        if (json != null && json.get("status").asString == "success") {
                            // --- Sukses: Ambil data array ---
                            val dataArray = json.getAsJsonArray("data")

                            // Konversi JsonArray ke List<Barang> menggunakan Gson
                            val listType = object : TypeToken<List<Barang>>() {}.type
                            val barangList: List<Barang> = Gson().fromJson(dataArray, listType)

                            // Masukkan data ke adapter
                            adapter.submitList(barangList)

                            // Cek apakah daftar kosong
                            if (barangList.isEmpty()) {
                                binding.tvEmptyList.visibility = View.VISIBLE
                                binding.rvBarang.visibility = View.GONE
                            } else {
                                binding.tvEmptyList.visibility = View.GONE
                                binding.rvBarang.visibility = View.VISIBLE
                            }

                        } else {
                            // Gagal dari API (misal: user_id tidak ada)
                            Toast.makeText(this@DashboardActivity, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
                            binding.tvEmptyList.visibility = View.VISIBLE
                        }
                    } else {
                        // Gagal HTTP (Error 404, 500, dll)
                        Toast.makeText(this@DashboardActivity, "Error server: ${response.message()}", Toast.LENGTH_SHORT).show()
                        binding.tvEmptyList.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    // Gagal koneksi (Tidak ada internet, server mati, BASE_URL salah)
                    binding.swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(this@DashboardActivity, "Koneksi Gagal: ${t.message}", Toast.LENGTH_LONG).show()
                    Log.e("DashboardActivity", "onFailure: ", t)
                    binding.tvEmptyList.visibility = View.VISIBLE
                }
            })
    }

    // --- Fungsi untuk Menampilkan Dialog Konfirmasi Hapus ---
    private fun showDeleteConfirmation(barang: Barang) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Barang")
            .setMessage("Apakah Anda yakin ingin menghapus '${barang.nama}'?")
            .setPositiveButton("Hapus") { _, _ ->
                // Panggil API hapus_barang.php
                callDeleteApi(barang.id)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // --- Fungsi untuk Memanggil API Hapus ---
    private fun callDeleteApi(idBarang: Int) {
        RetrofitClient.instance.deleteBarang(idBarang)
            .enqueue(object: Callback<JsonObject>{
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.isSuccessful && response.body()?.get("status")?.asString == "success") {
                        Toast.makeText(this@DashboardActivity, "Barang berhasil dihapus", Toast.LENGTH_SHORT).show()
                        loadDataBarang() // Refresh daftar setelah hapus
                    } else {
                        val message = response.body()?.get("message")?.asString ?: "Gagal menghapus"
                        Toast.makeText(this@DashboardActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Toast.makeText(this@DashboardActivity, "Koneksi Gagal: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    // --- Fungsi-fungsi untuk Menu Logout ---
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate (tampilkan) menu_dashboard.xml di toolbar
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Cek item menu apa yang diklik
        return when (item.itemId) {
            R.id.menu_logout -> {
                // Tampilkan dialog konfirmasi logout
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
                logout() // Panggil fungsi logout
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun logout() {
        // Hapus data sesi dari SharedPreferences
        val editor = sharedPreferences.edit()
        editor.clear() // Hapus semua data (IS_LOGGED_IN, USER_ID, dll)
        editor.apply()

        // Kembali ke LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        // Hapus history, agar tidak bisa "back" ke dashboard
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Tutup DashboardActivity
    }
}