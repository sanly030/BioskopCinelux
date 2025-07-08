package cinelux.bioskopcinelux.controller.menu;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;

public class MenuAdminCtrl {
    @FXML
    private AnchorPane panelMaster;

    @FXML
    private HBox btnSetting;

    @FXML
    private HBox btnPegawai;

    @FXML
    protected void btnSettingOnAction() {
        loadMaster("/cinelux/bioskopcinelux/view/DashboardAdmin/Setting.fxml");
    }

    @FXML
    protected void btnPegawaiOnAction() {
        loadMaster("/cinelux/bioskopcinelux/view/DashboardAdmin/Karyawan.fxml");
    }

    @FXML
    protected void btnMenuOnAction() {
        loadMaster("/cinelux/bioskopcinelux/view/DashboardAdmin/Menu.fxml");
    }

    @FXML
    protected void btnPromoOnAction() {loadMaster("/cinelux/bioskopcinelux/view/DashboardAdmin/Promo.fxml");}


    private void loadMaster(String fxmlPage) {
        try {
            // Pastikan path fxml diawali dengan slash "/"
            URL fxmlUrl = getClass().getResource(fxmlPage);

            if (fxmlUrl == null) {
                System.err.println("❌ Tidak dapat menemukan file FXML: " + fxmlPage);
                return;
            }

            Parent root = FXMLLoader.load(fxmlUrl);

            if (root == null) {
                System.err.println("❌ Root node dari FXML adalah null: " + fxmlPage);
                return;
            }

            panelMaster.getChildren().setAll(root);

        } catch (IOException e) {
            System.err.println("❌ Gagal memuat halaman FXML: " + fxmlPage);
            e.printStackTrace();
        }
    }
}
