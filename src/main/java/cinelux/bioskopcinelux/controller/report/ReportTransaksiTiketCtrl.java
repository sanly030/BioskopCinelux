package cinelux.bioskopcinelux.controller.report;

import cinelux.bioskopcinelux.connection.DBConnect;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

import javax.swing.*;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.ResourceBundle;

public class ReportTransaksiTiketCtrl implements Initializable {

    @FXML
    private Button btnFilter;

    @FXML
    private Button btnClear;

    @FXML
    private Button btnExportJasper;

    @FXML
    private ComboBox<String> comboBoxFilm;

    @FXML
    private DatePicker dateDari;

    @FXML
    private DatePicker dateSampai;

    @FXML
    private BarChart<String, Number> barChartPenjualan;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private Label labelStatus;

    DBConnect db = new DBConnect();
    private ObservableList<String> filmList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadFilmData();
        loadInitialChartData();
        setupDatePickers();
    }

    private void setupDatePickers() {
        // Set default date range (last 30 days)
        dateSampai.setValue(LocalDate.now());
        dateDari.setValue(LocalDate.now().minusDays(30));
    }

    private void loadFilmData() {
        try {
            filmList.clear();
            filmList.add("Semua Film"); // Option untuk menampilkan semua film

            String query = "SELECT DISTINCT f.flm_judul FROM Film f " +
                    "INNER JOIN JadwalTayang jt ON f.flm_id = jt.flm_id " +
                    "INNER JOIN TransaksiPembayaranTiket tpt ON jt.jty_id = tpt.jty_id " +
                    "WHERE f.flm_status = 1 " +
                    "ORDER BY f.flm_judul";

            PreparedStatement ps = db.conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                filmList.add(rs.getString("flm_judul"));
            }

            comboBoxFilm.setItems(filmList);
            comboBoxFilm.getSelectionModel().selectFirst(); // Select "Semua Film" by default

            rs.close();
            ps.close();

        } catch (SQLException e) {
            e.printStackTrace();
            labelStatus.setText("Error: Gagal memuat data film");
        }
    }

    private void loadInitialChartData() {
        loadChartData(null, null, null);
    }

    @FXML
    void handleFilterData(ActionEvent event) {
        String selectedFilm = comboBoxFilm.getSelectionModel().getSelectedItem();
        LocalDate dateFrom = dateDari.getValue();
        LocalDate dateTo = dateSampai.getValue();

        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            labelStatus.setText("Error: Tanggal 'Dari' tidak boleh lebih besar dari 'Sampai'");
            return;
        }

        // Jika "Semua Film" dipilih, set selectedFilm menjadi null
        if ("Semua Film".equals(selectedFilm)) {
            selectedFilm = null;
        }

        loadChartData(selectedFilm, dateFrom, dateTo);
        labelStatus.setText("Filter berhasil diterapkan");
    }

    @FXML
    void handleClearFilter(ActionEvent event) {
        comboBoxFilm.getSelectionModel().selectFirst(); // Select "Semua Film"
        dateDari.setValue(LocalDate.now().minusDays(30));
        dateSampai.setValue(LocalDate.now());

        loadChartData(null, null, null);
        labelStatus.setText("Filter berhasil direset");
    }

    @FXML
    void handleExportJasper(ActionEvent event) {
        // Method yang sudah ada - jangan dihapus
        onButtonLihatLaporan(event);
    }

    private void loadChartData(String selectedFilm, LocalDate dateFrom, LocalDate dateTo) {
        try {
            barChartPenjualan.getData().clear();

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT f.flm_judul, SUM(tpt.tpt_jumlah_tiket) as total_tiket, ")
                    .append("SUM(tpt.tpt_total_harga) as total_pendapatan ")
                    .append("FROM Film f ")
                    .append("INNER JOIN JadwalTayang jt ON f.flm_id = jt.flm_id ")
                    .append("INNER JOIN TransaksiPembayaranTiket tpt ON jt.jty_id = tpt.jty_id ")
                    .append("WHERE f.flm_status = 1 ");

            // Add film filter
            if (selectedFilm != null && !selectedFilm.trim().isEmpty()) {
                queryBuilder.append("AND f.flm_judul = ? ");
            }

            // Add date filter
            if (dateFrom != null) {
                queryBuilder.append("AND CAST(tpt.tpt_tanggal AS DATE) >= ? ");
            }
            if (dateTo != null) {
                queryBuilder.append("AND CAST(tpt.tpt_tanggal AS DATE) <= ? ");
            }

            queryBuilder.append("GROUP BY f.flm_judul ")
                    .append("ORDER BY total_tiket DESC");

            PreparedStatement ps = db.conn.prepareStatement(queryBuilder.toString());

            int paramIndex = 1;
            if (selectedFilm != null && !selectedFilm.trim().isEmpty()) {
                ps.setString(paramIndex++, selectedFilm);
            }
            if (dateFrom != null) {
                ps.setDate(paramIndex++, java.sql.Date.valueOf(dateFrom));
            }
            if (dateTo != null) {
                ps.setDate(paramIndex++, java.sql.Date.valueOf(dateTo));
            }

            ResultSet rs = ps.executeQuery();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Jumlah Tiket Terjual");

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                String judulFilm = rs.getString("flm_judul");
                int totalTiket = rs.getInt("total_tiket");

                series.getData().add(new XYChart.Data<>(judulFilm, totalTiket));
            }

            if (!hasData) {
                labelStatus.setText("Tidak ada data untuk filter yang dipilih");
                // Tambahkan data dummy untuk menunjukkan bahwa tidak ada data
                series.getData().add(new XYChart.Data<>("Tidak ada data", 0));
            } else {
                labelStatus.setText("Data berhasil dimuat - Total film: " + series.getData().size());
            }

            barChartPenjualan.getData().add(series);

            rs.close();
            ps.close();

        } catch (SQLException e) {
            e.printStackTrace();
            labelStatus.setText("Error: Gagal memuat data chart - " + e.getMessage());
        }
    }

    // Method yang sudah ada - JANGAN DIHAPUS
    @FXML
    void onButtonLihatLaporan(ActionEvent event) {
        try {
            HashMap<String, Object> param = new HashMap<>();
            param.put("TanggalDari", java.sql.Date.valueOf(dateDari.getValue()));
            param.put("TanggalSampai", java.sql.Date.valueOf(dateSampai.getValue()));

            JasperPrint jp = JasperFillManager.fillReport(
                    getClass().getResourceAsStream("/Report/ReportTransaksiTiket.jasper"),
                    param,
                    db.conn
            );

            JasperViewer viewer = new JasperViewer(jp, false);
            viewer.setExtendedState(JFrame.MAXIMIZED_BOTH); // Tambahkan ini untuk fullscreen
            viewer.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}