package cinelux.bioskopcinelux.controller.menu;

import cinelux.bioskopcinelux.connection.DBConnect;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DashboardAdminCtrl implements Initializable {

    @FXML
    private Label lbPromo;
    @FXML
    private Label lbKursiTersedia;
    @FXML
    private Label lbFilmTersedia;

    public void tampilkanJumlahFilm() {
        String sql = "SELECT COUNT(*) AS jumlah_film FROM Film";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int jumlah = rs.getInt("jumlah_film");
                lbFilmTersedia.setText(jumlah + " Film");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void tampilkanJumlahKursiTersedia() {
        String sql = "SELECT COUNT(*) AS jumlah_kursi_tersedia FROM Kursi WHERE krs_active = 1";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int jumlah = rs.getInt("jumlah_kursi_tersedia");
                lbKursiTersedia.setText(jumlah + " Kursi Tersedia");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void tampilkanJumlahPromoAktif() {
        String query = "SELECT COUNT(*) AS jumlah FROM Promo WHERE prm_status = 1 AND GETDATE() BETWEEN prm_tgl_mulai AND prm_tgl_selesai";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int jumlahPromo = rs.getInt("jumlah");

                if (lbPromo != null) {
                    lbPromo.setText(jumlahPromo + " Promo Aktif");
                } else {
                    System.err.println("❌ lbPromo is null! Periksa FXML Anda.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (lbPromo != null) {
                lbPromo.setText("Gagal mengambil data promo");
            }
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tampilkanJumlahPromoAktif();
        tampilkanJumlahKursiTersedia();
        tampilkanJumlahFilm();
    }
}
