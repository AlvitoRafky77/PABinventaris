<?php
// Memasukkan file koneksi
include 'koneksi.php';

// Inisialisasi array respons
$response = [];

// Cek apakah metode request adalah POST
if ($_SERVER['REQUEST_METHOD'] == 'POST') {

    // Ambil user_id dari body request
    $user_id = isset($_POST['user_id']) ? $_POST['user_id'] : null;

    // Validasi: user_id tidak boleh kosong
    if (empty($user_id)) {
        $response = [
            "status" => "error",
            "message" => "user_id tidak boleh kosong."
        ];
    } else {
        // --- Jika user_id ada ---
        
        // Siapkan query SELECT
        // Kita mengambil semua data barang HANYA untuk user_id yang spesifik
        $stmt = $koneksi->prepare("SELECT id, nama, jumlah, harga FROM tb_barang WHERE user_id = ? ORDER BY id DESC");
        $stmt->bind_param("i", $user_id);
        $stmt->execute();
        $result = $stmt->get_result();

        // Siapkan array untuk menampung data barang
        $barang_list = [];

        // Looping hasil query dan masukkan ke array
        while ($row = $result->fetch_assoc()) {
            $barang_list[] = $row;
        }

        // Cek apakah ada data yang ditemukan
        if (count($barang_list) > 0) {
            // Jika data ditemukan
            $response = [
                "status" => "success",
                "message" => "Data barang berhasil diambil.",
                "data" => $barang_list
            ];
        } else {
            // Jika tidak ada data untuk user tersebut
            $response = [
                "status" => "success", // Tetap sukses, tapi data kosong
                "message" => "Tidak ada data barang untuk user ini.",
                "data" => [] // Kirim array kosong
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