package cinelux.bioskopcinelux.model;


public class Karyawan {
    private Integer id;               // pgw_id
    private Setting role;             // Relasi ke dtSetting (tst_id)
    private String nama;              // pgw_nama
    private String username;          // pgw_username
    private String password;          // pgw_password
    private String noTelp;            // pgw_no_telp
    private String alamat;            // pgw_alamat
    private int status;               // pgw_status (1 = aktif, 0 = nonaktif)
    private String createdBy;         // pgw_created_by
    private String modifiedBy;        // pgw_modif_by

    // Constructor kosong
    public Karyawan() {
    }

    public Karyawan (Integer id, Setting role, String nama, String username, String password, String noTelp, String alamat, int status, String createdBy, String modifiedBy) {
        this.id = id;
        this.role = role;
        this.nama = nama;
        this.username = username;
        this.password = password;
        this.noTelp = noTelp;
        this.alamat = alamat;
        this.status = status;
        this.createdBy = createdBy;
        this.modifiedBy = modifiedBy;
    }

    // Constructor untuk INSERT (tanpa id & modifiedBy)
    public Karyawan(Setting role, String nama, String username, String password,
                    String noTelp, String alamat, String createdBy) {
        this.role = role;
        this.nama = nama;
        this.username = username;
        this.password = password;
        this.noTelp = noTelp;
        this.alamat = alamat;
        this.status = 1; // Default aktif saat insert
        this.createdBy = createdBy;
    }

    // Constructor untuk UPDATE (dengan id & modifiedBy)
    public Karyawan(Integer id, Setting role, String nama, String username, String password,
                    String noTelp, String alamat, String modifiedBy) {
        this.id = id;
        this.role = role;
        this.nama = nama;
        this.username = username;
        this.password = password;
        this.noTelp = noTelp;
        this.alamat = alamat;
        this.modifiedBy = modifiedBy;
    }

    public Integer getId() {return id;}
    public void setId(Integer id) {this.id = id;}
    public Setting getRole() {return role;}
    public void setRole(Setting role) {this.role = role;}
    public String getNama() {return nama;}
    public void setNama(String nama) {this.nama = nama;}
    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}
    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}
    public String getNoTelp() {return noTelp;}
    public void setNoTelp(String noTelp) {this.noTelp = noTelp;}
    public String getAlamat() {return alamat;}
    public void setAlamat(String alamat) {this.alamat = alamat;}
    public int getStatus() {return status;}
    public void setStatus(int status) {this.status = status;}
    public String getCreatedBy() {return createdBy;}
    public void setCreatedBy(String createdBy) {this.createdBy = createdBy;}
    public String getModifiedBy() {return modifiedBy;}
    public void setModifiedBy(String modifiedBy) {this.modifiedBy = modifiedBy;}

}