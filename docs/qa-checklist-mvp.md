# Checklist QA Manual — Alur Peminjaman & Pengembalian (MVP)

Dipakai buat verifikasi manual di device/emulator + Firebase dev project, dua alur sesuai
diagram `ENUVENTORY WORKFLOW`. Unit test (`app/src/test`) sudah nge-cover logic state-machine
di ViewModel; checklist ini buat nge-cover yang gak bisa dites via unit test: interaksi UI asli,
realtime update Firestore antar 2 device/akun berbeda, dan step yang masih mock (ditandai ⚠️).

Butuh 2 akun buat testing penuh: 1 akun `user` biasa, 1 akun `admin` (role `admin` di
Firestore collection `users/{uid}`). Testing di 2 device/emulator sekaligus jauh lebih
representatif daripada gantian di 1 device, karena realtime listener-nya baru kelihatan gunanya
kalau ada 2 sesi aktif bersamaan.

## Alur Peminjaman

- [ ] **Login user** — masuk pakai akun `user`, redirect ke Home tanpa demo/mock login (sudah
  dihapus).
- [ ] **Browse asset** — Home nampilin daftar asset, status (Available/Unavailable/Maintenance)
  match Firestore.
- [ ] **Buka detail asset** — tap salah satu asset, detail nampil (ID, stock, deskripsi, tombol "
  Pinjam Asset").
- [ ] **Checkout/ajukan pinjam** — tap "Pinjam Asset" → dialog muncul (label "Kirimkan pesan" + "
  Estimasi kembali") →
  isi estimasi tanggal kembali → tap "Submit".
    - [ ] Dialog nutup, halaman detail langsung berubah ke state "Menunggu Persetujuan" + tombol "
      Batalkan"
      (harusnya instan, tanpa perlu keluar-masuk halaman — ini yang diperbaiki di commit-commit
      terakhir).
    - [ ] Cek di Firestore console: record baru muncul di collection `borrows` dengan
      `status: Pending`.
- [ ] **Admin dapat notifikasi** ⚠️ **(gak ada push notification asli — lihat `workflow-gaps.md` #1)
  **
    - [ ] Yang bisa dites: buka app admin, buka tab **Approval** — request yang baru masuk nongol di
      list
      (realtime, gak perlu refresh manual) selama halaman Approval kebuka.
    - [ ] Coba tutup app admin sepenuhnya lalu ajukan pinjam dari sisi user — pastikan memang *
      *tidak ada**
      notifikasi apapun yang masuk ke admin (mengonfirmasi gap, bukan nyari bug).
- [ ] **Admin ACC/Tolak** — admin tap request di list Approval → detail request nampil (Tanggal
  pinjam,
  Estimasi Kembali, tombol "Approve" / "Tolak").
    - [ ] Tap **"Approve"** → balik ke list Approval, request hilang dari list Pending.
    - [ ] Di sisi **user** (device/akun user), tanpa refresh manual, state di halaman detail asset
      berubah
      jadi "Sedang Dipinjam".
    - [ ] Ulangi dengan tap **"Tolak"** di request lain → status jadi Rejected, dan sisi user harus
      balik
      bisa mengajukan pinjam lagi (state kembali ke Normal, bukan macet di "Menunggu Persetujuan").
      Sudah ke-cover unit test (`DetailAssetUserViewModelTest.\`finished record (rejected)...\``),
      checklist ini cuma buat konfirmasi visual di UI asli.
    - [ ] Buka **Detail Riwayat** punya request yang barusan ditolak → timeline harus nampilin
      "Diajukan" → "Ditolak" (bukan "Menunggu Persetujuan" lagi — ini bug yang sudah diperbaiki,
      lihat `workflow-gaps.md` #1).
- [ ] **Scan QR di produk** ⚠️ **(halaman ini murni mock — lihat gap #2)**
    - [ ] Buka `ScanQRPage` (via tombol "Scan QR" di Detail Riwayat, hanya muncul kalau ada jalan
      untuk
      mencapai state `MenungguPengambilan` — **saat ini state itu tidak pernah tercapai**, jadi
      tombolnya
      kemungkinan besar tidak akan pernah terlihat di flow normal. Verifikasi ini dulu.
    - [ ] Kalau berhasil dibuka: tap kotak scan → dialog konfirmasi "Ya" muncul → tap "Ya" → cuma
      `popBackStack()`, **tidak ada perubahan status apapun di Firestore**. Konfirmasi ini juga cuma
      untuk mendokumentasikan gap, bukan expect ini "lolos test".
- [ ] **Stock/concurrency** — set stock asset = 1, ajukan pinjam dari 2 akun user berbeda untuk
  asset yang
  sama sebelum admin approve salah satunya. Cek apakah ada validasi stock saat approve (kemungkinan
  besar **tidak ada** — dua-duanya bisa ke-approve dan status Borrowed sekaligus meski stock cuma
  1).
  Catat sebagai temuan jika demikian.

## Alur Pengembalian

- [ ] **Buka History** — user buka tab History, record yang sudah Borrowed muncul dengan status
  timeline
  yang benar (harus di step "Batas Kembali").
- [ ] **Detail Riwayat → Kembalikan** — buka detail record → tombol "Kembalikan" muncul → tap →
  masuk ke `PengembalianPage` (panduan: "1. Datang ke Kantor Enuma", "2. Kembalikan barang ke tempat
  yang disediakan", "3. Upload bukti foto").
- [ ] **Upload Bukti Foto** — tap "Upload Bukti Foto" → masuk ke halaman capture → tap capture (
  preview
  muncul) → bisa "Ulangi" buat capture ulang → tap "Submit".
    - [ ] ⚠️ **Foto yang di-upload SELALU dummy/hardcoded** (URL Unsplash placeholder), bukan foto
      yang
      beneran di-capture — lihat gap #3 di `workflow-gaps.md`. Verifikasi ini dengan cek field
      `proofImageUrl` di Firestore setelah submit: harus selalu sama persis walau foto capture-nya
      beda.
    - [ ] Setelah submit sukses, navigasi balik ke History dengan back stack bersih (gak bisa back
      ke
      halaman upload/pengembalian lagi).
- [ ] **Verifikasi status akhir** — record di Firestore harus `status: Completed`, ada `returnDate`
  ke-set otomatis, dan history/timeline di app nampilin "Dikembalikan".
- [ ] **Asset balik bisa dipinjam lagi** — setelah Completed, asset yang sama bisa diajukan pinjam
  lagi
  dari user manapun (termasuk user yang sama).

## Edge case umum (kedua alur)

- [ ] Sign out di tengah ada request Pending — login ulang, pastikan state request masih konsisten
  (gak hilang/reset karena disimpan di Firestore, bukan local state).
- [ ] Rotasi layar / app di-kill lalu dibuka lagi saat di tengah proses (misal di halaman Upload
  Bukti
  Foto) — pastikan gak crash dan state re-load dengan benar dari `recordId` di SavedStateHandle.
- [ ] Koneksi internet mati saat submit (checkout, approve, reject, submit return) — pastikan muncul
  pesan error yang jelas, bukan silent fail atau infinite loading.
