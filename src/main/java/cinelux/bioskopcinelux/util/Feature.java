package cinelux.bioskopcinelux.util;

import javafx.scene.control.TextField;

public class Feature {
    public static void limitTextLength(TextField textField, int maxLength) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > maxLength) {
                textField.setText(oldValue);
            }
        });
    }

    /**
     * Membatasi hanya huruf dan spasi saja, serta jumlah maksimal karakter.
     */
    public static void limitToLetters(TextField textField, int maxLength) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z\\s]*") || newValue.length() > maxLength) {
                textField.setText(oldValue);
            }
        });
    }

    /**
     * Membatasi hanya angka saja, serta jumlah maksimal karakter.
     */
    public static void limitToDigits(TextField textField, int maxLength) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*") || newValue.length() > maxLength) {
                textField.setText(oldValue);
            }
        });
    }
}