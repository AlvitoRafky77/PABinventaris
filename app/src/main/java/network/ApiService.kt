package com.example.pab_inventaris.network

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*

interface ApiService
{
    // Endpoint untuk register.php
    @FormUrlEncoded
    @POST("register.php")
    fun register(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<JsonObject> // Kita pakai JsonObject untuk respons simpel (status/message)

    // Endpoint untuk login.php
    @FormUrlEncoded
    @POST("login.php")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<JsonObject> // Akan mengembalikan data user_id

    // Endpoint untuk tampil_barang.php
    @FormUrlEncoded
    @POST("tampil_barang.php")
    fun getBarang(
        @Field("user_id") userId: Int
    ): Call<JsonObject> // Akan mengembalikan array data barang

    // Endpoint untuk tambah_barang.php
    @FormUrlEncoded
    @POST("tambah_barang.php")
    fun addBarang(
        @Field("user_id") userId: Int,
        @Field("nama") nama: String,
        @Field("jumlah") jumlah: Int,
        @Field("harga") harga: Long
    ): Call<JsonObject>

    // Endpoint untuk edit_barang.php (pakai POST, sesuai file PHP Anda)
    @FormUrlEncoded
    @POST("edit_barang.php")
    fun updateBarang(
        @Field("id") idBarang: Int,
        @Field("nama") nama: String,
        @Field("jumlah") jumlah: Int,
        @Field("harga") harga: Long
    ): Call<JsonObject>

    // Endpoint untuk hapus_barang.php
    @FormUrlEncoded
    @POST("hapus_barang.php")
    fun deleteBarang(
        @Field("id") idBarang: Int
    ): Call<JsonObject>

}