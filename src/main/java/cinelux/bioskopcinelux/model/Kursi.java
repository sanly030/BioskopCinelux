package cinelux.bioskopcinelux.model;

import java.time.LocalDateTime;

public class Kursi implements Comparable<Kursi> {
    private Integer id;
    private Studio studio;
    private String baris;
    private int kolom;
    private boolean active;

    private String nomor; // A1, B3, dll (bisa dikalkulasi otomatis atau diisi manual)

    private String createdBy;
    private LocalDateTime createdDate;
    private String modifiedBy;
    private LocalDateTime modifiedDate;

    public Kursi() {}

    public Kursi(Integer id, Studio studio, String baris, int kolom, boolean active,
                 String createdBy, LocalDateTime createdDate,
                 String modifiedBy, LocalDateTime modifiedDate) {
        this.id = id;
        this.studio = studio;
        this.baris = baris;
        this.kolom = kolom;
        this.active = active;
        this.nomor = baris + kolom;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.modifiedBy = modifiedBy;
        this.modifiedDate = modifiedDate;
    }

    // Getter dan Setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Studio getStudio() {
        return studio;
    }

    public void setStudio(Studio studio) {
        this.studio = studio;
    }

    public String getBaris() {
        return baris;
    }

    public void setBaris(String baris) {
        this.baris = baris;
        updateNomor();
    }

    public int getKolom() {
        return kolom;
    }

    public void setKolom(int kolom) {
        this.kolom = kolom;
        updateNomor();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getNomor() {
        return nomor;
    }

    public void setNomor(String nomor) {
        this.nomor = nomor;
    }

    private void updateNomor() {
        if (this.baris != null && this.kolom > 0) {
            this.nomor = baris + kolom;
        }
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Override
    public int compareTo(Kursi o) {
        int cmp = this.baris.compareTo(o.baris);
        if (cmp != 0) return cmp;
        return Integer.compare(this.kolom, o.kolom);

    }
}
