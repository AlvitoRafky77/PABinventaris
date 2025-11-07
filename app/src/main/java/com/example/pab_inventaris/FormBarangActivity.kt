// File: com/example/pab_inventaris/FormBarangActivity.kt
package com.example.pab_inventaris

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.example.pab_inventaris.databinding.ActivityFormBarangBinding
import com.example.pab_inventaris.network.RetrofitClient
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FormBarangActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormBarangBinding

    // Variabel untuk menyimpan data dari Intent
    private var isEditMode = false
    private var currentUserId = -1
    private var currentBarangId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Setup Toolbar
        setSupportActionBar(binding.toolbarForm)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Tampilkan tombol back

        // 2. Ambil data dari Intent
        isEditMode = intent.getBooleanExtra("IS_EDIT", false)

        // 3. Cek Mode (Tambah atau Edit)
        if (isEditMode) {
            // --- Mode EDIT ---
            supportActionBar?.title = "Edit Barang"

            // Ambil data barang yang dikirim dari DashboardActivity
            currentBarangId = intent.getIntExtra("BARANG_ID", -1)
            val nama = intent.getStringExtra("NAMA")
            val jumlah = intent.getIntExtra("JUMLAH", 0)
            val harga = intent.getLongExtra("HARGA", 0)

            // Tampilkan data ke EditText
            binding.etNama.setText(nama)
            binding.etJumlah.setText(jumlah.toString())
            binding.etHarga.setText(harga.toString())

        } else {
            // --- Mode TAMBAH ---
            supportActionBar?.title = "Tambah Barang Baru"

            // Ambil user_id yang dikirim dari DashboardActivity
            currentUserId = intent.getIntExtra("USER_ID", -1)
        }

        // 4. Set listener untuk tombol Simpan
        binding.btnSimpan.setOnClickListener {
            saveData()
        }
    }

    // Fungsi untuk tombol back di toolbar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish() // Tutup activity ini
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // Fungsi utama untuk menyimpan data (via API)
    private fun saveData() {
        // 1. Ambil data dari form
        val nama = binding.etNama.text.toString().trim()
        val jumlahString = binding.etJumlah.text.toString().trim()
        val hargaString = binding.etHarga.text.toString().trim()

        // 2. Validasi input tidak boleh kosong
        if (nama.isEmpty() || jumlahString.isEmpty() || hargaString.isEmpty()) {
            Toast.makeText(this, "Semua data tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. Konversi ke angka (dengan aman)
        val jumlah = jumlahString.toIntOrNull()
        val harga = hargaString.toLongOrNull()

        if (jumlah == null || harga == null) {
            Toast.makeText(this, "Jumlah dan Harga harus berupa angka valid", Toast.LENGTH_SHORT).show()
            return
        }

        // (Opsional: Tampilkan loading)
        binding.btnSimpan.isEnabled = false // Nonaktifkan tombol sementara

        // 4. Tentukan API mana yang harus dipanggil
        val apiCall: Call<JsonObject> = if (isEditMode) {
            // Panggil API Edit
            RetrofitClient.instance.updateBarang(currentBarangId, nama, jumlah, harga)
        } else {
            // Panggil API Tambah
            RetrofitClient.instance.addBarang(currentUserId, nama, jumlah, harga)
        }

        // 5. Eksekusi API
        apiCall.enqueue(object: Callback<JsonObject>{
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                binding.btnSimpan.isEnabled = true // Aktifkan tombol lagi

                if (response.isSuccessful && response.body()?.get("status")?.asString == "success") {
                    val message = if (isEditMode) "Data berhasil diupdate" else "Data berhasil disimpan"
                    Toast.makeText(this@FormBarangActivity, message, Toast.LENGTH_SHORT).show()

                    // --- PENTING ---
                    // Kirim sinyal "OK" kembali ke DashboardActivity
                    setResult(Activity.RESULT_OK)
                    finish() // Tutup form

                } else {
                    val message = response.body()?.get("message")?.asString ?: "Gagal menyimpan data"
                    Toast.makeText(this@FormBarangActivity, message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                binding.btnSimpan.isEnabled = true // Aktifkan tombol lagi
                Toast.makeText(this@FormBarangActivity, "Koneksi Gagal: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e("FormBarangActivity", "onFailure: ", t)
            }
        })
    }
}