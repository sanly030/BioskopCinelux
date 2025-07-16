// 1. UTILITY CLASS UNTUK RUPIAH FORMATTER
package cinelux.bioskopcinelux.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class RupiahFormatter {
    private static final DecimalFormat formatter;
    private static final NumberFormat numberFormat;

    static {
        // Buat formatter dengan locale Indonesia untuk separator
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        formatter = new DecimalFormat("#,##0", symbols);
        numberFormat = NumberFormat.getNumberInstance(new Locale("id", "ID"));
    }

    /**
     * Format angka ke format rupiah dengan separator (contoh: 150000 -> 150.000)
     */
    public static String formatToRupiah(double amount) {
        return "Rp " + formatter.format(amount);
    }

    /**
     * Format angka ke format dengan separator tanpa Rp (contoh: 150000 -> 150.000)
     */
    public static String formatWithSeparator(double amount) {
        return formatter.format(amount);
    }

    /**
     * Parse string rupiah kembali ke angka murni (contoh: "150.000" -> 150000)
     */
    public static double parseRupiahToNumber(String rupiahText) throws ParseException {
        if (rupiahText == null || rupiahText.trim().isEmpty()) {
            return 0.0;
        }

        // Hapus "Rp" dan spasi
        String cleanText = rupiahText.replace("Rp", "").trim();

        // Parse dengan NumberFormat Indonesia
        Number number = numberFormat.parse(cleanText);
        return number.doubleValue();
    }

    /**
     * Validasi apakah string adalah format angka yang valid
     */
    public static boolean isValidNumber(String text) {
        try {
            parseRupiahToNumber(text);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}