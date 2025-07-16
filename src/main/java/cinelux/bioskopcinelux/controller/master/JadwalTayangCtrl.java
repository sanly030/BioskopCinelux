// File: JadwalTayangCtrl.java
// Note: Pastikan class JadwalTayang menerima harga sebagai double atau BigDecimal sesuai kebutuhan.

package cinelux.bioskopcinelux.controller.master;

import cinelux.bioskopcinelux.controller.list.JadwalTayangListCtrl;
import cinelux.bioskopcinelux.model.Film;
import cinelux.bioskopcinelux.model.Studio;
import cinelux.bioskopcinelux.model.JadwalTayang;
import cinelux.bioskopcinelux.model.Role;
import cinelux.bioskopcinelux.service.impl.FilmImpl;
import cinelux.bioskopcinelux.service.impl.StudioImpl;
import cinelux.bioskopcinelux.service.impl.JadwalTayangImpl;
import cinelux.bioskopcinelux.util.MessageBox;
import cinelux.bioskopcinelux.util.OperationResult;
import cinelux.bioskopcinelux.util.RupiahFormatter;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Time;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.UnaryOperator;

public class JadwalTayangCtrl implements Initializable {
    @FXML private Button btnClear, btnFilter, btnSortHapus, btnTambah, btnUpdate, tnHapusFilter;
    @FXML private ComboBox<String> cmbFilm, cmbStudio, cmbHari, cmbFilterKategori, cmbFilterStatus, cmbSortBerdasarkan, cmbSortUrutan;
    @FXML private TextField txtId, txtJamMulai, txtJamSelesai, txtHarga, txtSearch;
    @FXML private VBox vbRowTable;

    private final JadwalTayangImpl jadwalTayangImpl = new JadwalTayangImpl();
    private final FilmImpl filmImpl = new FilmImpl();
    private final StudioImpl studioImpl = new StudioImpl();
    private final MessageBox msg = new MessageBox();
    private final Role user = new Role();
    private List<Film> filmList = new ArrayList<>();
    private List<Studio> studioList = new ArrayList<>();

    private ScheduledExecutorService searchExecutor;
    private ScheduledFuture<?> searchTask;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchExecutor = Executors.newSingleThreadScheduledExecutor();

        txtId.setText(String.valueOf(jadwalTayangImpl.getLastId() + 1));
        txtId.setEditable(false);

