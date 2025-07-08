package cinelux.bioskopcinelux.controller.list;

import cinelux.bioskopcinelux.controller.master.KaryawanCtrl;
import cinelux.bioskopcinelux.controller.master.SettingCtrl;
import cinelux.bioskopcinelux.model.Karyawan;
import cinelux.bioskopcinelux.model.Setting;
import cinelux.bioskopcinelux.util.MessageBox;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;

public class KaryawanListCtrl {
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
    private Label lbAlamat;
    @FXML
    private Label lbNama;
    @FXML
    private Label lbNoTelp;
    @FXML
    private Label lbRole;
    @FXML
    private Tooltip toolTipDelete;

    private KaryawanCtrl karyawanController;
    private Karyawan karyawan;

    public void setKaryawansController(KaryawanCtrl controller) {
        this.karyawanController = controller;
    }

    public void setData(Karyawan karyawan) {
        this.karyawan = karyawan;

        // ✅ Set label dengan data karyawan
        lbNama.setText(karyawan.getNama());
        lbNoTelp.setText(karyawan.getNoTelp());
        lbAlamat.setText(karyawan.getAlamat());

        // ✅ Cek apakah role null
        if (karyawan.getRole() != null) {
            lbRole.setText(karyawan.getRole().toString());
        } else {
            lbRole.setText("Role Tidak Ditemukan");
        }

        // ✅ Set ikon status aktif/nonaktif
        int status = karyawan.getStatus();
        String imgPath = (status == 1)
                ? "/cinelux/bioskopcinelux/asset/image/icon_remove.png"
                : "/cinelux/bioskopcinelux/asset/image/icon_update.png";
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
        int id = karyawan.getId();
        if (karyawanController != null) {
            karyawanController.toogleStatusKaryawan(id);
        }
    }

    @FXML
    void btnUpdateClick(ActionEvent event) {
        if (karyawan.getStatus() == 0) {
            MessageBox msg = new MessageBox();
            msg.alertWarning("Tidak bisa mengedit data yang tidak aktif. Silakan aktifkan kembali terlebih dahulu.");
            return;
        }

        if (karyawan != null && karyawanController != null) {
            karyawanController.loadDatatoForm(karyawan.getId());
        }
    }


}
