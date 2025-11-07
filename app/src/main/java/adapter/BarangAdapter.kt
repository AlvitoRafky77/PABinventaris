// File: adapter/BarangAdapter.kt
package com.example.pab_inventaris.adapter // Sesuaikan package

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pab_inventaris.databinding.ItemBarangBinding // Import View Binding
import com.example.pab_inventaris.model.Barang // Import Model
import java.text.NumberFormat
import java.util.Locale

// Kita menggunakan ListAdapter untuk efisiensi
class BarangAdapter : ListAdapter<Barang, BarangAdapter.BarangViewHolder>(BarangDiffCallback()) {

    // --- Menangani Klik Tombol ---
    // Kita buat "jembatan" (interface) agar DashboardActivity bisa tahu item mana yg diklik
    private var onEditClickListener: ((Barang) -> Unit)? = null
    private var onDeleteClickListener: ((Barang) -> Unit)? = null

    fun setOnEditClickListener(listener: (Barang) -> Unit) {
        onEditClickListener = listener
    }

    fun setOnDeleteClickListener(listener: (Barang) -> Unit) {
        onDeleteClickListener = listener
    }
    // --- Selesai Menangani Klik ---


    // 1. Membuat ViewHolder (tampilan) baru
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarangViewHolder {
        // Inflate (membuat) layout item_barang.xml
        val binding = ItemBarangBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BarangViewHolder(binding)
    }

    // 2. Menghubungkan data (Barang) dengan ViewHolder (tampilan)
    override fun onBindViewHolder(holder: BarangViewHolder, position: Int) {
        // Ambil data barang di posisi saat ini
        val barang = getItem(position)
        // Kirim data ke ViewHolder untuk ditampilkan
        holder.bind(barang)
    }


    // --- ViewHolder ---
    // Kelas ini bertugas "memegang" komponen UI dari item_barang.xml
    inner class BarangViewHolder(private val binding: ItemBarangBinding) : RecyclerView.ViewHolder(binding.root) {

        // Fungsi untuk "mengisi" data ke tampilan
        fun bind(barang: Barang) {
            binding.tvItemNama.text = barang.nama

            // Format angka harga dan jumlah agar lebih rapi
            val localeID = Locale("in", "ID")
            val numberFormat = NumberFormat.getNumberInstance(localeID)

            binding.tvItemHarga.text = numberFormat.format(barang.harga)
            binding.tvItemJumlah.text = numberFormat.format(barang.jumlah)

            // --- Atur Klik Listener untuk Tombol di item ini ---
            binding.btnItemEdit.setOnClickListener {
                onEditClickListener?.invoke(barang)
            }

            binding.btnItemDelete.setOnClickListener {
                onDeleteClickListener?.invoke(barang)
            }
        }
    }


    // --- DiffUtil ---
    // Kelas ini bertugas mengecek perubahan daftar secara efisien
    // (Agar tidak me-reload semua data saat ada 1 data berubah)
    class BarangDiffCallback : DiffUtil.ItemCallback<Barang>() {
        override fun areItemsTheSame(oldItem: Barang, newItem: Barang): Boolean {
            // Cek apakah item-nya sama (berdasarkan ID)
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Barang, newItem: Barang): Boolean {
            // Cek apakah kontennya sama (semua field)
            return oldItem == newItem
        }
    }
}