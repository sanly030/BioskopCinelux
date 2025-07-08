package cinelux.bioskopcinelux.controller;
import javafx.scene.control.Alert;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class controller {


    //Formater Uang
    public NumberFormat displayCurrencyFormatter = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
    {
        displayCurrencyFormatter.setMinimumFractionDigits(2);
        displayCurrencyFormatter.setMaximumFractionDigits(2);
    }

    //Formater Uang
    public String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return displayCurrencyFormatter.format(value);
    }

    public NumberFormat decimalFormater = NumberFormat.getNumberInstance(Locale.US); // <-- PERUBAHAN UTAMA
    {
        decimalFormater.setMinimumFractionDigits(2);
        decimalFormater.setMaximumFractionDigits(2);
        decimalFormater.setGroupingUsed(false);
    }

    public String formatDecimal(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return decimalFormater.format(value);
    }

    public void alertWarning(String message) {
        Alert warningAlert = new Alert(Alert.AlertType.WARNING);
        warningAlert.setTitle("Warning");
        warningAlert.setHeaderText(null);
        warningAlert.setContentText(message);
        warningAlert.showAndWait();
    }
}
