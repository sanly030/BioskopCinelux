package cinelux.bioskopcinelux.controller.list;

import cinelux.bioskopcinelux.controller.transaksi.PenjualanTiketCtrl;
import cinelux.bioskopcinelux.model.JadwalTayang;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

import cinelux.bioskopcinelux.controller.master.PemilihanKursiCtrl;

public class JadwalCardCtrl {

    @FXML
    private Label lblJudul;

    @FXML
    private Label lblStudio;

    @FXML
    private Label lblHari;

    @FXML
    private Label lblJam;

    @FXML
    private Label lblHarga;

    @FXML
    private Label lblStatus;

    @FXML
    private Button btnBeli;

    @FXML
    private ImageView imgPoster;

    private JadwalTayang jadwalTayang;
    private Runnable onBeliHandler;

    private PenjualanTiketCtrl mainController;

    public void setMainController(PenjualanTiketCtrl controller) {
        this.mainController = controller;
    }

    public void setData(JadwalTayang jadwalTayang, Runnable onBeliHandler) {
        this.jadwalTayang = jadwalTayang;
        this.onBeliHandler = onBeliHandler;

        lblJudul.setText(jadwalTayang.getNamaFilm().getJudul());
        lblStudio.setText(jadwalTayang.getNamaStudio().getNama());
//        lblHari.setText(jadwalTayang.getJenisHari());

        String jamMulai = jadwalTayang.getJamMulai().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        String jamSelesai = jadwalTayang.getJamSelesai().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        lblJam.setText(jamMulai + " - " + jamSelesai);

        lblHarga.setText(String.format("Rp %,d", jadwalTayang.getHarga().intValue()));
//        lblStatus.setText("Status: " + jadwalTayang.getStatus());

        // Load gambar poster film
        String posterPath = "src/main/resources/images/" + jadwalTayang.getNamaFilm().getPoster();
        File file = new File(posterPath);
        if (file.exists()) {
            imgPoster.setImage(new Image(file.toURI().toString()));
        }

        // Set action untuk tombol beli
        btnBeli.setOnAction(event -> {
            if (mainController != null && jadwalTayang != null) {
                mainController.handleTiketSelection(jadwalTayang);
            }
        });

    }

//    private void handleBeliTiket() {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cinelux/bioskopcinelux/view/DashboardKasir/PemilihanKursi.fxml"));
//            Parent root = loader.load();
//
//            PemilihanKursiCtrl controller = loader.getController();
//
//            Stage popupStage = new Stage();
//            popupStage.setTitle("Pilih Kursi - " + jadwalTayang.getNamaFilm().getJudul());
//            popupStage.setScene(new Scene(root));
//            popupStage.initModality(Modality.APPLICATION_MODAL);
//            popupStage.setResizable(false);
//
//            // ✅ Set mainController agar btnKonfirmasi tahu harus ke mana
//            controller.setMainController(mainController); // ⬅️ penting!
//            controller.setData(jadwalTayang, popupStage);
//
//            popupStage.showAndWait();
//
//            if (onBeliHandler != null) {
//                onBeliHandler.run();
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            showAlert("Error", "Gagal memuat form pemilihan kursi: " + e.getMessage());
//        }
//    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public JadwalTayang getJadwalTayang() {
        return jadwalTayang;
    }
}