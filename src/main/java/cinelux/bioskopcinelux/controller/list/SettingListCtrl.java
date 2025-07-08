package cinelux.bioskopcinelux.controller.list;

import cinelux.bioskopcinelux.controller.master.SettingCtrl;
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

public class SettingListCtrl {

    @FXML
    private Label lbNama;

    @FXML
    private Label lbValue;

    @FXML
    private Label lbKategori;

    @FXML
    private Button btnUpdate;

    @FXML
    private Button btnDelete;

    @FXML
    private ImageView imgStatus;

    @FXML
    private Tooltip toolTipDelete;

    private SettingCtrl settingsController;
    private Setting setting;

    public void setSettingsController(SettingCtrl controller) {
        this.settingsController = controller;
    }

    public void setData(Setting setting) {
        this.setting = setting;
        lbNama.setText(setting.getNama());
        lbValue.setText(setting.getValue());
        lbKategori.setText(setting.getKategori());

        int status = setting.getStatus();

        String imgPath;
        if ( status == 1) {
            imgPath = "/cinelux/bioskopcinelux/asset/image/icon_remove.png";
            toolTipDelete.setText("Hapus");
        }else {
            imgPath = "/cinelux/bioskopcinelux/asset/image/icon_update.png";
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
        int id = setting.getId();
        if (settingsController != null) {
            settingsController.toogleStatusSetting(id);
        }
    }

    @FXML
    void btnUpdateClick(ActionEvent event) {
        if (setting.getStatus() == 0) {
            MessageBox msg = new MessageBox();
            msg.alertWarning("Tidak bisa mengedit data yang tidak aktif. Silakan aktifkan kembali terlebih dahulu.");
            return;
        }

        if (settingsController != null && setting != null) {
            settingsController.loadDatatoForm(setting.getId());
        }
    }
}
