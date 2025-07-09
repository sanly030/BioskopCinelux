package cinelux.bioskopcinelux.model;

public class Kursi {
    private Integer id;
    private Studio idStudio;
    private int status;
    private String createdBy;
    private String modifiedBy;

    public Kursi() {}

    public Kursi(Integer id, Studio idStudio, int status, String createdBy, String modifiedBy) {
        this.id = id;
        this.idStudio = idStudio;
        this.status = status;
        this.createdBy = createdBy;
        this.modifiedBy = modifiedBy;
    }

    public Integer getId() {return id;}
    public void setId(Integer id) {this.id = id;}
    public Studio getIdStudio() {return idStudio;}
    public void setIdStudio(Studio idStudio) {this.idStudio = idStudio;}
    public int getStatus() {return status;}
    public void setStatus(int status) {this.status = status;}
    public String getCreatedBy() {return createdBy;}
    public void setCreatedBy(String createdBy) {this.createdBy = createdBy;}
    public String getModifiedBy() {return modifiedBy;}
    public void setModifiedBy(String modifiedBy) {this.modifiedBy = modifiedBy;}
}
