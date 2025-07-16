package cinelux.bioskopcinelux.model;

import java.time.LocalDateTime;
import java.util.List;

public class TransaksiPenjualanTiket {
    private Integer id;
    private Karyawan pegawai;
    private Setting metodePembayaran; // Referensi ke dtSetting kategori: Metode Pembayaran
    private Promo promo;              // Bisa null
    private JadwalTayang jadwal;
    private LocalDateTime tglTransaksi;
    private Double hargaTiket;
    private int jumlahTiket;
    private Double totalHarga;
    private String statusKursi;
    private String createdBy;
    private LocalDateTime createdDate;

    // Detail Penjualan Tiket
    private List<DetailPenjualanTiket> detailPenjualanTiket;

    // Constructor tanpa ID (untuk insert)
    public TransaksiPenjualanTiket(Karyawan pegawai, Setting metodePembayaran, Promo promo, JadwalTayang jadwal,
                                   LocalDateTime tglTransaksi, Double hargaTiket, int jumlahTiket, Double totalHarga,
                                   String statusKursi, String createdBy, LocalDateTime createdDate) {
        this.pegawai = pegawai;
        this.metodePembayaran = metodePembayaran;
        this.promo = promo;
        this.jadwal = jadwal;
        this.tglTransaksi = tglTransaksi;
        this.hargaTiket = hargaTiket;
        this.jumlahTiket = jumlahTiket;
        this.totalHarga = totalHarga;
        this.statusKursi = statusKursi;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
    }

    // Constructor lengkap (untuk ambil data dari DB)
    public TransaksiPenjualanTiket(Integer id, Karyawan pegawai, Setting metodePembayaran, Promo promo, JadwalTayang jadwal,
                                   LocalDateTime tglTransaksi, Double hargaTiket, int jumlahTiket, Double totalHarga,
                                   String statusKursi, String createdBy, LocalDateTime createdDate) {
        this(pegawai, metodePembayaran, promo, jadwal, tglTransaksi, hargaTiket, jumlahTiket, totalHarga, statusKursi, createdBy, createdDate);
        this.id = id;
    }

    // Default constructor
    public TransaksiPenjualanTiket() {
    }

    // Getter & Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Karyawan getPegawai() { return pegawai; }
    public void setPegawai(Karyawan pegawai) { this.pegawai = pegawai; }

    public Setting getMetodePembayaran() { return metodePembayaran; }
    public void setMetodePembayaran(Setting metodePembayaran) { this.metodePembayaran = metodePembayaran; }

    public Promo getPromo() { return promo; }
    public void setPromo(Promo promo) { this.promo = promo; }

    public LocalDateTime getTglTransaksi() { return tglTransaksi; }
    public void setTglTransaksi(LocalDateTime tglTransaksi) { this.tglTransaksi = tglTransaksi; }

    public Double getHargaTiket() { return hargaTiket; }
    public void setHargaTiket(Double hargaTiket) { this.hargaTiket = hargaTiket; }

    public int getJumlahTiket() { return jumlahTiket; }
    public void setJumlahTiket(int jumlahTiket) { this.jumlahTiket = jumlahTiket; }

    public Double getTotalHarga() { return totalHarga; }
    public void setTotalHarga(Double totalHarga) { this.totalHarga = totalHarga; }

    public JadwalTayang getJadwal() {
        return jadwal;
    }

    public void setJadwal(JadwalTayang jadwal) {
        this.jadwal = jadwal;
    }

    public String getStatusKursi() { return statusKursi; }
    public void setStatusKursi(String statusKursi) { this.statusKursi = statusKursi; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public List<DetailPenjualanTiket> getDetailPenjualanTiket() { return detailPenjualanTiket; }
    public void setDetailPenjualanTiket(List<DetailPenjualanTiket> detailPenjualanTiket) {
        this.detailPenjualanTiket = detailPenjualanTiket;
    }
}
