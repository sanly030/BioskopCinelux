package cinelux.bioskopcinelux.controller.report;

import cinelux.bioskopcinelux.connection.DBConnect;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

public class ReportTransaksiMenuCtrl implements Initializable {
    @FXML
    private Button btnLaporan;

    @FXML
    private DatePicker dateDari;

    @FXML
    private DatePicker dateSampai;

    @FXML
    private ComboBox<String> comboBoxMenu;

    @FXML
    private Button btnFilter;

    @FXML
    private Button btnClear;

    @FXML
    private Button btnExportJasper;

    @FXML
    private BarChart<String, Number> barChartPenjualanMenu;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private Label labelStatus;

    DBConnect db = new DBConnect();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadMenuToComboBox();
        loadInitialChartData();

        // Set default date range (last 30 days)
        dateSampai.setValue(LocalDate.now());
        dateDari.setValue(LocalDate.now().minusDays(30));
    }

    private void loadMenuToComboBox() {
        try {
            String query = "SELECT DISTINCT mnu_nama FROM Menu WHERE mnu_status = 1 ORDER BY mnu_nama";
            PreparedStatement stmt = db.conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            ObservableList<String> menuList = FXCollections.observableArrayList();
            menuList.add("Semua Menu"); // Option untuk menampilkan semua menu

            while (rs.next()) {
                menuList.add(rs.getString("mnu_nama"));
            }

            comboBoxMenu.setItems(menuList);
            comboBoxMenu.setValue("Semua Menu");

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            labelStatus.setText("Error: Gagal memuat data menu");
        }
    }

    private void loadInitialChartData() {
        // Load data dari 30 hari terakhir
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        loadChartData(startDate, endDate, null);
    }

    @FXML
    public void handleFilterData() {
        LocalDate startDate = dateDari.getValue();
        LocalDate endDate = dateSampai.getValue();
        String selectedMenu = comboBoxMenu.getValue();

        // Jika tidak ada tanggal yang dipilih, gunakan range default
        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(30);
            labelStatus.setText("Menggunakan range tanggal default (30 hari terakhir)");
        }

        if (startDate.isAfter(endDate)) {
            labelStatus.setText("Error: Tanggal mulai tidak boleh lebih besar dari tanggal selesai");
            return;
        }

        String menuFilter = null;
        if (selectedMenu != null && !selectedMenu.equals("Semua Menu")) {
            menuFilter = selectedMenu;
        }

        // Test query basic dulu
        testBasicQuery();

        loadChartData(startDate, endDate, menuFilter);
    }

    private void testBasicQuery() {
        try {
            String testQuery = "SELECT COUNT(*) as total FROM TransaksiPenjualanMenu";
            PreparedStatement stmt = db.conn.prepareStatement(testQuery);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int total = rs.getInt("total");
                System.out.println("Total transaksi: " + total);
            }

            rs.close();
            stmt.close();

            // Test detail query
            testQuery = "SELECT tpm.tpm_id, tpm.tpm_tanggal, tpm.tpm_total_harga, " +
                    "dpm.sub_total, m.mnu_nama " +
                    "FROM TransaksiPenjualanMenu tpm " +
                    "JOIN DetailPenjualanMenu dpm ON tpm.tpm_id = dpm.tpm_id " +
                    "JOIN Menu m ON dpm.mnu_id = m.mnu_id " +
                    "WHERE m.mnu_status = 1 " +
                    "ORDER BY tpm.tpm_tanggal DESC";

            stmt = db.conn.prepareStatement(testQuery);
            rs = stmt.executeQuery();

            int count = 0;
            while (rs.next() && count < 5) {
                System.out.println("Data " + (count + 1) + ": " +
                        rs.getString("mnu_nama") + " - " +
                        rs.getDouble("sub_total") + " - " +
                        rs.getDate("tpm_tanggal"));
                count++;
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error testing query: " + e.getMessage());
        }
    }

    @FXML
    public void handleClearFilter() {
        comboBoxMenu.setValue("Semua Menu");
        dateDari.setValue(null);
        dateSampai.setValue(null);
        loadInitialChartData();
        labelStatus.setText("Status: Filter telah dibersihkan");
    }

    @FXML
    public void handleExportJasper() {
        onButtonLihatLaporan();
    }

    private void loadChartData(LocalDate startDate, LocalDate endDate, String menuFilter) {
        try {
            StringBuilder query = new StringBuilder();

            // Coba query yang lebih sederhana dulu untuk debugging
            if (menuFilter != null && !menuFilter.equals("Semua Menu")) {
                // Query untuk menu spesifik
                query.append("SELECT m.mnu_nama AS kategori_menu, ");
                query.append("SUM(dpm.sub_total) AS total_pendapatan ");
                query.append("FROM TransaksiPenjualanMenu tpm ");
                query.append("JOIN DetailPenjualanMenu dpm ON tpm.tpm_id = dpm.tpm_id ");
                query.append("JOIN Menu m ON dpm.mnu_id = m.mnu_id ");
                query.append("WHERE m.mnu_status = 1 ");
                query.append("AND m.mnu_nama = ? ");
            } else {
                // Query untuk semua menu berdasarkan kategori
                query.append("SELECT COALESCE(ds.tst_nama, m.mnu_nama) AS kategori_menu, ");
                query.append("SUM(dpm.sub_total) AS total_pendapatan ");
                query.append("FROM TransaksiPenjualanMenu tpm ");
                query.append("JOIN DetailPenjualanMenu dpm ON tpm.tpm_id = dpm.tpm_id ");
                query.append("JOIN Menu m ON dpm.mnu_id = m.mnu_id ");
                query.append("LEFT JOIN dtSetting ds ON m.tst_id = ds.tst_id AND ds.tst_kategori = 'KATEGORI_MENU' ");
                query.append("WHERE m.mnu_status = 1 ");
            }

            int parameterIndex = 1;

            if (menuFilter != null && !menuFilter.equals("Semua Menu")) {
                parameterIndex++;
            }

            if (startDate != null) {
                query.append("AND CONVERT(DATE, tpm.tpm_tanggal) >= ? ");
            }

            if (endDate != null) {
                query.append("AND CONVERT(DATE, tpm.tpm_tanggal) <= ? ");
            }

            if (menuFilter != null && !menuFilter.equals("Semua Menu")) {
                query.append("GROUP BY m.mnu_nama ");
            } else {
                query.append("GROUP BY COALESCE(ds.tst_nama, m.mnu_nama) ");
            }
            query.append("ORDER BY total_pendapatan DESC");

            PreparedStatement stmt = db.conn.prepareStatement(query.toString());

            int paramIndex = 1;

            if (menuFilter != null && !menuFilter.equals("Semua Menu")) {
                stmt.setString(paramIndex++, menuFilter);
            }

            if (startDate != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(startDate));
            }

            if (endDate != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(endDate));
            }

            System.out.println("Executing query: " + query.toString()); // Debug

            ResultSet rs = stmt.executeQuery();

            // Clear existing data
            barChartPenjualanMenu.getData().clear();

            // Create new series
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Pendapatan");

            boolean hasData = false;
            double totalPendapatan = 0;

            while (rs.next()) {
                String kategori = rs.getString("kategori_menu");
                double pendapatan = rs.getDouble("total_pendapatan");

                System.out.println("Data found: " + kategori + " = " + pendapatan); // Debug

                series.getData().add(new XYChart.Data<>(kategori, pendapatan));
                totalPendapatan += pendapatan;
                hasData = true;
            }

            System.out.println("Total data found: " + series.getData().size()); // Debug

            if (hasData) {
                barChartPenjualanMenu.getData().add(series);
                labelStatus.setText(String.format("Status: Data berhasil dimuat. Total Pendapatan: Rp %,.2f", totalPendapatan));
            } else {
                labelStatus.setText("Status: Tidak ada data yang sesuai dengan filter");
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            labelStatus.setText("Error: Gagal memuat data chart - " + e.getMessage());
        }
    }

    @FXML
    public void onButtonLihatLaporan() {
        try {
            HashMap<String, Object> param = new HashMap<>();

            if (dateDari.getValue() != null && dateSampai.getValue() != null) {
                param.put("TanggalDari", java.sql.Date.valueOf(dateDari.getValue()));
                param.put("TanggalSampai", java.sql.Date.valueOf(dateSampai.getValue()));
            } else {
                // Default date range if not selected
                param.put("TanggalDari", java.sql.Date.valueOf(LocalDate.now().minusDays(30)));
                param.put("TanggalSampai", java.sql.Date.valueOf(LocalDate.now()));
            }

            JasperPrint jp = JasperFillManager.fillReport(
                    getClass().getResourceAsStream("/Report/ReportTransaksiMenu.jasper"),
                    param,
                    db.conn
            );

            JasperViewer viewer = new JasperViewer(jp, false);
            viewer.setExtendedState(JFrame.MAXIMIZED_BOTH);
            viewer.setVisible(true);

            labelStatus.setText("Status: Laporan berhasil dibuka");

        } catch (Exception e) {
            e.printStackTrace();
            labelStatus.setText("Error: Gagal membuka laporan - " + e.getMessage());
        }
    }
}