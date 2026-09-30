# Nebeng

Nebeng adalah aplikasi Android komuter carpooling dan ridesharing berbasis komunitas. Dirancang untuk membantu pekerja dan komuter di kawasan aglomerasi (seperti Jabodetabek) berbagi tumpangan perjalanan harian secara gratis, aman, dan efisien, dengan opsi pemberian tip langsung melalui QRIS pengemudi tanpa potongan biaya platform.

---

## Fitur Utama

- **Pencarian & Penawaran Tebengan**:
  - Penumpang dapat mencari tebengan berdasarkan lokasi jemput, tujuan, waktu berangkat, dan radius kedekatan menggunakan query spasial PostGIS.
  - Pengemudi dapat mempublikasikan rute harian, menentukan titik temu, bagasi, serta preferensi berkendara (gender, rokok, musik).
- **Denah Kursi Interaktif**:
  - Visualisasi tata letak tempat duduk untuk mobil (kapasitas 4–7 kursi) dan motor.
  - Penumpang dapat memilih nomor kursi yang tersedia saat proses pemesanan.
- **Live Tracking & Rute**:
  - Pelacakan posisi GPS pengemudi secara real-time selama perjalanan aktif menggunakan Supabase Realtime channel dan MapLibre SDK (OpenStreetMap vector tiles).
  - Estimasi rute perjalanan menggunakan Open Source Routing Machine (OSRM).
- **Verifikasi & Keamanan**:
  - **PIN Penjemputan**: Kode verifikasi 6-digit yang wajib dimasukkan pengemudi sebelum memulai perjalanan guna mencegah salah penjemputan.
  - **Fitur Darurat (SOS)**: Tombol darurat cepat yang langsung membuka draft SMS darurat berisi koordinat GPS terkini ke kontak darurat yang terdaftar.
  - **Verifikasi Identitas (KYC)**: Unggah dokumen identitas (e-KTP) untuk mendapatkan badge verifikasi profil.
- **Chat Realtime**:
  - Fitur obrolan langsung antara pengemudi dan penumpang per pesanan perjalanan dengan indikator status pengiriman dan quick reply chips.
- **Tip Sukarela via QRIS**:
  - Penumpang dapat memberikan apresiasi sukarela langsung ke QRIS pribadi pengemudi tanpa perantara dompet digital internal aplikasi.
- **Tebengan Rutin**:
  - Penjadwalan tebengan berulang untuk hari kerja (Senin–Jumat) guna mempermudah rutinitas komuter tetap.
- **Rating & Ulasan**:
  - Sistem penilaian dua arah setelah perjalanan selesai untuk menjaga standar kenyamanan komunitas.

---

## Arsitektur & Teknologi

Aplikasi dibangun mengikuti prinsip **Clean Architecture** dan pola **MVVM (Model-View-ViewModel)** dengan pemisahan dependensi yang modular:

```
app/src/main/java/com/disinidev/nebeng/
├── core/                  # Utilities, navigation, design system token & themes
├── data/                  # Implementasi repository, data source (Supabase, DataStore, GPS)
│   ├── mapper/            # Entity <-> DTO mappers
│   ├── model/             # Request & response data transfer objects
│   └── repository/        # Repositories implementation
├── di/                    # Modul Dagger Hilt untuk Dependency Injection
├── domain/                # Business logic murni: Entities, Repositories interface, UseCases
│   ├── model/             # Core business models
│   └── repository/        # Interface contracts
└── presentation/          # UI Layer (Jetpack Compose screens, ViewModels, UI states)
    ├── activity/          # Riwayat & monitoring perjalanan aktif
    ├── auth/              # Login, onboarding, & verifikasi nomor HP
    ├── chat/              # Chat realtime pengemudi-penumpang
    ├── checkout/          # Konfirmasi booking & pemilihan denah kursi
    ├── driver/            # Publikasi dan manajemen rute pengemudi
    ├── home/              # Beranda & rekomendasi tebengan sekitar
    ├── notification/      # Pusat notifikasi pengguna
    ├── profile/           # Profil, verifikasi identitas (KYC), & kelola QRIS
    ├── routine/           # Manajemen tebengan rutin komuter
    ├── search/            # Filter pencarian rute & tujuan
    ├── settings/          # Pengaturan aplikasi & kebijakan
    ├── tip/               # Halaman tip QRIS pengemudi
    ├── tracking/          # Peta navigasi & pelacakan lokasi real-time
    ├── tripdone/          # Ulasan & penilaian perjalanan
    └── vehicle/           # Manajemen data kendaraan pengemudi
```

### Stack Teknologi

- **Bahasa**: Kotlin (100%)
- **UI Toolkit**: Jetpack Compose, Material 3, Navigation Compose
- **Dependency Injection**: Dagger Hilt
- **Asinkron & Reactive**: Kotlin Coroutines, StateFlow, SharedFlow
- **Backend & Database**:
  - Supabase (PostgreSQL 15 + PostGIS untuk geospatial query)
  - Supabase Realtime (Websocket channel untuk koordinat GPS & chat)
  - Supabase Storage (Penyimpanan foto profil, kendaraan, e-KTP, dan QRIS)
- **Autentikasi**: Firebase Phone Authentication (SMS OTP) terintegrasi dengan akun Supabase
- **Peta & Lokasi**: MapLibre SDK for Android (OpenStreetMap / Carto Positron vector tiles), OSRM (Open Source Routing Machine), OSM Nominatim (Pencarian Lokasi & Geocoding), Android Fused Location Provider
- **Image Loading**: Coil 3
- **Serialisasi**: Kotlinx Serialization JSON

