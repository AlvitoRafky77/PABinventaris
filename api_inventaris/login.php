<?php
// Memasukkan file koneksi
include 'koneksi.php';

// Inisialisasi array respons
$response = [];

// Cek apakah metode request adalah POST
if ($_SERVER['REQUEST_METHOD'] == 'POST') {

    // Ambil data dari body request
    $username = isset($_POST['username']) ? $_POST['username'] : null;
    $password = isset($_POST['password']) ? $_POST['password'] : null;

    // --- VALIDASI MINIMAL ---

    // 1. Validasi: Input tidak boleh kosong
    if (empty($username) || empty($password)) {
        $response = [
            "status" => "error",
            "message" => "Username dan Password tidak boleh kosong."
        ];
    } else {
        // --- Proses Login ---

        // 1. Cari username di database
        $stmt = $koneksi->prepare("SELECT id, password FROM tb_user WHERE username = ?");
        $stmt->bind_param("s", $username);
        $stmt->execute();
        $result = $stmt->get_result();

        if ($result->num_rows == 1) {
            // Jika username ditemukan, ambil datanya
            $user = $result->fetch_assoc();
            $hashed_password = $user['password'];
            $user_id = $user['id'];

            // 2. Verifikasi password yang di-hash
            if (password_verify($password, $hashed_password)) {
                // Jika password cocok
                $response = [
                    "status" => "success",
                    "message" => "Login berhasil.",
                    "data" => [
                        "user_id" => $user_id,
                        "username" => $username
                    ]
                ];
            } else {
                // Jika password tidak cocok
                $response = [
                    "status" => "error",
                    "message" => "Password salah."
                ];
            }
        } else {
            // Jika username tidak ditemukan
            $response = [
                "status" => "error",
                "message" => "Username tidak ditemukan."
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