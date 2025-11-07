<?php
// Memasukkan file koneksi
include 'koneksi.php';

// Inisialisasi array respons
$response = [];

// Cek apakah metode request adalah POST
if ($_SERVER['REQUEST_METHOD'] == 'POST') {

    // Ambil data dari body request
    $id = isset($_POST['id']) ? $_POST['id'] : null; // ID barang yang akan dihapus

    // --- VALIDASI ---
    if (empty($id)) {
        $response = [
            "status" => "error",
            "message" => "ID barang tidak boleh kosong."
        ];
    } 
    else if (!is_numeric($id)) {
         $response = [
            "status" => "error",
            "message" => "ID barang harus berupa angka."
        ];
    }
    else {
        // --- Jika semua validasi lolos ---
        
        // Siapkan query DELETE menggunakan prepared statements
        $stmt = $koneksi->prepare("DELETE FROM tb_barang WHERE id = ?");
        
        // Bind parameter ke query
        // Tipe data: "i" -> integer (id)
        $stmt->bind_param("i", $id);

        // Eksekusi query
        if ($stmt->execute()) {
            // Cek apakah ada baris yang benar-benar terhapus
            if ($stmt->affected_rows > 0) {
                $response = [
                    "status" => "success",
                    "message" => "Data barang berhasil dihapus."
                ];
            } else {
                $response = [
                    "status" => "warning",
                    "message" => "Gagal menghapus: ID barang tidak ditemukan."
                ];
            }
        } else {
            $response = [
                "status" => "error",
                "message" => "Gagal menghapus barang: " . $stmt->error
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