---

## Persyaratan Lingkungan

- **JDK**: Versi 17
- **Android Studio**: Android Studio Koala / Ladybug (2024.1+) atau versi lebih baru
- **Android SDK**:
  - `minSdk`: 26 (Android 8.0 Oreo)
  - `targetSdk` / `compileSdk`: 37
- Akun layanan eksternal:
  - **Supabase Project** dengan ekstensi PostGIS aktif
  - **Firebase Project** dengan Phone Authentication diaktifkan (SMS OTP)
  - _(Peta tidak memerlukan API key berbayar karena menggunakan stack OpenStreetMap / MapLibre)_

---

## Panduan Menjalankan Proyek

### 1. Clone Repositori

```bash
git clone https://github.com/rukys/NebengApp.git
cd nebeng
```

### 2. Konfigurasi Variabel Lingkungan (`local.properties`)

Salin file `local.properties.example` menjadi `local.properties` di root direktori proyek:

```bash
cp local.properties.example local.properties
```

Isi variabel konfigurasi sesuai dengan kredensial proyek lokal Anda:

```properties
## Lokasi Android SDK di komputer Anda
sdk.dir=/path/ke/Android/Sdk

## Konfigurasi Supabase
SUPABASE_URL=https://<project-ref>.supabase.co
SUPABASE_ANON_KEY=<your-supabase-anon-key>
```

> **Catatan**: File `local.properties` sudah masuk dalam `.gitignore` dan tidak boleh di-commit ke version control publik.

### 3. Konfigurasi Firebase

1. Masuk ke Firebase Console dan tambahkan Android App dengan package name:
   - Development: `com.disinidev.nebeng.dev`
   - Production: `com.disinidev.nebeng`
2. Unduh file `google-services.json`.
3. Letakkan file tersebut pada direktori:
   ```
   app/google-services.json
   ```
4. Pastikan SHA-1 fingerprint dari debug keystore lokal Anda telah didaftarkan pada konfigurasi Firebase App agar Phone Auth OTP dapat berfungsi.

### 4. Setup Database Supabase

Jalankan script SQL migrasi yang tersedia pada folder `supabase/migrations/` secara berurutan melalui SQL Editor di Supabase Dashboard:

1. `000_full_schema.sql` — Tabel utama (`profiles`, `vehicles`, `rides`, `bookings`, `emergency_contacts`), ekstensi PostGIS, trigger otomatisasi, dan RLS.
2. `001_update_free_community.sql` — Penyesuaian skema 100% gratis komunitas dan kolom QRIS pengemudi.
3. `002_fix_rls_firebase.sql` — Penyesuaian aturan Row Level Security untuk autentikasi Firebase.
4. `003_search_nearby_rides_rpc.sql` — RPC function `search_nearby_rides` untuk pencarian radius geospasial.
5. `004_seed_test_rides.sql` — Data dummy rute komuter Jabodetabek (opsional untuk testing).
6. `005_make_phone_nullable.sql` — Fleksibilitas skema nomor kontak.
7. `006_push_notifications_trigger.sql` — Trigger database untuk pengiriman push notification.
8. `007_chat_messages.sql` — Skema pesan chat per pemesanan tebengan.
9. `008_routine_commutes_and_cancellation.sql` — Dukungan jadwal komuter rutin dan pencatatan alasan pembatalan.

#### Konfigurasi Storage Bucket (Supabase)

Buat bucket berikut di menu Storage Supabase Dashboard:

| Nama Bucket       | Status Akses        | Kegunaan                             |
| ----------------- | ------------------- | ------------------------------------ |
| `profile-avatars` | Public              | Foto profil pengguna                 |
| `vehicle-images`  | Public              | Foto kendaraan pengemudi             |
| `qris-codes`      | Public              | Gambar QRIS pembayaran tip pengemudi |
| `ktp-documents`   | Private (Auth only) | Dokumen verifikasi identitas e-KTP   |

---

## Build & Instalasi

Proyek ini memiliki 2 Product Flavor:

- **`dev`**: Namespace `com.disinidev.nebeng.dev`, menggunakan label aplikasi `Nebeng Dev`.
- **`production`**: Namespace `com.disinidev.nebeng`.

### Melalui Terminal

Jalankan Gradle Wrapper sesuai flavor yang diinginkan:

```bash
# Kompilasi APK development debug
./gradlew assembleDevDebug

# Install langsung ke perangkat atau emulator yang terhubung via ADB
./gradlew installDevDebug
```

Untuk pengguna sistem operasi Windows:

```powershell
.\gradlew.bat assembleDevDebug
.\gradlew.bat installDevDebug
```

### Menjalankan Unit Test

```bash
./gradlew testDevDebugUnitTest
```

---

## Keamanan Data & Kebijakan

- **Row Level Security (RLS)**: Seluruh tabel transaksi data pengguna dilindungi oleh kebijakan RLS di level PostgreSQL. Pengguna hanya dapat memodifikasi data dan rute miliknya sendiri.
- **Kerahasiaan Kredensial**: Repositori ini tidak menyimpan API key, secret token, ataupun database URL asli. Seluruh konfigurasi diinjeksi saat build-time melalui `local.properties` dan file konfigurasi Firebase yang ter-ignore secara lokal.
- **Data Privasi**: Dokumen identitas KYC disimpan dalam storage bucket privat yang hanya dapat diakses melalui token autentikasi terverifikasi.

---

## Lisensi

Proyek ini dikembangkan untuk keperluan komunitas komuter. Seluruh kontribusi dan pengembangan modul baru dipersilakan melalui mekanisme Pull Request.
