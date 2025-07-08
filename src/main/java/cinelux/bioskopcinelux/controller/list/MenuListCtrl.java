package cinelux.bioskopcinelux.controller.list;

import cinelux.bioskopcinelux.controller.master.MenuCtrl;
import cinelux.bioskopcinelux.model.Menu;
import cinelux.bioskopcinelux.util.MessageBox;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;

public class MenuListCtrl {
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
    private Label lbHarga;
    @FXML
    private Label lbKategori;
    @FXML
    private Label lbNama;
    @FXML
    private Label lbStok;
    @FXML
    private Tooltip toolTipDelete;
    private MenuCtrl menuCtrl;
    private Menu menu;


    public void setMenusController(MenuCtrl controller) {
        this.menuCtrl = controller;
    }

    public void setData(Menu menu) {
        this.menu = menu;
        lbNama.setText(menu.getNama());
        lbHarga.setText("Rp " + String.format("%,.2f", menu.getHarga()));
        lbKategori.setText(menu.getJenis_makanan().getNama());
        lbStok.setText(String.valueOf(menu.getStok()));


        int status = menu.getStatus();

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
        if (menu != null && menuCtrl != null) {
            menuCtrl.toogleStatusMenu(menu.getId());
        }
    }

    @FXML
    void btnUpdateClick(ActionEvent event) {
        if (menu.getStatus() == 0) {
            MessageBox msg = new MessageBox();
            msg.alertWarning("Tidak bisa mengedit data yang tidak aktif. Silakan aktifkan kembali terlebih dahulu.");
            return;
        }

        if (menuCtrl != null && menu != null) {
            menuCtrl.loadDataToForm(menu.getId());
        }
    }

}
