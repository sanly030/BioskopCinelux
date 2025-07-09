package cinelux.bioskopcinelux.controller.list;

import cinelux.bioskopcinelux.controller.master.StudioCtrl;
import cinelux.bioskopcinelux.model.Studio;
import cinelux.bioskopcinelux.model.Role;
import cinelux.bioskopcinelux.util.MessageBox;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;

public class StudioListCtrl {
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
    private Label lbBaris;
    @FXML
    private Label lbKolom;
    @FXML
    private Label lbKapasitas;
    @FXML
    private Label lbNama;
    @FXML
    private Tooltip toolTipDelete;

    private Studio studio;
    private StudioCtrl studioCtrl;
    Role user = new Role();

    public void setStudioController(StudioCtrl controller) {
        this.studioCtrl = controller;
    }

    public void setStudio(Studio studio) {
        this.studio = studio;
        lbNama.setText(studio.getNama());
        lbKapasitas.setText(String.valueOf(studio.getKapasitas()));
        lbBaris.setText(String.valueOf(studio.getBaris()));
        lbKolom.setText(String.valueOf(studio.getKolom()));

        int status = studio.getStatus();
        String imgPath;

        if (status == 1) {
            imgPath = "/cinelux/bioskopcinelux/asset/image/icon_remove.png";
            toolTipDelete.setText("Hapus");
        } else {
            imgPath = "/cinelux/bioskopcinelux/asset/image/icon_update.png";
            toolTipDelete.setText("Aktifkan");
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
        int id = studio.getId();
        String modifiedBy = (user.getName() != null) ? user.getName() : "Admin";

        if (studioCtrl != null) {
            studioCtrl.toogleStatusStudio(id, modifiedBy); // Toggle status aktif/nonaktif
        }
    }

    @FXML
    void btnUpdateClick(ActionEvent event) {
        if (studio.getStatus() == 0) {
            MessageBox msg = new MessageBox();
            msg.alertWarning("Studio tidak aktif. Aktifkan terlebih dahulu sebelum mengedit.");
            return;
        }

        if (studioCtrl != null && studio != null) {
            studioCtrl.loadDatatoForm(studio.getId()); // Muat data studio ke form edit
        }
    }
}
