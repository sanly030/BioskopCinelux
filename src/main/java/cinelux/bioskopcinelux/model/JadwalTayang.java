package cinelux.bioskopcinelux.model;

import java.sql.Time;

public class JadwalTayang {
    private Integer id;
    private Film namaFilm;
    private Studio namaStudio;
    private Time jamMulai;
    private Time jamSelesai;
    private String jenisHari;
    private Double harga;
    private int status;
    private String createdBy;
    private String modifiedBy;

    public JadwalTayang() {}

    public JadwalTayang(Integer id, Film namaFilm, Studio namaStudio, Time jamMulai, Time jamSelesai, String jenisHari, Double harga, int status, String createdBy, String modifiedBy) {
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

    public JadwalTayang(Film namaFilm, Studio namaStudio, Time jamMulai, Time jamSelesai, String jenisHari, Double harga, int status, String createdBy) {
        this.namaFilm = namaFilm;
        this.namaStudio = namaStudio;
        this.jamMulai = jamMulai;
        this.jamSelesai = jamSelesai;
        this.jenisHari = jenisHari;
        this.harga = harga;
        this.status = status;
        this.createdBy = createdBy;
    }

    public JadwalTayang(Integer id, Film namaFilm, Studio namaStudio, Time jamMulai, Time jamSelesai, String jenisHari, Double harga, int status, String modifiedBy) {
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

    // Getters and setters if needed


    public Integer getId() {return id;}
    public void setId(Integer id) {this.id = id;}
    public Film getNamaFilm() {return namaFilm;}
    public void setNamaFilm(Film idFilm) {this.namaFilm = idFilm;}
    public Studio getNamaStudio() {return namaStudio;}
    public void setNamaStudio(Studio idStudio) {this.namaStudio = idStudio;}
    public Time getJamMulai() {return jamMulai;}
    public void setJamMulai(Time jamMulai) {this.jamMulai = jamMulai;}
    public Time getJamSelesai() {return jamSelesai;}
    public void setJamSelesai(Time jamSelesai) {this.jamSelesai = jamSelesai;}
    public String getJenisHari() {return jenisHari;}
    public void setJenisHari(String jenisHari) {this.jenisHari = jenisHari;}
    public Double getHarga() {return harga;}
    public void setHarga(Double harga) {this.harga = harga;}
    public int getStatus() {return status;}
    public void setStatus(int status) {this.status = status;}
    public String getCreatedBy() {return createdBy;}
    public void setCreatedBy(String createdBy) {this.createdBy = createdBy;}
    public String getModifiedBy() {return modifiedBy;}
    public void setModifiedBy(String modifiedBy) {this.modifiedBy = modifiedBy;}
}