        setupTimeFormatter(txtJamMulai);
        setupTimeFormatter(txtJamSelesai);
        setupHargaFormatter();

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> delaySearch());
        cmbFilterStatus.setItems(FXCollections.observableArrayList("Aktif", "Tidak Aktif"));
        cmbFilterStatus.getSelectionModel().select("Aktif");
        cmbFilterKategori.setItems(FXCollections.observableArrayList("Film", "Studio", "Hari"));
        cmbSortBerdasarkan.setItems(FXCollections.observableArrayList("Film", "Studio", "Tanggal"));
        cmbSortUrutan.setItems(FXCollections.observableArrayList("Menaik", "Menurun"));
        cmbSortUrutan.getSelectionModel().select("Menurun");
        cmbHari.setItems(FXCollections.observableArrayList("Weekend", "Weekdays"));

        cmbFilterStatus.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());
        cmbFilterKategori.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());
        cmbSortBerdasarkan.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());
        cmbSortUrutan.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());

        loadFilmComboBox();
        loadStudioComboBox();
        loadDetailsToTable(null, null, 1, "DESC", "Film");
    }

    private void setupTimeFormatter(TextField field) {
        UnaryOperator<TextFormatter.Change> timeFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("^([01]?\\d|2[0-3])(:[0-5]?\\d?)?$") || newText.isEmpty()) return change;
            return null;
        };
        field.setTextFormatter(new TextFormatter<>(timeFilter));
    }

    private void setupHargaFormatter() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();

            // Pastikan selalu diawali dengan "Rp "
            if (!newText.startsWith("Rp ")) {
                return null; // Tolak perubahan
            }

            // Ambil hanya digit angka setelah "Rp "
            String digitsOnly = newText.replaceAll("[^\\d]", "");
            if (digitsOnly.isEmpty()) {
                change.setText("Rp ");
                change.setRange(0, change.getControlText().length());
                return change;
            }

            try {
                double value = Double.parseDouble(digitsOnly);
                String formatted = "Rp " + RupiahFormatter.formatWithSeparator(value);
                change.setText(formatted);
                change.setRange(0, change.getControlText().length());
                return change;
            } catch (NumberFormatException e) {
                return null;
            }
        };

        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        txtHarga.setTextFormatter(textFormatter);
        txtHarga.setText("Rp ");
    }

    private double getHargaValue() throws ParseException {
        String text = txtHarga.getText();
        return RupiahFormatter.parseRupiahToNumber(text);
    }


    private void setHargaValue(double value) {
        txtHarga.setText("Rp " + RupiahFormatter.formatWithSeparator(value));
    }

    private void loadFilmComboBox() {
        filmList = filmImpl.getAllData(null, null, 1, null, null);
        cmbFilm.setItems(FXCollections.observableArrayList(
                filmList.stream().map(Film::getJudul).toList()
        ));
    }

    private void loadStudioComboBox() {
        studioList = studioImpl.getAllData(null, 1, null, null);
        cmbStudio.setItems(FXCollections.observableArrayList(
                studioList.stream().map(Studio::getNama).toList()
        ));
    }

    private void delaySearch() {
        if (searchTask != null && !searchTask.isDone()) searchTask.cancel(true);
        searchTask = searchExecutor.schedule(() -> Platform.runLater(() -> {
            String search = txtSearch.getText();
            String kategori = cmbFilterKategori.getValue();
            Integer status = cmbFilterStatus.getValue().equals("Aktif") ? 1 : 0;
            String urutan = cmbSortUrutan.getValue().equals("Menaik") ? "ASC" : "DESC";
            String sortBy = cmbSortBerdasarkan.getValue();
            loadDetailsToTable(search, kategori, status, urutan, sortBy);
        }), 500, TimeUnit.MILLISECONDS);
    }

    private void loadDetailsToTable(String search, String jenisHari, Integer status, String urutan, String sortBy) {
        vbRowTable.getChildren().clear();
        List<JadwalTayang> list = jadwalTayangImpl.getAllData(search, jenisHari, status, urutan, sortBy);
        if (list.isEmpty()) {
            vbRowTable.getChildren().add(new Label("Tidak ada data ditemukan."));
            return;
        }
        for (JadwalTayang j : list) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/cinelux/bioskopcinelux/view/List/JadwalTayangList.fxml"));
                Parent node = loader.load();
                JadwalTayangListCtrl ctrl = loader.getController();
                ctrl.setData(j);
                ctrl.setJadwalController(this);
                vbRowTable.getChildren().add(node);
            } catch (IOException e) {
                System.err.println("Gagal load: " + e.getMessage());
            }
        }
    }

    @FXML
    void handleBtnInsertClick(ActionEvent e) {
        if (!validateInput()) return;
        try {
            Film film = filmList.stream().filter(f -> f.getJudul().equals(cmbFilm.getValue())).findFirst().orElse(null);
            Studio studio = studioList.stream().filter(s -> s.getNama().equals(cmbStudio.getValue())).findFirst().orElse(null);
            if (film == null || studio == null) {
                msg.alertWarning("Film atau Studio tidak valid.");
                return;
            }
            Time mulai = Time.valueOf(txtJamMulai.getText() + ":00");
            Time selesai = Time.valueOf(txtJamSelesai.getText() + ":00");
            double harga = getHargaValue();
            if (harga <= 0) {
                msg.alertWarning("Harga tiket harus lebih dari 0.");
                return;
            }
            String usr = user.getName() == null ? "Admin" : user.getName();
            JadwalTayang jadwal = new JadwalTayang(
                    film,
                    studio,
                    mulai,
                    selesai,
                    cmbHari.getValue(),
                    new BigDecimal(harga),  // <-- Konversi double ke BigDecimal
                    1,
                    usr
            );

            OperationResult result = jadwalTayangImpl.insertData(jadwal);
            if (result.isSuccess()) {
                msg.alertInfo(result.getMessage());
                delaySearch();
                clearForm();
            } else {
                msg.alertError(result.getMessage());
            }
        } catch (Exception ex) {
            msg.alertError("Error: " + ex.getMessage());
        }
    }

    @FXML
    void handleBtnUpdateClick(ActionEvent e) {
        if (!validateInput()) return;
        try {
            int id = Integer.parseInt(txtId.getText());
            Film film = filmList.stream().filter(f -> f.getJudul().equals(cmbFilm.getValue())).findFirst().orElse(null);
            Studio studio = studioList.stream().filter(s -> s.getNama().equals(cmbStudio.getValue())).findFirst().orElse(null);
            Time mulai = Time.valueOf(txtJamMulai.getText() + ":00");
            Time selesai = Time.valueOf(txtJamSelesai.getText() + ":00");
            double harga = getHargaValue();
            String usr = user.getName() == null ? "Admin" : user.getName();
            JadwalTayang jadwal = new JadwalTayang(
                    id,
                    film,
                    studio,
                    mulai,
                    selesai,
                    cmbHari.getValue(),
                    new BigDecimal(harga),  // <-- Konversi double ke BigDecimal
                    1,
                    usr
            );

            OperationResult result = jadwalTayangImpl.updateData(jadwal);
            if (result.isSuccess()) {
                msg.alertInfo(result.getMessage());
                delaySearch();
                clearForm();
            } else {
                msg.alertError(result.getMessage());
            }
        } catch (Exception ex) {
            msg.alertError("Error: " + ex.getMessage());
        }
    }

    private boolean validateInput() {
        if (cmbFilm.getValue() == null || cmbStudio.getValue() == null ||
                txtJamMulai.getText().isEmpty() || txtJamSelesai.getText().isEmpty() ||
                txtHarga.getText().isEmpty() || cmbHari.getValue() == null ||
                !RupiahFormatter.isValidNumber(txtHarga.getText())) {
            msg.alertWarning("Harap lengkapi semua field dengan benar.");
            return false;
        }
        return true;
    }

    public void clearForm() {
        cmbFilm.getSelectionModel().clearSelection();
        cmbStudio.getSelectionModel().clearSelection();
        cmbHari.getSelectionModel().clearSelection();
        txtJamMulai.clear();
        txtJamSelesai.clear();
        txtHarga.setText("Rp ");
        txtId.setText(String.valueOf(jadwalTayangImpl.getLastId() + 1));
        btnUpdate.setVisible(false);
        btnTambah.setVisible(true);
    }

    public void loadDataToForm(int id) {
        JadwalTayang jadwal = jadwalTayangImpl.getById(id);
        if (jadwal == null) {
            msg.alertError("Data tidak ditemukan.");
            return;
        }
        txtId.setText(String.valueOf(jadwal.getId()));
        cmbFilm.getSelectionModel().select(jadwal.getNamaFilm().getJudul());
        cmbStudio.getSelectionModel().select(jadwal.getNamaStudio().getNama());
        txtJamMulai.setText(jadwal.getJamMulai().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        txtJamSelesai.setText(jadwal.getJamSelesai().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        setHargaValue(jadwal.getHarga().doubleValue());
        cmbHari.getSelectionModel().select(jadwal.getJenisHari());
        btnTambah.setVisible(false);
        btnUpdate.setVisible(true);
    }

    public void toogleStatusJadwal(Integer id) {
        if (id == null) {
            msg.alertWarning("ID jadwal tidak boleh kosong.");
            return;
        }
        JadwalTayang jadwal = jadwalTayangImpl.getById(id);
        if (jadwal == null) {
            msg.alertError("Jadwal tidak ditemukan.");
            return;
        }
        int newStatus = jadwal.getStatus() == 1 ? 0 : 1;
        jadwal.setStatus(newStatus);
        jadwal.setModifiedBy(user.getName() == null ? "Admin" : user.getName());
        OperationResult result = jadwalTayangImpl.updateData(jadwal);
        if (result.isSuccess()) {
            msg.alertInfo("Status jadwal berhasil diperbarui.");
            delaySearch();
        } else {
            msg.alertError("Gagal ubah status: " + result.getMessage());
        }
    }

    public void cleanup() {
        if (searchExecutor != null && !searchExecutor.isShutdown()) {
            searchExecutor.shutdown();
        }
    }

    @FXML
    public void handleBtnClearClick(ActionEvent event) {
        clearForm();
    }

}
