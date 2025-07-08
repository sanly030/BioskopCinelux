package cinelux.bioskopcinelux.model;

import java.sql.Date;

public class Promo {
    private Integer id;
    private String nama_promo;
    private String tipe_promo;
    private Double diskon;
    private Date tanggal_mulai;
    private Date tanggal_selesai;
    private Integer status;
    private String createdBy;
    private String createdDate;
    private String modifiedBy;
    private String modifiedDate;

    // Constructor default
    public Promo() {}

    // Constructor lengkap (untuk update/view)
    public Promo(Integer id, String nama_promo, String tipe_promo, Double diskon,
                 Date tanggal_mulai, Date tanggal_selesai, Integer status,
                 String createdBy, String createdDate, String modifiedBy, String modifiedDate) {
        this.id = id;
        this.nama_promo = nama_promo;
        this.tipe_promo = tipe_promo;
        this.diskon = diskon;
        this.tanggal_mulai = tanggal_mulai;
        this.tanggal_selesai = tanggal_selesai;
        this.status = status;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.modifiedBy = modifiedBy;
        this.modifiedDate = modifiedDate;
    }

    // Constructor untuk INSERT
    public Promo(String nama_promo, String tipe_promo, Double diskon,
                 Date tanggal_mulai, Date tanggal_selesai,
                 Integer status, String createdBy) {
        this.nama_promo = nama_promo;
        this.tipe_promo = tipe_promo;
        this.diskon = diskon;
        this.tanggal_mulai = tanggal_mulai;
        this.tanggal_selesai = tanggal_selesai;
        this.status = status;
        this.createdBy = createdBy;
    }

    // Constructor untuk UPDATE
    public Promo(Integer id, String nama_promo, String tipe_promo, Double diskon,
                 Date tanggal_mulai, Date tanggal_selesai,
                 Integer status, String modifiedBy) {
        this.id = id;
        this.nama_promo = nama_promo;
        this.tipe_promo = tipe_promo;
        this.diskon = diskon;
        this.tanggal_mulai = tanggal_mulai;
        this.tanggal_selesai = tanggal_selesai;
        this.status = status;
        this.modifiedBy = modifiedBy;
    }


    @Override
    public String toString() {
        return nama_promo; // agar tampil nama promo saat di-comboBox atau log
    }

    // Getter & Setter
    public Integer getId() {return id;}
    public void setId(Integer id) {this.id = id;}
    public String getNama_promo() {return nama_promo;}
    public void setNama_promo(String nama_promo) {this.nama_promo = nama_promo;}
    public String getTipe_promo() {return tipe_promo;}
    public void setTipe_promo(String tipe_promo) {this.tipe_promo = tipe_promo;}
    public Double getDiskon() {return diskon;}
    public Date getTanggal_mulai() {return tanggal_mulai;}
    public Date getTanggal_selesai() {return tanggal_selesai;}
    public void setTanggal_selesai(Date tanggal_selesai) {this.tanggal_selesai = tanggal_selesai;}
    public Integer getStatus() {return status;}
    public void setStatus(Integer status) {this.status = status;}
    public String getCreatedBy() {return createdBy;}
    public void setCreatedBy(String createdBy) {this.createdBy = createdBy;}
    public String getCreatedDate() {return createdDate;}
    public void setCreatedDate(String createdDate) {this.createdDate = createdDate;}
    public String getModifiedBy() {return modifiedBy;}
    public void setModifiedBy(String modifiedBy) {this.modifiedBy = modifiedBy;}
    public String getModifiedDate() {return modifiedDate;}
    public void setModifiedDate(String modifiedDate) {this.modifiedDate = modifiedDate;}
}

