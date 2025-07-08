package cinelux.bioskopcinelux.controller.list;

import cinelux.bioskopcinelux.controller.master.MenuCtrl;
import cinelux.bioskopcinelux.controller.master.PromoCtrl;
import cinelux.bioskopcinelux.model.Promo;
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

public class PromoListCtrl {
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnUpdate;
    @FXML
    private ImageView imgStatus;
    @FXML
    private Label lbKategori;
    @FXML
    private Label lbNama;
    @FXML
    private Label lbDiskon;
    @FXML
    private Tooltip toolTipDelete;

    private Promo promo;
    private PromoCtrl promosController;

    public void setPromoController(PromoCtrl controller) {
        this.promosController = controller;
    }

    public void setData(Promo promo) {
        this.promo = promo;
        lbNama.setText(promo.getNama_promo());
        lbDiskon.setText(String.format("%.0f %%", promo.getDiskon()));
        lbKategori.setText(promo.getTipe_promo());

        int status = promo.getStatus();

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
        int id = promo.getId();
        if(promosController != null) {
            promosController.toogleStatusPromo(id);
        }

    }

    @FXML
    void btnUpdateClick(ActionEvent event) {
        if (promo.getStatus() == 0) {
            MessageBox msg = new MessageBox();
            msg.alertWarning("Tidak bisa mengedit data yang tidak aktif. Silakan aktifkan kembali terlebih dahulu.");
            return;
        }

        if (promosController != null && promo != null) {
            promosController.loadDatatoForm(promo.getId());
        }
    }
}
