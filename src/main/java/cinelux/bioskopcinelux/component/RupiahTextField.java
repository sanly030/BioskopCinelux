// 2. CUSTOM TEXTFIELD UNTUK RUPIAH INPUT
package cinelux.bioskopcinelux.component;

import cinelux.bioskopcinelux.util.RupiahFormatter;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.DoubleStringConverter;

import java.text.ParseException;
import java.util.function.UnaryOperator;

public class RupiahTextField extends TextField {
    private double currentValue = 0.0;

    public RupiahTextField() {
        super();
        setupFormatter();
    }

    public RupiahTextField(double initialValue) {
        super();
        this.currentValue = initialValue;
        setupFormatter();
        updateDisplay();
    }

    private void setupFormatter() {
        // Filter untuk hanya menerima angka dan separator
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();

            // Hapus "Rp " untuk validasi
            String cleanText = newText.replace("Rp ", "").trim();

            // Izinkan string kosong
            if (cleanText.isEmpty()) {
                return change;
            }

            // Validasi format angka dengan separator
            if (cleanText.matches("\\d{1,3}(\\.\\d{3})*")) {
                return change;
            }

            return null; // Tolak perubahan
        };

        // Set text formatter dengan filter
        this.setTextFormatter(new TextFormatter<>(filter));

        // Event handler untuk format otomatis saat kehilangan fokus
        this.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                formatCurrentText();
            }
        });
    }

    private void formatCurrentText() {
        try {
            String text = this.getText();
            if (text != null && !text.trim().isEmpty()) {
                double value = RupiahFormatter.parseRupiahToNumber(text);
                this.currentValue = value;
                updateDisplay();
            }
        } catch (ParseException e) {
            // Jika parsing gagal, kembalikan ke nilai sebelumnya
            updateDisplay();
        }
    }

    private void updateDisplay() {
        String formatted = RupiahFormatter.formatToRupiah(currentValue);
        this.setText(formatted);
    }

    /**
     * Set nilai dalam bentuk angka murni
     */
    public void setValue(double value) {
        this.currentValue = value;
        updateDisplay();
    }

    /**
     * Dapatkan nilai dalam bentuk angka murni (tanpa separator)
     */
    public double getValue() {
        try {
            return RupiahFormatter.parseRupiahToNumber(this.getText());
        } catch (ParseException e) {
            return currentValue;
        }
    }

    /**
     * Dapatkan nilai dalam bentuk angka murni sebagai String
     */
    public String getPlainValue() {
        return String.valueOf(getValue());
    }
}