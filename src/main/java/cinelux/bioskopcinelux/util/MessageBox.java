package cinelux.bioskopcinelux.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.concurrent.atomic.AtomicBoolean;

public class MessageBox {
    public void alertInfo(String message){
        Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
        infoAlert.setTitle("Information");
        infoAlert.setHeaderText(null);
        infoAlert.setContentText(message);
        infoAlert.showAndWait();
    }
    public void alertWarning(String message){
        Alert warningAlert = new Alert(Alert.AlertType.WARNING);
        warningAlert.setTitle("Warning");
        warningAlert.setHeaderText(null);
        warningAlert.setContentText(message);
        warningAlert.showAndWait();
    }

    public void alertError(String message){
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Error");
        errorAlert.setHeaderText(null);
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
    }
    public boolean alertConfirm(String message){
        AtomicBoolean result = new AtomicBoolean(false);
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText(message);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                result.set(true);
            } else {
                result.set(false);
            }
        });
        return result.get();
    }
}
