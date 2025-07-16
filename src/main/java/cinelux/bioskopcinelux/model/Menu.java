package cinelux.bioskopcinelux.model;

    public class Menu {
    private Integer id;
    private Setting jenis_makanan;
    private String nama;
    private int stok;
    private Double harga;
    private String gambar;
    private int status;
    private String createdBy;
    private String modifiedBy;

    public Menu() {}

    // ✅ Constructor lengkap (biasanya untuk mapping dari DB)
    public Menu(Integer id, Setting jenis_makanan, String nama, int stok, Double harga, String gambar, int status, String createdBy, String modifiedBy) {
        this.id = id;
        this.jenis_makanan = jenis_makanan;
        this.nama = nama;
        this.stok = stok;
        this.harga = harga;
        this.gambar = gambar;
        this.status = status;
        this.createdBy = createdBy;
        this.modifiedBy = modifiedBy;
    }

        public Menu(Integer id, Setting jenis_makanan, String nama, int stok, double harga, int status, String gambar, String createdBy) {
            this.id = id;
            this.jenis_makanan = jenis_makanan;
            this.nama = nama;
            this.stok = stok;
            this.harga = harga;
            this.status = status;
            this.gambar = gambar;
            this.createdBy = createdBy;
        }

        public Menu(int id, Setting jenis_makanan, String nama, int stok, double harga, int status, String gambar, String modifiedBy) {
            this.id = id;
            this.jenis_makanan = jenis_makanan;
            this.nama = nama;
            this.stok = stok;
            this.harga = harga;
            this.status = status;
            this.gambar = gambar;
            this.modifiedBy = modifiedBy;
        }


        // Getter & Setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Setting getJenis_makanan() {
        return jenis_makanan;
    }

    public void setJenis_makanan(Setting jenis_makanan) {
        this.jenis_makanan = jenis_makanan;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public int getStok() {
        return stok;
    }

    public void setStok(int stok) {
        this.stok = stok;
    }

    public Double getHarga() {
        return harga;
    }

    public void setHarga(Double harga) {
        this.harga = harga;
    }

    public String getGambar() {
        return gambar;
    }

    public void setGambar(String gambar) {
        this.gambar = gambar;
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
