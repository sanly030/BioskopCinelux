package cinelux.bioskopcinelux.connection;

import java.sql.*;

public class DBConnect {
    public Connection conn;
    public Statement stat;
    public ResultSet result;
    public PreparedStatement pstat;
    public CallableStatement cstat;

    public DBConnect() {
        try {
            String url = "jdbc:sqlserver://localhost:1433;databaseName=CINELUXE;user=sa;password=1234;encrypt=false;trustServerCertificate=true;";
            conn = DriverManager.getConnection(url);
            stat = conn.createStatement();
            System.out.println("Koneksi Berhasil");
        } catch (Exception e) {
            System.out.println("Error saat connect database: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        DBConnect db = new DBConnect();
    }
}
