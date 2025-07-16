package cinelux.bioskopcinelux.model;

import java.math.BigDecimal;
import java.sql.Time;

public class JadwalTayang {
    private Integer id;
    private Film namaFilm;
    private Studio namaStudio;
    private Time jamMulai;
    private Time jamSelesai;
    private String jenisHari;
    private BigDecimal harga;
    private int status;
    private String createdBy;
    private String modifiedBy;

    // ======================== Constructors ========================
    public JadwalTayang() {}

    // Constructor lengkap untuk view (getAll)
    public JadwalTayang(Integer id, Film namaFilm, Studio namaStudio, Time jamMulai, Time jamSelesai,
                        String jenisHari, BigDecimal harga, int status,
                        String createdBy, String modifiedBy) {
        this.id = id;
        this.namaFilm = namaFilm;
        this.namaStudio = namaStudio;
        this.jamMulai = jamMulai;
        this.jamSelesai = jamSelesai;
        this.jenisHari = jenisHari;
        this.harga = harga;
        this.status = status;
        this.createdBy = createdBy;
        this.modifiedBy = modifiedBy;
    }

    // Constructor untuk insert
    public JadwalTayang(Film namaFilm, Studio namaStudio, Time jamMulai, Time jamSelesai,
                        String jenisHari, BigDecimal harga, int status, String createdBy) {
        this.namaFilm = namaFilm;
        this.namaStudio = namaStudio;
        this.jamMulai = jamMulai;
        this.jamSelesai = jamSelesai;
        this.jenisHari = jenisHari;
        this.harga = harga;
        this.status = status;
        this.createdBy = createdBy;
    }

    // Constructor untuk update
    public JadwalTayang(Integer id, Film namaFilm, Studio namaStudio, Time jamMulai, Time jamSelesai,
                        String jenisHari, BigDecimal harga, int status, String modifiedBy) {
        this.id = id;
        this.namaFilm = namaFilm;
        this.namaStudio = namaStudio;
        this.jamMulai = jamMulai;
        this.jamSelesai = jamSelesai;
        this.jenisHari = jenisHari;
        this.harga = harga;
        this.status = status;
        this.modifiedBy = modifiedBy;
    }

    // ======================== Getters & Setters ========================
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Film getNamaFilm() {
        return namaFilm;
    }

    public void setNamaFilm(Film namaFilm) {
        this.namaFilm = namaFilm;
    }

    public Studio getNamaStudio() {
        return namaStudio;
    }

    public void setNamaStudio(Studio namaStudio) {
        this.namaStudio = namaStudio;
    }

    public Time getJamMulai() {
        return jamMulai;
    }

    public void setJamMulai(Time jamMulai) {
        this.jamMulai = jamMulai;
    }

    public Time getJamSelesai() {
        return jamSelesai;
    }

    public void setJamSelesai(Time jamSelesai) {
        this.jamSelesai = jamSelesai;
    }

    public String getJenisHari() {
        return jenisHari;
    }

    public void setJenisHari(String jenisHari) {
        this.jenisHari = jenisHari;
    }

    public BigDecimal getHarga() {
        return harga;
    }

    public void setHarga(BigDecimal harga) {
        this.harga = harga;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
}
