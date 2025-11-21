package com.example.pab_inventaris.model

// Anotasi @SerializedName dan import com.google.gson sudah tidak diperlukan
// karena kita beralih ke database SQLite lokal.

data class Barang(
    val id: Int,
    val nama: String,
    val jumlah: Int,
    val harga: Long
)
