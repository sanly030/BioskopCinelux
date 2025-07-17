package cinelux.bioskopcinelux.controller.menu;

import cinelux.bioskopcinelux.connection.DBConnect;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class DashboardManagerCtrl implements Initializable {
    @FXML
    private Label lbPendapatan;
    @FXML
    private Label lbPenjualanMenu;
    @FXML
    private Label lbMenu;
    @FXML
    private Label lbTotalTiket;

    private void tampilkanTotalTiketTerjual() {
        String query = "SELECT SUM(tpt_jumlah_tiket) AS total FROM TransaksiPembayaranTiket";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int total = rs.getInt("total");
                lbTotalTiket.setText(String.valueOf(total));
            } else {
                lbTotalTiket.setText("0");
            }
        } catch (Exception e) {
            e.printStackTrace();
            lbTotalTiket.setText("Error");
        }
    }


    private void tampilkanMenuPalingSeringDipesan() {
        String query = """
        SELECT TOP 1 m.mnu_nama AS nama_menu, SUM(dpm.jumlah) AS total
        FROM DetailPenjualanMenu dpm
        JOIN Menu m ON dpm.mnu_id = m.mnu_id
        GROUP BY m.mnu_nama
        ORDER BY total DESC
    """;

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String namaMenu = rs.getString("nama_menu");
                lbMenu.setText(namaMenu);
            } else {
                lbMenu.setText("Belum ada penjualan");
            }
        } catch (Exception e) {
            e.printStackTrace();
            lbMenu.setText("Error");
        }
    }


    private void tampilkanJumlahPenjualanMenu() {
        String query = "SELECT COUNT(*) FROM TransaksiPenjualanMenu";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int jumlah = rs.getInt(1);
                lbPenjualanMenu.setText(String.valueOf(jumlah));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tampilkanTotalPendapatan() {
        String query = """
                SELECT 
                    ISNULL((SELECT SUM(tpt_total_harga) FROM TransaksiPembayaranTiket), 0) +
                    ISNULL((SELECT SUM(tpm_total_harga) FROM TransaksiPenjualanMenu), 0) AS total_pendapatan
                """;

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                double total = rs.getDouble("total_pendapatan");
                String formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(total);
                lbPendapatan.setText(formatRupiah);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tampilkanTotalPendapatan();
        tampilkanJumlahPenjualanMenu();
        tampilkanMenuPalingSeringDipesan();
        tampilkanTotalTiketTerjual();
    }
}
