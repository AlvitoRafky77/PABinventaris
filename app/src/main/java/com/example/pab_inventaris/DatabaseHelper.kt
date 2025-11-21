package com.example.pab_inventaris

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.pab_inventaris.model.Barang
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 5
        private const val DATABASE_NAME = "Inventaris.db"

        // Tabel Users
        private const val TABLE_USERS = "users"
        private const val KEY_USER_ID = "id"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
        private const val KEY_GOOGLE_ID = "google_id"
        private const val KEY_NAMA_LENGKAP = "nama_lengkap"
        private const val KEY_JENIS_KELAMIN = "jenis_kelamin"
        private const val KEY_TANGGAL_LAHIR = "tanggal_lahir"
        private const val KEY_FOTO_URL = "foto_url"

        // Tabel Barang
        private const val TABLE_BARANG = "barang"
        private const val KEY_BARANG_ID = "id"
        private const val KEY_BARANG_NAMA = "nama_barang"
        private const val KEY_BARANG_JUMLAH = "jumlah_barang"
        private const val KEY_BARANG_HARGA = "harga_barang"
        private const val KEY_BARANG_USER_ID = "user_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_USERS_TABLE = ("CREATE TABLE " + TABLE_USERS + "("
                + KEY_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_EMAIL + " TEXT UNIQUE,"
                + KEY_GOOGLE_ID + " TEXT UNIQUE,"
                + KEY_NAMA_LENGKAP + " TEXT,"
                + KEY_JENIS_KELAMIN + " TEXT,"
                + KEY_TANGGAL_LAHIR + " TEXT,"
                + KEY_FOTO_URL + " TEXT,"
                + KEY_PASSWORD + " TEXT)")
        db.execSQL(CREATE_USERS_TABLE)

        val CREATE_BARANG_TABLE = ("CREATE TABLE " + TABLE_BARANG + "("
                + KEY_BARANG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_BARANG_USER_ID + " INTEGER,"
                + KEY_BARANG_NAMA + " TEXT,"
                + KEY_BARANG_JUMLAH + " INTEGER,"
                + KEY_BARANG_HARGA + " INTEGER,"
                + "FOREIGN KEY(" + KEY_BARANG_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_USER_ID + "))")
        db.execSQL(CREATE_BARANG_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BARANG")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // --- FUNGSI UNTUK USERS ---
    suspend fun addUser(email: String, pass: String, nama: String, jk: String, tglLahir: String): Long = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(KEY_EMAIL, email)
            put(KEY_PASSWORD, pass)
            put(KEY_NAMA_LENGKAP, nama)
            put(KEY_JENIS_KELAMIN, jk)
            put(KEY_TANGGAL_LAHIR, tglLahir)
        }
        writableDatabase.insert(TABLE_USERS, null, values)
    }

    suspend fun addGoogleUser(googleId: String, email: String, nama: String, jk: String, tglLahir: String, pass: String?, fotoUrl: String?): Long = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(KEY_GOOGLE_ID, googleId)
            put(KEY_EMAIL, email)
            put(KEY_NAMA_LENGKAP, nama)
            put(KEY_JENIS_KELAMIN, jk)
            put(KEY_TANGGAL_LAHIR, tglLahir)
            put(KEY_FOTO_URL, fotoUrl)
            if (!pass.isNullOrEmpty()) {
                put(KEY_PASSWORD, pass)
            }
        }
        writableDatabase.insert(TABLE_USERS, null, values)
    }

    suspend fun checkUser(email: String, pass: String): Map<String, Any?>? = withContext(Dispatchers.IO) {
        val cursor: Cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_USERS WHERE $KEY_EMAIL = ? AND $KEY_PASSWORD = ?", arrayOf(email, pass))
        cursor.use {
            if (it.moveToFirst()) {
                mapOf(
                    "id" to it.getInt(it.getColumnIndexOrThrow(KEY_USER_ID)),
                    "nama" to it.getString(it.getColumnIndexOrThrow(KEY_NAMA_LENGKAP)),
                    "jenis_kelamin" to it.getString(it.getColumnIndexOrThrow(KEY_JENIS_KELAMIN)),
                    "tanggal_lahir" to it.getString(it.getColumnIndexOrThrow(KEY_TANGGAL_LAHIR)),
                    "foto_url" to it.getString(it.getColumnIndexOrThrow(KEY_FOTO_URL))
                )
            } else {
                null
            }
        }
    }

    suspend fun findUserByGoogleId(googleId: String): Map<String, Any?>? = withContext(Dispatchers.IO) {
        val cursor: Cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_USERS WHERE $KEY_GOOGLE_ID = ?", arrayOf(googleId))
        cursor.use {
            if (it.moveToFirst()) {
                mapOf(
                    "id" to it.getInt(it.getColumnIndexOrThrow(KEY_USER_ID)),
                    "nama" to it.getString(it.getColumnIndexOrThrow(KEY_NAMA_LENGKAP)),
                    "jenis_kelamin" to it.getString(it.getColumnIndexOrThrow(KEY_JENIS_KELAMIN)),
                    "tanggal_lahir" to it.getString(it.getColumnIndexOrThrow(KEY_TANGGAL_LAHIR)),
                    "foto_url" to it.getString(it.getColumnIndexOrThrow(KEY_FOTO_URL))
                )
            } else {
                null
            }
        }
    }

    // --- FUNGSI UNTUK BARANG ---
    suspend fun addBarang(barang: Barang, userId: Int): Long = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(KEY_BARANG_USER_ID, userId)
            put(KEY_BARANG_NAMA, barang.nama)
            put(KEY_BARANG_JUMLAH, barang.jumlah)
            put(KEY_BARANG_HARGA, barang.harga)
        }
        writableDatabase.insert(TABLE_BARANG, null, values)
    }

    suspend fun getBarangForUser(userId: Int): List<Barang> = withContext(Dispatchers.IO) {
        val barangList = ArrayList<Barang>()
        val selectQuery = "SELECT * FROM $TABLE_BARANG WHERE $KEY_BARANG_USER_ID = ?"
        val cursor = readableDatabase.rawQuery(selectQuery, arrayOf(userId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val barang = Barang(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_BARANG_ID)),
                    nama = cursor.getString(cursor.getColumnIndexOrThrow(KEY_BARANG_NAMA)),
                    jumlah = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_BARANG_JUMLAH)),
                    harga = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_BARANG_HARGA))
                )
                barangList.add(barang)
            } while (cursor.moveToNext())
        }
        cursor.close()
        barangList
    }

    suspend fun updateBarang(barang: Barang): Int = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(KEY_BARANG_NAMA, barang.nama)
            put(KEY_BARANG_JUMLAH, barang.jumlah)
            put(KEY_BARANG_HARGA, barang.harga)
        }
        writableDatabase.update(TABLE_BARANG, values, "$KEY_BARANG_ID = ?", arrayOf(barang.id.toString()))
    }

    suspend fun deleteBarang(id: Int): Int = withContext(Dispatchers.IO) {
        writableDatabase.delete(TABLE_BARANG, "$KEY_BARANG_ID = ?", arrayOf(id.toString()))
    }
}