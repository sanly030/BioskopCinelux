package cinelux.bioskopcinelux.util;

import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

public class SwapPage {
    public void openNewWindow(String fxmlFile, String windowTitle, Node currentNode) {
        try {

            URL fxmlUrl = getClass().getResource("/cinelux/bioskopcinelux/view/" + fxmlFile);
            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Kesalahan UI", "File FXML tidak ditemukan: " + fxmlFile);
                return;
            }

            Parent root = FXMLLoader.load(fxmlUrl);

            Stage newStage = new Stage();
            newStage.initStyle(StageStyle.UNDECORATED);
            newStage.setTitle(windowTitle);
            newStage.setScene(new Scene(root));
            newStage.show();

            PauseTransition pause = new PauseTransition(Duration.seconds(1));

            pause.setOnFinished(event -> {
                Stage oldStage = (Stage) currentNode.getScene().getWindow();
                oldStage.close();
            });

            pause.play();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Kesalahan I/O", "Gagal memuat halaman baru.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
