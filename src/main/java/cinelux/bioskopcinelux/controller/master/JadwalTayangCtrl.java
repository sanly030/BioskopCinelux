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
import java.net.URL;
import java.sql.Time;
import java.sql.Date;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.UnaryOperator;

public class JadwalTayangCtrl implements Initializable {
    @FXML private Button btnClear, btnFilter, btnSortHapus, btnTambah, btnUpdate, tnHapusFilter;
    @FXML private ComboBox<String> cmbFilm, cmbStudio, cmbHari, cmbFilterKategori, cmbFilterStatus, cmbSortBerdasarkan, cmbSortUrutan;
    @FXML private TextField txtId, txtJamMulai, txtJamSelesai, txtHarga, txtSearch;
    @FXML private DatePicker dpTanggal;
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

        // Tampilkan ID yang akan digunakan selanjutnya (read-only)
        txtId.setText(String.valueOf(jadwalTayangImpl.getLastId() + 1));
        txtId.setEditable(false); // Buat read-only karena auto-increment

        setupTimeFormatter(txtJamMulai);
        setupTimeFormatter(txtJamSelesai);
        setupHargaFormatter();

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> delaySearch());
        cmbFilterStatus.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());
        cmbFilterKategori.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());
        cmbSortBerdasarkan.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());
        cmbSortUrutan.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());

        cmbFilterStatus.setItems(FXCollections.observableArrayList("Aktif", "Tidak Aktif"));
        cmbFilterStatus.getSelectionModel().select("Aktif");
        cmbSortBerdasarkan.setItems(FXCollections.observableArrayList("Film", "Studio", "Tanggal"));
        cmbSortUrutan.setItems(FXCollections.observableArrayList("Menaik", "Menurun"));
        cmbSortUrutan.getSelectionModel().select("Menurun");
        cmbHari.setItems(FXCollections.observableArrayList("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"));
        cmbFilterKategori.setItems(FXCollections.observableArrayList("Film", "Studio", "Hari"));

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
        NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        rupiahFormat.setMaximumFractionDigits(0);

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText().replaceAll("[^\\d]", "");
            if (newText.isEmpty()) return change;
            try {
                long value = Long.parseLong(newText);
                String formatted = rupiahFormat.format(value);
                change.setText(formatted);
                change.setRange(0, change.getControlText().length());
                change.setCaretPosition(formatted.length());
                change.setAnchor(formatted.length());
                return change;
            } catch (NumberFormatException e) {
                return null;
            }
        };
        txtHarga.setTextFormatter(new TextFormatter<>(filter));
    }

    private void loadFilmComboBox() {
        filmList = filmImpl.getAllData(null, null, 1, null, null);
        cmbFilm.getItems().clear();
        for (Film film : filmList) cmbFilm.getItems().add(film.getJudul());
    }

    private void loadStudioComboBox() {
        studioList = studioImpl.getAllData(null, 1, null, null);
        cmbStudio.getItems().clear();
        for (Studio studio : studioList) cmbStudio.getItems().add(studio.getNama());
    }

    private void delaySearch() {
        if (searchTask != null && !searchTask.isDone()) searchTask.cancel(true);
        searchTask = searchExecutor.schedule(() -> Platform.runLater(() -> {
            String search = txtSearch.getText();
            String kategori = cmbFilterKategori.getSelectionModel().getSelectedItem();
            Integer status = cmbFilterStatus.getSelectionModel().getSelectedItem().equals("Aktif") ? 1 : 0;
            String urutan = cmbSortUrutan.getSelectionModel().getSelectedItem().equals("Menaik") ? "ASC" : "DESC";
            String sortBy = cmbSortBerdasarkan.getSelectionModel().getSelectedItem();
            loadDetailsToTable(search, kategori, status, urutan, sortBy);
        }), 500, TimeUnit.MILLISECONDS);
    }

    private void loadDetailsToTable(String search, String kategori, Integer status, String urutan, String sortBy) {
        vbRowTable.getChildren().clear();
        List<JadwalTayang> list = jadwalTayangImpl.getAllData(search, kategori, status, urutan, sortBy);

        if (list.isEmpty()) {
            vbRowTable.getChildren().add(new Label("Tidak ada data ditemukan."));
            return;
        }

        String fxmlPath = "/cinelux/bioskopcinelux/view/List/JadwalTayangList.fxml";
        for (JadwalTayang j : list) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent node = loader.load();
                JadwalTayangListCtrl ctrl = loader.getController();
                ctrl.setData(j);
                ctrl.setJadwalController(this);
                vbRowTable.getChildren().add(node);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Gagal load: " + e.getMessage());
            }
        }
    }

    @FXML
    void handleBtnInsertClick(ActionEvent e) {
        if (!validateInput()) return;

        String filmNama = cmbFilm.getValue();
        String studioNama = cmbStudio.getValue();
        Film film = filmList.stream().filter(f -> f.getJudul().equals(filmNama)).findFirst().orElse(null);
        Studio studio = studioList.stream().filter(s -> s.getNama().equals(studioNama)).findFirst().orElse(null);

        if (film == null || studio == null) {
            msg.alertWarning("Film atau Studio tidak valid.");
            return;
        }

        try {
            // Fixed: Add validation for time format before converting
            String jamMulaiText = txtJamMulai.getText().trim();
            String jamSelesaiText = txtJamSelesai.getText().trim();

            // Add seconds if not present
            if (!jamMulaiText.contains(":")) {
                jamMulaiText += ":00";
            } else if (jamMulaiText.split(":").length == 2) {
                jamMulaiText += ":00";
            }

            if (!jamSelesaiText.contains(":")) {
                jamSelesaiText += ":00";
            } else if (jamSelesaiText.split(":").length == 2) {
                jamSelesaiText += ":00";
            }

            Time mulai = Time.valueOf(jamMulaiText);
            Time selesai = Time.valueOf(jamSelesaiText);

            // Validate time logic
            if (mulai.compareTo(selesai) >= 0) {
                msg.alertWarning("Jam mulai harus lebih awal dari jam selesai.");
                return;
            }

            Double harga = Double.parseDouble(txtHarga.getText().replaceAll("[^\\d]", ""));
            String hari = cmbHari.getValue();
            String usr = user.getName() == null ? "Admin" : user.getName();

            JadwalTayang jadwal = new JadwalTayang(null, film, studio, mulai, selesai, hari, harga, 1, usr);
            OperationResult result = jadwalTayangImpl.insertData(jadwal);

            if (result.isSuccess()) {
                msg.alertInfo(result.getMessage());
                delaySearch();
                clearForm();
            } else {
                msg.alertError(result.getMessage());
            }
        } catch (IllegalArgumentException ex) {
            msg.alertError("Format waktu tidak valid. Gunakan format HH:MM");
        } catch (Exception ex) {
            msg.alertError("Data tidak valid: " + ex.getMessage());
        }
    }

    @FXML
    void handleBtnUpdateClick(ActionEvent e) {
        if (!validateInput()) return;

        try {
            int id = Integer.parseInt(txtId.getText());
            String filmNama = cmbFilm.getValue();
            String studioNama = cmbStudio.getValue();
            Film film = filmList.stream().filter(f -> f.getJudul().equals(filmNama)).findFirst().orElse(null);
            Studio studio = studioList.stream().filter(s -> s.getNama().equals(studioNama)).findFirst().orElse(null);
            Time mulai = Time.valueOf(txtJamMulai.getText() + ":00");
            Time selesai = Time.valueOf(txtJamSelesai.getText() + ":00");
            Double harga = Double.parseDouble(txtHarga.getText().replaceAll("[^\\d]", ""));
            String hari = cmbHari.getValue();
            String usr = user.getName() == null ? "Admin" : user.getName();

            JadwalTayang jadwal = new JadwalTayang(id, film, studio, mulai, selesai, hari, harga, 1, usr);
            OperationResult result = jadwalTayangImpl.updateData(jadwal);

            if (result.isSuccess()) {
                msg.alertInfo(result.getMessage());
                delaySearch();
                clearForm();
            } else msg.alertError(result.getMessage());
        } catch (Exception ex) {
            msg.alertError("Data tidak valid: " + ex.getMessage());
        }
    }

    private boolean validateInput() {
        if (cmbFilm.getValue() == null || cmbStudio.getValue() == null ||
                txtJamMulai.getText().isEmpty() || txtJamSelesai.getText().isEmpty() ||
                txtHarga.getText().isEmpty() || cmbHari.getValue() == null) {
            msg.alertWarning("Semua field harus diisi.");
            return false;
        }
        return true;
    }

    @FXML
    void handleBtnClearClick(ActionEvent event) {
        clearForm();
    }

    private void clearForm() {
        cmbFilm.getSelectionModel().clearSelection();
        cmbStudio.getSelectionModel().clearSelection();
        cmbHari.getSelectionModel().clearSelection();
        txtJamMulai.clear();
        txtJamSelesai.clear();
        txtHarga.clear();
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
        txtHarga.setText(NumberFormat.getCurrencyInstance(new Locale("in", "ID")).format(jadwal.getHarga()));
        cmbHari.getSelectionModel().select(jadwal.getJenisHari());

        btnTambah.setVisible(false);
        btnUpdate.setVisible(true);
    }

    public void toogleStatusJadwal(int id) {
        JadwalTayang jadwal = jadwalTayangImpl.getById(id);
        if (jadwal == null) {
            msg.alertWarning("Data tidak ditemukan.");
            return;
        }

        String confirmMsg = jadwal.getStatus() == 1 ? "Nonaktifkan jadwal ini?" : "Aktifkan kembali jadwal ini?";
        if (msg.alertConfirm(confirmMsg)) {
            String usr = (user.getName() == null) ? "Admin" : user.getName();
            OperationResult result = jadwalTayangImpl.deleteData(id, usr);
            if (result.isSuccess()) {
                msg.alertInfo(result.getMessage());
                delaySearch();
            } else {
                msg.alertError(result.getMessage());
            }
        }
    }

    @FXML
    void handleFilterHapusClick(ActionEvent event) {
        txtSearch.clear();
        cmbFilterKategori.getSelectionModel().clearSelection();
        cmbFilterStatus.getSelectionModel().select("Aktif");
        cmbSortBerdasarkan.getSelectionModel().clearSelection();
        cmbSortUrutan.getSelectionModel().select("Menurun");
        delaySearch();
    }

    public void refreshData() {
        loadFilmComboBox();
        loadStudioComboBox();
        delaySearch();
    }
}