<?php
// Atur header ke JSON
header('Content-Type: application/json');

// Cek apakah metodenya POST
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    
    // Langsung cetak semua isi dari array $_POST
    echo json_encode($_POST);

} else {
    // Kirim pesan jika bukan POST
    echo json_encode(["status" => "error", "message" => "Metode harus POST"]);
}
?>