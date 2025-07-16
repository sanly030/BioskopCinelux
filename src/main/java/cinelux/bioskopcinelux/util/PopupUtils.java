package cinelux.bioskopcinelux.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class PopupUtils {

    public static void showPemilihanKursiPopup(cinelux.bioskopcinelux.model.JadwalTayang jadwalTayang) {
        try {
            FXMLLoader loader = new FXMLLoader(PopupUtils.class.getResource("/fxml/popup/PemilihanKursi.fxml"));
            Parent root = loader.load();

            cinelux.bioskopcinelux.controller.master.PemilihanKursiCtrl controller = loader.getController();

            Stage popupStage = new Stage();
            popupStage.setTitle("Pilih Kursi - " + jadwalTayang.getNamaFilm().getJudul());
            popupStage.setScene(new Scene(root));
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setResizable(false);

            controller.setData(jadwalTayang, popupStage);

            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}