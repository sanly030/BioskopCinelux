package cinelux.bioskopcinelux.controller.master;

import cinelux.bioskopcinelux.connection.DBConnect;
import cinelux.bioskopcinelux.controller.list.StudioListCtrl;
import cinelux.bioskopcinelux.model.Role;
import cinelux.bioskopcinelux.model.Studio;
import cinelux.bioskopcinelux.service.impl.FilmImpl;
import cinelux.bioskopcinelux.service.impl.StudioImpl;
import cinelux.bioskopcinelux.util.MessageBox;
import cinelux.bioskopcinelux.util.OperationResult;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class StudioCtrl implements Initializable {
    @FXML
    private Button btnClear;
    @FXML
    private Button btnFilter;
    @FXML
    private Button btnSortHapus;
    @FXML
    private Button btnTambah;
    @FXML
    private Button btnUpdate;
    @FXML
    private Button btnUrutan;
    @FXML
    private ComboBox<String> cmbFilterKategori;
    @FXML
    private ComboBox<String> cmbFilterStatus;
    @FXML
    private ComboBox<String> cmbSortBerdasarkan;
    @FXML
    private ComboBox<String> cmbSortUrutan;
    @FXML
    private Button tnHapusFilter;
    @FXML
    private TextField txtBaris;
    @FXML
    private TextField txtId;
    @FXML
    private TextField txtKapasitas;
    @FXML
    private TextField txtKolom;
    @FXML
    private TextField txtNama;
    @FXML
    private TextField txtSearch;
    @FXML
    private VBox vbRowTable;

    private ScheduledExecutorService searchExecutor;
    private ScheduledFuture<?> searchTask;
    private Studio selectedStudio; // Variable untuk menyimpan studio yang dipilih

    DBConnect connect = new DBConnect();
    StudioImpl studioImpl = new StudioImpl();
    MessageBox msg = new MessageBox();
    Role user = new Role();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Event listener untuk txtBaris
        txtBaris.textProperty().addListener((observable, oldValue, newValue) -> {
            calculateCapacity();
        });

        // Event listener untuk txtKolom
        txtKolom.textProperty().addListener((observable, oldValue, newValue) -> {
            calculateCapacity();
        });
        searchExecutor = Executors.newSingleThreadScheduledExecutor();
        txtId.setEditable(false);
        txtId.setText(String.valueOf(studioImpl.getLastId() + 1));


        cmbFilterStatus.getItems().addAll("Aktif", "Tidak Aktif");
        cmbFilterStatus.getSelectionModel().select("Aktif");
        cmbSortBerdasarkan.getItems().addAll("Judul", "Genre");
        cmbSortUrutan.getItems().addAll("Menaik", "Menurun");
        cmbSortUrutan.getSelectionModel().select("Menurun");

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> delaySearch());
        cmbFilterStatus.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());
        cmbSortBerdasarkan.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());
        cmbSortUrutan.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());
        cmbFilterKategori.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());

        loadDetailsToTable(null, null, null, null);
    }

    private void delaySearch() {
        if (searchTask != null && !searchTask.isDone()) {
            searchTask.cancel(true);
        }

        searchTask = searchExecutor.schedule(() -> javafx.application.Platform.runLater(() -> {
            String search = txtSearch.getText();
            String statusText = cmbFilterStatus.getValue();
            String categoryText = cmbFilterKategori.getValue();
            String urutan = cmbSortUrutan.getValue();
            String sortBy = cmbSortBerdasarkan.getValue();

            int status = statusText.equalsIgnoreCase("Aktif") ? 1 : 0;
            loadDetailsToTable(search.isEmpty() ? null : search, status, urutan, sortBy);
        }), 0, TimeUnit.MILLISECONDS);
    }

    private void loadDetailsToTable(String search, Integer status, String urutan, String sortBy) {
        vbRowTable.getChildren().clear();
        List<Studio> studioList = studioImpl.getAllData(search, status, urutan, sortBy);

        if (studioList.isEmpty()) {
            vbRowTable.getChildren().add(new Label("Tidak ada data studio ditemukan."));
            return;
        }

        for (Studio studio : studioList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/cinelux/bioskopcinelux/view/List/StudioList.fxml"));
                Parent node = loader.load();
                StudioListCtrl listCtrl = loader.getController();
                listCtrl.setStudio(studio);
                listCtrl.setStudioController(this); // hubungkan controller utama
                vbRowTable.getChildren().add(node);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void toogleStatusStudio(int id, String modifiedBy) {
        Studio studio = studioImpl.getById(id);
        if (studio == null) {
            msg.alertWarning("Data studio tidak ditemukan.");
            return;
        }

        String currentUser = (user.getName() != null) ? user.getName() : "Admin";

        boolean confirm = msg.alertConfirm(
                studio.getStatus() == 1 ? "Nonaktifkan studio ini?" : "Aktifkan kembali studio ini?"
        );

        if (confirm) {
            OperationResult result = studioImpl.deleteData(id, currentUser);
            if (result.isSuccess()) {
                msg.alertInfo(result.getMessage());
                delaySearch();
            } else {
                msg.alertError(result.getMessage());
            }
        }
    }

    public void loadDatatoForm(int id) {
        btnTambah.setVisible(false);
        btnUpdate.setVisible(true);

        Studio studio = studioImpl.getById(id);
        if (studio != null) {
            selectedStudio = studio; // Set selectedStudio untuk digunakan di update
            txtId.setText(String.valueOf(studio.getId()));
            txtNama.setText(studio.getNama());
            txtBaris.setText(String.valueOf(studio.getBaris()));
            txtKolom.setText(String.valueOf(studio.getKolom()));
            txtKapasitas.setText(String.valueOf(studio.getKapasitas()));
            // Jika ada ComboBox untuk status atau kategori, bisa ditambahkan di sini
        } else {
            msg.alertError("Data studio tidak ditemukan.");
        }
    }

    @FXML
    void handleBtnClearClick(ActionEvent event) {
        clearInputFields();
        selectedStudio = null;
        btnTambah.setVisible(true);
        btnUpdate.setVisible(false);
    }

    @FXML
    void handleBtnInsertClick(ActionEvent event) {
        try {
            // Validasi input fields
            if (txtNama.getText().trim().isEmpty()) {
                showAlert("Error", "Nama studio tidak boleh kosong!");
                return;
            }

            if (txtBaris.getText().trim().isEmpty()) {
                showAlert("Error", "Jumlah baris tidak boleh kosong!");
                return;
            }

            if (txtKolom.getText().trim().isEmpty()) {
                showAlert("Error", "Jumlah kolom tidak boleh kosong!");
                return;
            }

            // Parse input values
            String namaStudio = txtNama.getText().trim();
            int baris = Integer.parseInt(txtBaris.getText().trim());
            int kolom = Integer.parseInt(txtKolom.getText().trim());

            // Validasi nilai baris dan kolom
            if (baris <= 0 || kolom <= 0) {
                showAlert("Error", "Jumlah baris dan kolom harus lebih dari 0!");
                return;
            }

            // Hitung kapasitas otomatis
            int kapasitas = baris * kolom;
            txtKapasitas.setText(String.valueOf(kapasitas)); // Optional: show it in the UI

            // Ambil user login
            String usr = (user.getName() == null) ? "Admin" : user.getName();

            // Buat objek Studio
            Studio studio = new Studio(null, namaStudio, baris, kolom, kapasitas, 1, usr, null);

            // Panggil method insert
            OperationResult result = studioImpl.InsertStudioWithKursi(studio);

            if (result.isSuccess()) {
                showAlert("Success", result.getMessage());
                clearInputFields();
                refreshStudioTable(); // Refresh tabel
            } else {
                showAlert("Error", result.getMessage());
            }

        } catch (NumberFormatException e) {
            showAlert("Error", "Format angka tidak valid untuk baris atau kolom!");
        } catch (Exception e) {
            showAlert("Error", "Terjadi kesalahan: " + e.getMessage());
        }
    }

    // Method untuk update studio dengan kursi
    public OperationResult updateStudioWithKursi(Studio studio) {
        try {
            // Menggunakan method yang ada di StudioImpl
            OperationResult result = studioImpl.updateStudioWithKursi(studio);
            return result;
        } catch (Exception e) {
            return OperationResult.failure("Gagal memperbarui studio: " + e.getMessage());
        }
    }

    @FXML
    void handleBtnUpdateClick(ActionEvent event) {
        try {
            // Validasi apakah ada studio yang dipilih untuk diupdate
            if (selectedStudio == null) {
                showAlert("Error", "Pilih studio yang akan diperbarui!");
                return;
            }

            // Validasi input fields
            if (txtNama.getText().trim().isEmpty()) {
                showAlert("Error", "Nama studio tidak boleh kosong!");
                return;
            }

            if (txtBaris.getText().trim().isEmpty()) {
                showAlert("Error", "Jumlah baris tidak boleh kosong!");
                return;
            }

            if (txtKolom.getText().trim().isEmpty()) {
                showAlert("Error", "Jumlah kolom tidak boleh kosong!");
                return;
            }

            // Parse input values
            String namaStudio = txtNama.getText().trim();
            int baris = Integer.parseInt(txtBaris.getText().trim());
            int kolom = Integer.parseInt(txtKolom.getText().trim());

            // Validasi nilai baris dan kolom
            if (baris <= 0 || kolom <= 0) {
                showAlert("Error", "Jumlah baris dan kolom harus lebih dari 0!");
                return;
            }

            // Hitung kapasitas otomatis berdasarkan baris x kolom
            int kapasitas = baris * kolom;

            // Update kapasitas field
            txtKapasitas.setText(String.valueOf(kapasitas));

            // Konfirmasi update jika ada perubahan pada baris/kolom
            if (baris != selectedStudio.getBaris() || kolom != selectedStudio.getKolom()) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Konfirmasi Update");
                confirmAlert.setHeaderText("Perubahan Baris/Kolom Detected");
                confirmAlert.setContentText("Mengubah baris atau kolom akan menghapus semua kursi lama dan membuat kursi baru. Lanjutkan?");

                if (confirmAlert.showAndWait().get() != ButtonType.OK) {
                    return;
                }
            }

            String usr = (user.getName() == null) ? "Admin" : user.getName();

            // Buat object Studio untuk update
            // ✅ Benar urutan: baris, kolom, kapasitas
            Studio studio = new Studio(
                    selectedStudio.getId(),
                    namaStudio,
                    baris,
                    kolom,
                    kapasitas,
                    selectedStudio.getStatus(),
                    null,
                    usr
            );

            // Panggil method update
            OperationResult result = updateStudioWithKursi(studio);

            if (result.isSuccess()) {
                showAlert("Success", result.getMessage());
                clearInputFields();
                refreshStudioTable(); // Refresh tabel
                selectedStudio = null; // Reset selected studio
                btnTambah.setVisible(true);
                btnUpdate.setVisible(false);
            } else {
                showAlert("Error", result.getMessage());
            }

        } catch (NumberFormatException e) {
            showAlert("Error", "Format angka tidak valid untuk baris atau kolom!");
        } catch (Exception e) {
            showAlert("Error", "Terjadi kesalahan: " + e.getMessage());
        }
    }

    private void calculateCapacity() {
        try {
            String barisText = txtBaris.getText().trim();
            String kolomText = txtKolom.getText().trim();

            if (!barisText.isEmpty() && !kolomText.isEmpty()) {
                int baris = Integer.parseInt(barisText);
                int kolom = Integer.parseInt(kolomText);

                if (baris > 0 && kolom > 0) {
                    int kapasitas = baris * kolom;
                    txtKapasitas.setText(String.valueOf(kapasitas));
                } else {
                    txtKapasitas.setText("");
                }
            } else {
                txtKapasitas.setText("");
            }
        } catch (NumberFormatException e) {
            txtKapasitas.setText("");
        }
    }

    private void clearInputFields() {
        txtId.setText(String.valueOf(studioImpl.getLastId() + 1));
        txtNama.clear();
        txtBaris.clear();
        txtKolom.clear();
        txtKapasitas.clear();
    }

    // Method helper untuk show alert
    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Success") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method untuk refresh tabel
    private void refreshStudioTable() {
        delaySearch(); // Menggunakan method yang sudah ada untuk refresh
    }
}