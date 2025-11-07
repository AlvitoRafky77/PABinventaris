package com.example.pab_inventaris.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient
{
    // GANTI DENGAN URL API ANDA
    // Gunakan "http://10.0.2.2/" jika menjalankan di Emulator Android
    // Gunakan IP Asli PC Anda (misal: "http://192.168.1.5/") jika pakai HP asli
    private const val BASE_URL = "http://10.0.2.2/api_inventaris/"

    // lazy artinya 'instance' baru akan dibuat saat pertama kali diakses
    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Pakai Gson
            .build()

        retrofit.create(ApiService::class.java)
    }
}