package cinelux.bioskopcinelux.controller.list;

import cinelux.bioskopcinelux.controller.master.JadwalTayangCtrl;
import cinelux.bioskopcinelux.model.JadwalTayang;
import cinelux.bioskopcinelux.util.MessageBox; // Added import
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class JadwalTayangListCtrl {
    @FXML
    private Tooltip Information;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnInformation;
    @FXML
    private Button btnUpdate;
    @FXML
    private ImageView imgStatus;
    @FXML
    private Label lbFilm;
    @FXML
    private Label lbHari;
    @FXML
    private Label lbJamMulai;
    @FXML
    private Label lbJamSelesai;
    @FXML
    private Label lbStudio;
    @FXML
    private Tooltip toolTipDelete;

    private JadwalTayangCtrl jadwalCtrl;
    private JadwalTayang jadwal;

    public void setJadwalController(JadwalTayangCtrl controller) {
        this.jadwalCtrl = controller;
    }

    public void setData(JadwalTayang jadwal) {
        this.jadwal = jadwal;

        // Check for null values before accessing properties
        if (jadwal.getNamaFilm() != null) {
            lbFilm.setText(jadwal.getNamaFilm().getJudul());
        } else {
            lbFilm.setText("Film tidak tersedia");
        }

        if (jadwal.getNamaStudio() != null) {
            lbStudio.setText(jadwal.getNamaStudio().getNama());
        } else {
            lbStudio.setText("Tidak tersedia");
        }

        lbHari.setText((jadwal.getJenisHari()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalTime jamMulai = jadwal.getJamMulai().toLocalTime();
        LocalTime jamSelesai = jadwal.getJamSelesai().toLocalTime();

        lbJamMulai.setText(jamMulai.format(formatter));
        lbJamSelesai.setText(jamSelesai.format(formatter));


        int status = jadwal.getStatus();
        String imgPath;
        if (status == 1) {
            imgPath = "/cinelux/bioskopcinelux/asset/image/icon_remove.png";
            toolTipDelete.setText("Hapus");
        } else {
            imgPath = "/cinelux/bioskopcinelux/asset/image/icon_update.png";
            toolTipDelete.setText("Aktifkan kembali");
        }
        setImage(imgPath);
    }


    private void setImage(String imagePath) {
        try (InputStream imageStream = getClass().getResourceAsStream(imagePath)) {
            if (imageStream != null) {
                Image image = new Image(imageStream);
                imgStatus.setImage(image);
            } else {
                System.err.println("Gagal memuat gambar: " + imagePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnDeleteClick(ActionEvent event) {
        if (jadwal != null && jadwalCtrl != null) {
            jadwalCtrl.toogleStatusJadwal(jadwal.getId());
        }
    }

    @FXML
    void btnUpdateClick(ActionEvent event) {
        if (jadwal == null) {
            MessageBox msg = new MessageBox();
            msg.alertWarning("Data jadwal tidak tersedia.");
            return;
        }

        if (jadwal.getStatus() == 0) {
            MessageBox msg = new MessageBox();
            msg.alertWarning("Tidak bisa mengedit jadwal yang tidak aktif. Silakan aktifkan kembali terlebih dahulu.");
            return;
        }

        if (jadwalCtrl != null && jadwal != null) {
            jadwalCtrl.loadDataToForm(jadwal.getId());
        }
    }
}
