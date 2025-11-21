package com.example.pab_inventaris

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pab_inventaris.databinding.ActivityFormBarangBinding
import com.example.pab_inventaris.model.Barang
import kotlinx.coroutines.launch

class FormBarangActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormBarangBinding
    private lateinit var dbHelper: DatabaseHelper
    private var isEdit = false
    private var barangId = -1
    private var userId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        isEdit = intent.getBooleanExtra("IS_EDIT", false)
        userId = intent.getIntExtra("USER_ID", -1)

        if (isEdit) {
            // Mode Edit
            binding.tvFormTitle.text = "Edit Barang"
            binding.btnSimpan.text = "Update"

            barangId = intent.getIntExtra("BARANG_ID", -1)
            val nama = intent.getStringExtra("NAMA")
            val jumlah = intent.getIntExtra("JUMLAH", 0)
            val harga = intent.getLongExtra("HARGA", 0L)

            binding.tietNamaBarang.setText(nama)
            binding.tietJumlahBarang.setText(jumlah.toString())
            binding.tietHargaBarang.setText(harga.toString())

        } else {
            // Mode Tambah
            binding.tvFormTitle.text = "Tambah Barang Baru"

            if (userId == -1) {
                Toast.makeText(this, "Error: User ID tidak ditemukan", Toast.LENGTH_LONG).show()
                finish()
                return
            }
        }

        binding.btnSimpan.setOnClickListener {
            simpanData()
        }
    }

    private fun simpanData() {
        val nama = binding.tietNamaBarang.text.toString().trim()
        val jumlahString = binding.tietJumlahBarang.text.toString().trim()
        val hargaString = binding.tietHargaBarang.text.toString().trim()

        if (nama.isEmpty() || jumlahString.isEmpty() || hargaString.isEmpty()) {
            Toast.makeText(this, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val jumlah = jumlahString.toIntOrNull()
        val harga = hargaString.toLongOrNull()

        if (jumlah == null || harga == null) {
            Toast.makeText(this, "Jumlah dan Harga harus berupa angka", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            if (isEdit) {
                val barang = Barang(id = barangId, nama = nama, jumlah = jumlah, harga = harga)
                val result = dbHelper.updateBarang(barang)
                if (result > 0) {
                    Toast.makeText(this@FormBarangActivity, "Data berhasil diupdate", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@FormBarangActivity, "Gagal mengupdate data", Toast.LENGTH_SHORT).show()
                }
            } else {
                val barang = Barang(id = 0, nama = nama, jumlah = jumlah, harga = harga)
                val result = dbHelper.addBarang(barang, userId)
                if (result != -1L) {
                    Toast.makeText(this@FormBarangActivity, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@FormBarangActivity, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
