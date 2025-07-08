package cinelux.bioskopcinelux.model;

public class Role {
    private static int id;
    private static String name;
    private static String role;
    private static String pict;


    public Role() {}
    public Role(int id, String name, String role, String pict) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.pict = pict;
    }

    public static String getPict() {
        return pict;
    }

    public static void setPict(String pict) {
        Role.pict = pict;
    }

    public static String getNama() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
