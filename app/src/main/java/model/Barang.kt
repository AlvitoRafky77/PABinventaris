// File: model/Barang.kt
package com.example.pab_inventaris.model // Sesuaikan package

import com.google.gson.annotations.SerializedName

data class Barang(
    // Pastikan nama variabel di sini (kiri)
    // SAMA PERSIS dengan nama kolom di API JSON Anda (kanan)

    @SerializedName("id")
    val id: Int,

    @SerializedName("nama")
    val nama: String,

    @SerializedName("jumlah")
    val jumlah: Int,

    @SerializedName("harga")
    val harga: Long
)