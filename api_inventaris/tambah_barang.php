<?php
// Memasukkan file koneksi
include 'koneksi.php';

// Inisialisasi array respons
$response = [];

// Cek apakah metode request adalah POST
if ($_SERVER['REQUEST_METHOD'] == 'POST') {

    // Ambil data dari body request
    // user_id akan dikirim dari Android setelah login berhasil
    $user_id = isset($_POST['user_id']) ? $_POST['user_id'] : null;
    $nama = isset($_POST['nama']) ? $_POST['nama'] : null;
    $jumlah = isset($_POST['jumlah']) ? $_POST['jumlah'] : null;
    $harga = isset($_POST['harga']) ? $_POST['harga'] : null;

    // --- VALIDASI (Sesuai UTS: input tidak boleh kosong) --- 
    if (empty($user_id) || empty($nama) || empty($jumlah) || empty($harga)) {
        $response = [
            "status" => "error",
            "message" => "Semua field (user_id, nama, jumlah, harga) tidak boleh kosong."
        ];
    }
    // Validasi tambahan: jumlah dan harga harus angka
    else if (!is_numeric($jumlah) || !is_numeric($harga)) {
         $response = [
            "status" => "error",
            "message" => "Jumlah dan Harga harus berupa angka."
        ];
    }
    else {
        // --- Jika semua validasi lolos ---
        
        // Siapkan query INSERT menggunakan prepared statements
        $stmt = $koneksi->prepare("INSERT INTO tb_barang (user_id, nama, jumlah, harga) VALUES (?, ?, ?, ?)");
        
        // Bind parameter ke query
        // Tipe data: "isii" -> integer (user_id), string (nama), integer (jumlah), integer (harga)
        $stmt->bind_param("isii", $user_id, $nama, $jumlah, $harga);

        // Eksekusi query
        if ($stmt->execute()) {
            $response = [
                "status" => "success",
                "message" => "Barang berhasil ditambahkan."
            ];
        } else {
            $response = [
                "status" => "error",
                "message" => "Gagal menambahkan barang: " . $stmt->error
            ];
        }
        $stmt->close();
    }
} else {
    // Jika metode request bukan POST
    $response = [
        "status" => "error",
        "message" => "Metode request tidak valid."
    ];
}

// Mengirimkan respons dalam format JSON
echo json_encode($response);

// Menutup koneksi database
$koneksi->close();
?>