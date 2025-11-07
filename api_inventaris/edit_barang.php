<?php
// Memasukkan file koneksi
include 'koneksi.php';

// Inisialisasi array respons
$response = [];

// Cek apakah metode request adalah POST
if ($_SERVER['REQUEST_METHOD'] == 'POST') {

    // Ambil data dari body request
    $id = isset($_POST['id']) ? $_POST['id'] : null; // ID barang
    $nama = isset($_POST['nama']) ? $_POST['nama'] : null;
    $jumlah = isset($_POST['jumlah']) ? $_POST['jumlah'] : null;
    $harga = isset($_POST['harga']) ? $_POST['harga'] : null;

    // --- VALIDASI ---
    if (empty($id) || empty($nama) || empty($jumlah) || empty($harga)) {
        $response = [
            "status" => "error",
            "message" => "Semua field (id, nama, jumlah, harga) tidak boleh kosong."
        ];
    } 
    else if (!is_numeric($id) || !is_numeric($jumlah) || !is_numeric($harga)) {
         $response = [
            "status" => "error",
            "message" => "ID, Jumlah, dan Harga harus berupa angka."
        ];
    }
    else {
        // --- Jika semua validasi lolos ---
        
        $stmt = $koneksi->prepare("UPDATE tb_barang SET nama = ?, jumlah = ?, harga = ? WHERE id = ?");
        $stmt->bind_param("siii", $nama, $jumlah, $harga, $id);

        if ($stmt->execute()) {
            if ($stmt->affected_rows > 0) {
                $response = [
                    "status" => "success",
                    "message" => "Data barang berhasil diupdate."
                ];
            } else {
                $response = [
                    "status" => "warning",
                    "message" => "Data barang tidak berubah atau ID tidak ditemukan."
                ];
            }
        } else {
            $response = [
                "status" => "error",
                "message" => "Gagal mengupdate barang: " . $stmt->error
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