package cinelux.bioskopcinelux.controller.master;

import cinelux.bioskopcinelux.connection.DBConnect;
import cinelux.bioskopcinelux.controller.transaksi.PenjualanTiketCtrl;
import cinelux.bioskopcinelux.model.JadwalTayang;
import cinelux.bioskopcinelux.model.Kursi;
import cinelux.bioskopcinelux.service.impl.PenjualanTiketImpl;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class PemilihanKursiCtrl {

    @FXML private Label lblJudulFilm;
    @FXML private Label lblStudioInfo;
    @FXML private Label lblJamTayang;
    @FXML private Label lblHargaTiket;
    @FXML private GridPane gridKursi;
    @FXML private Label lblKursiDipilih;
    @FXML private Label lblTotalHarga;
    @FXML private Button btnKonfirmasi;
    @FXML private Button btnBatal;

    private JadwalTayang jadwalTayang;
    private Set<Kursi> kursiDipilih = new HashSet<>();
    private Map<String, Button> kursiButtonMap = new HashMap<>();
    private Map<String, Kursi> nomorToKursiMap = new HashMap<>();
    private Stage stage;
    private PenjualanTiketCtrl mainController;

    private final PenjualanTiketImpl penjualanTiketImpl = new PenjualanTiketImpl();

    public void initialize() {
        handleKonfirmasi();
    }

    public void setData(JadwalTayang jadwalTayang, Stage stage) {
        this.jadwalTayang = jadwalTayang;
        this.stage = stage;

        lblJudulFilm.setText(jadwalTayang.getNamaFilm().getJudul());
        lblStudioInfo.setText("Studio: " + jadwalTayang.getNamaStudio().getNama());

        String jamMulai = jadwalTayang.getJamMulai().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        String jamSelesai = jadwalTayang.getJamSelesai().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        lblJamTayang.setText("Jam: " + jamMulai + " - " + jamSelesai);

        lblHargaTiket.setText(String.format("Harga per tiket: Rp %,d", jadwalTayang.getHarga().intValue()));

        tampilkanKursiStudio();
        updateTampilan();
    }

    public void setMainController(PenjualanTiketCtrl mainController) {
        this.mainController = mainController;
    }

    public List<Kursi> getKursiDipilih() {
        return new ArrayList<>(kursiDipilih);
    }

    private void tampilkanKursiStudio() {
        try (Connection conn = DBConnect.getConnection()) {
            int std_id = jadwalTayang.getNamaStudio().getId();

            // Ambil semua kursi studio
            String sql = "SELECT krs_id, krs_nomor, krs_baris, krs_kolom, krs_active FROM Kursi WHERE std_id = ? ORDER BY krs_baris, krs_kolom";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, std_id);
            ResultSet rs = stmt.executeQuery();

            Map<String, List<Kursi>> barisMap = new TreeMap<>();

            while (rs.next()) {
                if (rs.getInt("krs_active") != 1) continue;

                Kursi kursi = new Kursi();
                kursi.setId(rs.getInt("krs_id"));
                kursi.setNomor(rs.getString("krs_nomor"));
                kursi.setBaris(rs.getString("krs_baris"));
                kursi.setKolom(rs.getInt("krs_kolom"));
                kursi.setActive(true);

                nomorToKursiMap.put(kursi.getNomor(), kursi);
                barisMap.computeIfAbsent(kursi.getBaris(), k -> new ArrayList<>()).add(kursi);
            }

            // Ambil kursi yang sudah dipesan dari transaksi lain
            List<Integer> kursiTerpakai = penjualanTiketImpl.getKursiYangSudahDipesan(jadwalTayang.getId());

            int rowIndex = 0;
            for (Map.Entry<String, List<Kursi>> entry : barisMap.entrySet()) {
                List<Kursi> kursiBaris = entry.getValue();
                kursiBaris.sort(Comparator.naturalOrder());

                int totalKursi = kursiBaris.size();
                int tengah = totalKursi / 2;
                int colIndexVisual = 0;

                for (int i = 0; i < totalKursi; i++) {
                    if (i == tengah) colIndexVisual++;

                    Kursi kursi = kursiBaris.get(i);
                    Button btn = new Button(kursi.getNomor());
                    btn.setPrefSize(50, 50);
                    btn.setFont(new Font(13));
                    btn.setEffect(new DropShadow(2, Color.GRAY));

                    if (kursiTerpakai.contains(kursi.getId())) {
                        btn.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                        btn.setDisable(true);
                    } else {
                        btn.setStyle("-fx-background-color: #90EE90;");
                        btn.setOnAction(e -> toggleKursi(kursi, btn));
                    }

                    gridKursi.add(btn, colIndexVisual, rowIndex);
                    kursiButtonMap.put(kursi.getNomor(), btn);
                    colIndexVisual++;
                }
                rowIndex++;
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal memuat layout kursi: " + e.getMessage());
        }
    }

    private void toggleKursi(Kursi kursi, Button btn) {
        if (kursiDipilih.contains(kursi)) {
            kursiDipilih.remove(kursi);
            btn.setStyle("-fx-background-color: #90EE90;");
        } else {
            kursiDipilih.add(kursi);
            btn.setStyle("-fx-background-color: #FFD700;");
        }
        updateTampilan();
    }

    private void updateTampilan() {
        if (kursiDipilih.isEmpty()) {
            lblKursiDipilih.setText("Kursi dipilih: -");
            lblTotalHarga.setText("Total: Rp 0");
            btnKonfirmasi.setDisable(true);
        } else {
            List<Kursi> kursiList = new ArrayList<>(kursiDipilih);
            kursiList.sort(Kursi::compareTo);
            String kursiStr = kursiList.stream().map(Kursi::getNomor).collect(Collectors.joining(", "));

            lblKursiDipilih.setText("Kursi dipilih: " + kursiStr);
            int total = kursiDipilih.size() * jadwalTayang.getHarga().intValue();
            lblTotalHarga.setText(String.format("Total: Rp %,d", total));
            btnKonfirmasi.setDisable(false);
        }
    }

    @FXML
    private void handleKonfirmasi() {
        try {
            if (kursiDipilih.isEmpty()) {
                showAlert("Peringatan", "Silakan pilih kursi terlebih dahulu!");
                return;
            }

            if (mainController != null) {
                mainController.addTicketToCart(jadwalTayang, kursiDipilih.size(), new ArrayList<>(kursiDipilih));
                showAlert("Sukses", "Tiket berhasil ditambahkan ke keranjang!");
                stage.close();
            } else {
                showAlert("Error", "Main controller tidak ditemukan.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal menambahkan tiket ke keranjang: " + e.getMessage());
        }
    }

    @FXML
    private void handleBatal() {
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
