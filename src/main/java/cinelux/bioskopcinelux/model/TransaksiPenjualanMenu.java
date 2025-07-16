package cinelux.bioskopcinelux.model;

import java.time.LocalDateTime;
import java.util.List;

public class TransaksiPenjualanMenu {
    private Integer id;
    private Karyawan pegawai;
    private Setting metodePembayaran; // Referensi ke dtSetting (kategori: Metode Pembayaran)
    private Promo promo;              // Bisa null
    private int jumlahMenu;
    private Double totalHarga;
    private LocalDateTime tanggal;
    private String createdBy;
    private LocalDateTime createdDate;

    // Tambahan untuk detail penjualan
    private List<DetailPenjualanMenu> detailPenjualanMenu;

    // Constructor tanpa ID (untuk insert)
    public TransaksiPenjualanMenu(Karyawan pegawai, Setting metodePembayaran, Promo promo,
                                  int jumlahMenu, Double totalHarga, LocalDateTime tanggal,
                                  String createdBy, LocalDateTime createdDate) {
        this.pegawai = pegawai;
        this.metodePembayaran = metodePembayaran;
        this.promo = promo;
        this.jumlahMenu = jumlahMenu;
        this.totalHarga = totalHarga;
        this.tanggal = tanggal;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
    }

    // Constructor lengkap (untuk ambil data dari DB)
    public TransaksiPenjualanMenu(Integer id, Karyawan pegawai, Setting metodePembayaran, Promo promo,
                                  int jumlahMenu, Double totalHarga, LocalDateTime tanggal,
                                  String createdBy, LocalDateTime createdDate) {
        this(pegawai, metodePembayaran, promo, jumlahMenu, totalHarga, tanggal, createdBy, createdDate);
        this.id = id;
    }

    // Default constructor
    public TransaksiPenjualanMenu() {
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

    public int getJumlahMenu() { return jumlahMenu; }
    public void setJumlahMenu(int jumlahMenu) { this.jumlahMenu = jumlahMenu; }

    public Double getTotalHarga() { return totalHarga; }
    public void setTotalHarga(Double totalHarga) { this.totalHarga = totalHarga; }

    public LocalDateTime getTanggal() { return tanggal; }
    public void setTanggal(LocalDateTime tanggal) { this.tanggal = tanggal; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public List<DetailPenjualanMenu> getDetailPenjualanMenu() { return detailPenjualanMenu; }
    public void setDetailPenjualanMenu(List<DetailPenjualanMenu> detailPenjualanMenu) {
        this.detailPenjualanMenu = detailPenjualanMenu;
    }
}