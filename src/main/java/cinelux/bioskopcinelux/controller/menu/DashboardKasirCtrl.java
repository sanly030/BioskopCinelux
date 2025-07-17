package cinelux.bioskopcinelux.controller.menu;

import cinelux.bioskopcinelux.connection.DBConnect;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class DashboardKasirCtrl implements Initializable {

    @FXML
    private Label lbPendapatan;
    @FXML
    private Label lbPendapatanTiket;
    @FXML
    private PieChart pieChart;
    @FXML
    private PieChart pieChartTiket;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadPendapatan();
        loadPieChart();
        loadPendapatanTiket();
        loadPieChartTiket();
    }

    private void loadPendapatan() {
        String sql = "SELECT SUM(tpm_total_harga) FROM TransaksiPenjualanMenu";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal(1);
                NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                lbPendapatan.setText(formatRupiah.format(total));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPendapatanTiket() {
        String sql = "SELECT SUM(tpt_total_harga) FROM TransaksiPembayaranTiket";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal(1);
                NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                lbPendapatanTiket.setText(formatRupiah.format(total));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPieChart() {
        pieChart.getData().clear();
        String sql = """
            SELECT m.mnu_nama, SUM(dpm.jumlah) AS total_jumlah
            FROM DetailPenjualanMenu dpm
            JOIN Menu m ON dpm.mnu_id = m.mnu_id
            GROUP BY m.mnu_nama
        """;

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int totalSemua = 0;
            try (Statement stTotal = conn.createStatement();
                 ResultSet rsTotal = stTotal.executeQuery("SELECT SUM(jumlah) FROM DetailPenjualanMenu")) {
                if (rsTotal.next()) {
                    totalSemua = rsTotal.getInt(1);
                }
            }

            while (rs.next()) {
                String namaMenu = rs.getString("mnu_nama");
                int jumlah = rs.getInt("total_jumlah");
                double persentase = (double) jumlah / totalSemua * 100;

                PieChart.Data slice = new PieChart.Data(namaMenu, jumlah);
                pieChart.getData().add(slice);

                String tooltipText = String.format("%s\nJumlah: %d\n%.2f%%", namaMenu, jumlah, persentase);
                Tooltip tooltip = new Tooltip(tooltipText);
                Tooltip.install(slice.getNode(), tooltip);

                slice.getNode().setOnMouseEntered(e -> {
                    slice.getNode().setStyle("-fx-cursor: hand;");
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPieChartTiket() {
        pieChartTiket.getData().clear();
        String sql = """
            SELECT f.flm_judul, SUM(tpt.tpt_jumlah_tiket) AS total_tiket
            FROM TransaksiPembayaranTiket tpt
            JOIN JadwalTayang jt ON tpt.jty_id = jt.jty_id
            JOIN Film f ON jt.flm_id = f.flm_id
            GROUP BY f.flm_judul
        """;

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int totalTiket = 0;
            try (Statement st = conn.createStatement();
                 ResultSet rsTotal = st.executeQuery("SELECT SUM(tpt_jumlah_tiket) FROM TransaksiPembayaranTiket")) {
                if (rsTotal.next()) {
                    totalTiket = rsTotal.getInt(1);
                }
            }

            while (rs.next()) {
                String judulFilm = rs.getString("flm_judul");
                int jumlah = rs.getInt("total_tiket");
                double persentase = (double) jumlah / totalTiket * 100;

                PieChart.Data slice = new PieChart.Data(judulFilm, jumlah);
                pieChartTiket.getData().add(slice);

                String tooltipText = String.format("%s\nJumlah Tiket: %d\n%.2f%%", judulFilm, jumlah, persentase);
                Tooltip tooltip = new Tooltip(tooltipText);
                Tooltip.install(slice.getNode(), tooltip);

                slice.getNode().setOnMouseEntered(e -> {
                    slice.getNode().setStyle("-fx-cursor: hand;");
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}