<?php
// Mengatur header agar responsenya adalah JSON
header('Content-Type: application/json');

// Informasi koneksi database
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "pab_inventaris";

// Membuat koneksi
$koneksi = new mysqli($servername, $username, $password, $dbname);

// Cek koneksi
if ($koneksi->connect_error) {
    // Jika koneksi gagal
    $response = [
        "status" => "error",
        "message" => "Koneksi Database Gagal: " . $koneksi->connect_error
    ];
    echo json_encode($response);
    die();
}
?>