package cinelux.bioskopcinelux.model;

public class Setting {
    private Integer id;
    private String nama;
    private String value;
    private String kategori;
    private int status;
    private String created_by;
    private String modified_by;

    public Setting() {}

    @Override
    public String toString() {
        return nama; // tampilkan hanya nama role
    }


    public Setting(Integer id, String nama, String value, String kategori, int status, String created_by, String modified_by) {
        this.id = id;
        this.nama = nama;
        this.value = value;
        this.kategori = kategori;
        this.status = status;
        this.created_by = created_by;
        this.modified_by = modified_by;
    }

    public Setting(String nama, String value, String kategori, String created_by) {
        this.nama = nama;
        this.value = value;
        this.kategori = kategori;
        this.created_by = created_by;
    }

    public Setting(Integer id, String nama, String value, String kategori, String modified_by) {
        this.id = id;
        this.nama = nama;
        this.value = value;
        this.kategori = kategori;
        this.modified_by = modified_by;
    }


    public Integer getId() {return id;}
    public void setId(Integer id) {this.id = id;}
    public String getNama() {return nama;}
    public void setNama(String nama) {this.nama = nama;}
    public String getValue() {return value;}
    public void setValue(String value) {this.value = value;}
    public String getKategori() {return kategori;}public void setKategori(String kategori) {this.kategori = kategori;}
    public int getStatus() {return status;}
    public void setStatus(int status) {this.status = status;}
    public String getCreated_by() {return created_by;}public void setCreated_by(String created_by) {this.created_by = created_by;}
    public String getModified_by() {return modified_by;}
    public void setModified_by(String modified_by) {this.modified_by = modified_by;}

}
