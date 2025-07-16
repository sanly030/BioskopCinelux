package cinelux.bioskopcinelux.controller.report;

import cinelux.bioskopcinelux.connection.DBConnect;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

import javax.swing.*;
import java.util.HashMap;

public class ReportTransaksiTiketCtrl {

    @FXML
    private Button btnLaporan;

    @FXML
    private DatePicker dateDari;

    @FXML
    private DatePicker dateSampai;

    DBConnect db = new DBConnect();
    @FXML
    void onButtonLihatLaporan(ActionEvent event) {
        try {
            HashMap<String, Object> param = new HashMap<>();
            param.put("TanggalDari", java.sql.Date.valueOf(dateDari.getValue()));
            param.put("TanggalSampai", java.sql.Date.valueOf(dateSampai.getValue()));

            JasperPrint jp = JasperFillManager.fillReport(
                    getClass().getResourceAsStream("/Report/ReportTransaksiTiket.jasper"),
                    param,
                    db.conn
            );

            JasperViewer viewer = new JasperViewer(jp, false);
            viewer.setExtendedState(JFrame.MAXIMIZED_BOTH); // Tambahkan ini untuk fullscreen
            viewer.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
