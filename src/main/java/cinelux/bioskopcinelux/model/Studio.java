package cinelux.bioskopcinelux.model;

public class Studio {
    private Integer id;
    private String nama;
    private int baris;
    private int kolom;
    private int kapasitas;
    private int status;
    private String createdBy;
    private String modifiedBy;

    // Constructor default
    public Studio() {}

    // Constructor lengkap (view / getAll)
    public Studio(Integer id, String nama, int baris, int kolom, int kapasitas,
                  int status, String createdBy, String modifiedBy) {
        this.id = id;
        this.nama = nama;
        this.baris = baris;
        this.kolom = kolom;
        this.kapasitas = kapasitas;
        this.status = status;
        this.createdBy = createdBy;
        this.modifiedBy = modifiedBy;
    }

    // Constructor untuk INSERT
    public Studio(String nama, int baris, int kolom, int kapasitas,
                  int status, String createdBy) {
        this.nama = nama;
        this.baris = baris;
        this.kolom = kolom;
        this.kapasitas = kapasitas;
        this.status = status;
        this.createdBy = createdBy;
    }

    // Constructor untuk UPDATE
    public Studio(Integer id, String nama, int baris, int kolom, int kapasitas,
                  int status, String modifiedBy) {
        this.id = id;
        this.nama = nama;
        this.baris = baris;
        this.kolom = kolom;
        this.kapasitas = kapasitas;
        this.status = status;
        this.modifiedBy = modifiedBy;
    }

    // Getter dan Setter
    public Integer getId() {return id;}
    public void setId(Integer id) {this.id = id;}
    public String getNama() {return nama;}
    public void setNama(String nama) {this.nama = nama;}
    public int getBaris() {return baris;}
    public void setBaris(int baris) {this.baris = baris;}
    public int getKolom() {return kolom;}
    public void setKolom(int kolom) {this.kolom = kolom;}
    public int getKapasitas() {return kapasitas;}
    public void setKapasitas(int kapasitas) {this.kapasitas = kapasitas;}
    public int getStatus() {return status;}
    public void setStatus(int status) {this.status = status;}
    public String getCreatedBy() {return createdBy;}
    public void setCreatedBy(String createdBy) {this.createdBy = createdBy;}
    public String getModifiedBy() {return modifiedBy;}
    public void setModifiedBy(String modifiedBy) {this.modifiedBy = modifiedBy;}
}



