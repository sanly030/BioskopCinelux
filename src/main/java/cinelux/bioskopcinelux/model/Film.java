package cinelux.bioskopcinelux.model;

public class Film {
    private Integer id;
    private String judul;
    private String genre;
    private String durasi;
    private String rating_usia;
    private String poster;
    private int status;
    private String createdBy;

    private String modifiedBy;

    // Constructor default
    public Film() {}

    // Constructor lengkap (view / getAll)
    public Film(Integer id, String judul, String genre, String durasi,
                String rating_usia, String poster, int status,
                String createdBy,
                String modifiedBy) {
        this.id = id;
        this.judul = judul;
        this.genre = genre;
        this.durasi = durasi;
        this.rating_usia = rating_usia;
        this.poster = poster;
        this.status = status;
        this.createdBy = createdBy;
        this.modifiedBy = modifiedBy;

    }

    // Constructor untuk INSERT
    public Film(String judul, String genre, String durasi,
                String rating_usia, String poster, int status,
                String createdBy) {
        this.judul = judul;
        this.genre = genre;
        this.durasi = durasi;
        this.rating_usia = rating_usia;
        this.poster = poster;
        this.status = status;
        this.createdBy = createdBy;
    }

    // Constructor untuk UPDATE
    public Film(Integer id, String judul, String genre, String durasi,
                String rating_usia, String poster, int status,
                String modifiedBy) {
        this.id = id;
        this.judul = judul;
        this.genre = genre;
        this.durasi = durasi;
        this.rating_usia = rating_usia;
        this.poster = poster;
        this.status = status;
        this.modifiedBy = modifiedBy;
    }

    // ===== Getter & Setter =====
    public Integer getId() {return id;}
    public void setId(Integer id) {this.id = id;}
    public String getJudul() {return judul;}
    public void setJudul(String judul) {this.judul = judul;}
    public String getGenre() {return genre;}
    public void setGenre(String genre) {this.genre = genre;}
    public String getDurasi() {return durasi;}
    public void setDurasi(String durasi) {this.durasi = durasi;}
    public String getRating_usia() {return rating_usia;}
    public void setRating_usia(String rating_usia) {this.rating_usia = rating_usia;}
    public String getPoster() {return poster;}
    public void setPoster(String poster) {this.poster = poster;}
    public int getStatus() {return status;}
    public void setStatus(int status) {this.status = status;}
    public String getCreatedBy() {return createdBy;}
    public String getModifiedBy() {return modifiedBy;}
    public void setModifiedBy(String modifiedBy) {this.modifiedBy = modifiedBy;}
}

