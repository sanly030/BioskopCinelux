package cinelux.bioskopcinelux.model;

import java.util.Set;

public class Menu {
    private Integer id;
    private Setting jenis_makanan;
    private String nama;
    private int stok;
    private Double harga;
    private int status;
    private String createdBy;
    private String modifiedBy;


    public Menu() {}

    public Menu(Integer id,Setting jenis_makanan, String nama, int stok , Double harga, int status, String createdBy, String modifiedBy) {
        this.id = id;
        this.jenis_makanan = jenis_makanan;
        this.nama = nama;
        this.stok = stok;
        this.harga = harga;
        this.status = status;
        this.createdBy = createdBy;
        this.modifiedBy = modifiedBy;
    }

    public Menu(Setting jenis_makanan, String nama, int stok,Double harga, int status, String createdBy) {
        this.jenis_makanan = jenis_makanan;
        this.nama = nama;
        this.stok = stok;
        this.harga = harga;
        this.status = status;
        this.createdBy = createdBy;
    }

    public Menu(Integer id, Setting jenis_makanan, String nama, int stok ,Double harga, int status, String modifiedBy) {
        this.id = id;
        this.jenis_makanan = jenis_makanan;
        this.nama = nama;
        this.stok = stok;
        this.harga = harga;
        this.status = status;
        this.modifiedBy = modifiedBy;
    }

    public Integer getId() {return id;}

    public void setId(Integer id) {this.id = id;}
    public Setting getJenis_makanan() {return jenis_makanan;}
    public void setJenis_makanan(Setting jenis_makanan) {this.jenis_makanan = jenis_makanan;}
    public String getNama() {return nama;}
    public void setNama(String nama) {this.nama = nama;}
    public int getStok() {return stok;}
    public void setStok(int stok) {this.stok = stok;}
    public Double getHarga() {return harga;}
    public void setHarga(Double harga) {this.harga = harga;}
    public int getStatus() {return status;}
    public void setStatus(int status) {this.status = status;}
    public String getCreatedBy() {return createdBy;}
    public void setCreatedBy(String createdBy) {this.createdBy = createdBy;}
    public String getModifiedBy() {return modifiedBy;}
    public void setModifiedBy(String modifiedBy) {this.modifiedBy = modifiedBy;}
}
