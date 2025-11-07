<?php
// Memasukkan file koneksi
include 'koneksi.php';

// Inisialisasi array respons
$response = [];

// Cek apakah metode request adalah POST
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    
    // Ambil data dari body request (Android akan mengirim sebagai POST)
    $username = isset($_POST['username']) ? $_POST['username'] : null;
    $password = isset($_POST['password']) ? $_POST['password'] : null;

    // --- VALIDASI MINIMAL (Sesuai UTS) --- 

    // 1. Validasi: Input tidak boleh kosong
    if (empty($username) || empty($password)) {
        $response = [
            "status" => "error",
            "message" => "Username dan Password tidak boleh kosong."
        ];
    } 
    // 2. Validasi: Password >= 6 karakter 
    else if (strlen($password) < 6) {
        $response = [
            "status" => "error",
            "message" => "Password minimal harus 6 karakter."
        ];
    } 
    // 3. Validasi: Username unik 
    else {
        // Cek dulu apakah username sudah ada
        $stmt_check = $koneksi->prepare("SELECT id FROM tb_user WHERE username = ?");
        $stmt_check->bind_param("s", $username);
        $stmt_check->execute();
        $stmt_check->store_result();

        if ($stmt_check->num_rows > 0) {
            // Jika username sudah ada
            $response = [
                "status" => "error",
                "message" => "Username sudah digunakan, silakan pilih yang lain."
            ];
        } else {
            // --- Jika semua validasi lolos ---

            // Enkripsi password (WAJIB untuk keamanan)
            $hashed_password = password_hash($password, PASSWORD_BCRYPT);

            // Masukkan data ke database menggunakan prepared statements (lebih aman)
            $stmt_insert = $koneksi->prepare("INSERT INTO tb_user (username, password) VALUES (?, ?)");
            $stmt_insert->bind_param("ss", $username, $hashed_password);

            if ($stmt_insert->execute()) {
                $response = [
                    "status" => "success",
                    "message" => "Registrasi berhasil."
                ];
            } else {
                $response = [
                    "status" => "error",
                    "message" => "Registrasi gagal: " . $stmt_insert->error
                ];
            }
            $stmt_insert->close();
        }
        $stmt_check->close();
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