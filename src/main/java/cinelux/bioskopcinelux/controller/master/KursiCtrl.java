package cinelux.bioskopcinelux.controller.master;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import cinelux.bioskopcinelux.connection.DBConnect;

public class KursiCtrl {

    @FXML
    private GridPane gridStudioContainer;

    public void initialize() {
        tampilkanSemuaStudio();
    }

    private void tampilkanSemuaStudio() {
        try (Connection conn = DBConnect.getConnection()) {
            String sqlStudio = "SELECT std_id, std_nama, std_kapasitas FROM Studio WHERE std_status = 1 ORDER BY std_id";
            PreparedStatement stmtStudio = conn.prepareStatement(sqlStudio);
            ResultSet rsStudio = stmtStudio.executeQuery();

            int studioIndex = 0;

            while (rsStudio.next()) {
                int std_id = rsStudio.getInt("std_id");
                String nama = rsStudio.getString("std_nama");
                int kapasitas = rsStudio.getInt("std_kapasitas");

                Label label = new Label(nama + " (Kapasitas: " + kapasitas + ")");
                label.setFont(new Font("Arial", 16));
                label.setStyle("-fx-font-weight: bold;");

                GridPane gridKursi = new GridPane();
                gridKursi.setHgap(5);
                gridKursi.setVgap(5);

                tampilkanKursiStudio(std_id, gridKursi);

                VBox studioBox = new VBox(10);
                studioBox.setStyle("-fx-border-color: brown; -fx-border-radius: 10; -fx-border-width: 3; -fx-padding: 15; -fx-background-color: #ffffff; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 2, 2);");
                studioBox.getChildren().addAll(label, gridKursi);

                int row = studioIndex / 4;
                int col = studioIndex % 4;

                gridStudioContainer.add(studioBox, col, row);
                studioIndex++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tampilkanKursiStudio(int std_id, GridPane gridKursi) {
        try (Connection conn = DBConnect.getConnection()) {
            String sql = "SELECT krs_id, krs_nomor, krs_active FROM Kursi WHERE std_id = ? ORDER BY krs_baris, krs_kolom";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, std_id);
            ResultSet rs = stmt.executeQuery();

            Map<String, List<KursiData>> barisMap = new TreeMap<>();

            while (rs.next()) {
                String nomor = rs.getString("krs_nomor");
                int status = rs.getInt("krs_active");
                int id = rs.getInt("krs_id");

                String row = nomor.replaceAll("[0-9]", "");
                barisMap.putIfAbsent(row, new ArrayList<>());
                barisMap.get(row).add(new KursiData(nomor, status, id));
            }

            int rowIndex = 0;
            for (Map.Entry<String, List<KursiData>> entry : barisMap.entrySet()) {
                List<KursiData> kursiBaris = entry.getValue();
                kursiBaris.sort(Comparator.comparing(k -> Integer.parseInt(k.nomor.replaceAll("\\D", ""))));

                int totalKursi = kursiBaris.size();
                int tengah = totalKursi / 2;
                int colIndexVisual = 0;

                for (int i = 0; i < totalKursi; i++) {
                    if (i == tengah) colIndexVisual++;

                    KursiData data = kursiBaris.get(i);
                    Button btn = new Button(data.nomor);
                    btn.setPrefSize(50, 50);
                    btn.setFont(new Font(13));

                    if (data.status == 1) {
                        btn.setStyle("-fx-background-color: #90EE90;");
                    } else {
                        btn.setStyle("-fx-background-color: #D3D3D3;");
                    }

                    btn.setEffect(new DropShadow(2, Color.GRAY));

                    btn.setOnAction(e -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Info Kursi");
                        alert.setHeaderText(null);
                        alert.setContentText("Kursi: " + data.nomor + "\nStatus: " + (data.status == 1 ? "Tersedia" : "Tidak Aktif"));
                        alert.showAndWait();
                    });

                    gridKursi.add(btn, colIndexVisual, rowIndex);
                    colIndexVisual++;
                }

                rowIndex++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class KursiData {
        String nomor;
        int status;
        int id;

        public KursiData(String nomor, int status, int id) {
            this.nomor = nomor;
            this.status = status;
            this.id = id;
        }
    }
}
