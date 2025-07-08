package cinelux.bioskopcinelux.controller.master;

import cinelux.bioskopcinelux.controller.list.KaryawanListCtrl;
import cinelux.bioskopcinelux.model.Karyawan;
import cinelux.bioskopcinelux.model.Role;
import cinelux.bioskopcinelux.model.Setting;
import cinelux.bioskopcinelux.service.impl.KaryawanImpl;
import cinelux.bioskopcinelux.service.impl.SettingImpl;
import cinelux.bioskopcinelux.util.MessageBox;
import cinelux.bioskopcinelux.util.OperationResult;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class KaryawanCtrl implements Initializable {

    @FXML
    private Button btnClear, btnPilihGambar, btnSortHapus, btnTambah, btnUpdate, btnUrutan;
    @FXML
    private ComboBox<String> cmbFilterKategori, cmbFilterStatus, cmbSortBerdasarkan, cmbSortUrutan;
    @FXML
    private ComboBox<String> cmbRole;
    @FXML
    private ImageView imgPegawai;
    @FXML
    private Button btnHapusFilter;
    @FXML
    private TextArea txtAlamat;
    @FXML
    private TextField txtId, txtNama, txtNoTelp, txtPassword, txtSearch, txtUsername;
    @FXML
    private VBox vbRowTable;

    private ScheduledExecutorService searchExecutor;
    private ScheduledFuture<?> searchTask;

    private final KaryawanImpl karyawanImpl = new KaryawanImpl();
    private final SettingImpl settingImpl = new SettingImpl();
    private final MessageBox msg = new MessageBox();
    private final Role user = new Role();

    private List<Setting> roleList = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        btnPilihGambar.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
            );

            File selectedFile = fileChooser.showOpenDialog(btnPilihGambar.getScene().getWindow());

            if (selectedFile != null) {
                try {
                    Path destination = Paths.get("src/main/resources/images", selectedFile.getName());
                    Files.createDirectories(destination.getParent());
                    Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                    // Load gambar ke ImageView
                    Image image = new Image(destination.toUri().toString());
                    imgPegawai.setImage(image);

                } catch (IOException ex) {
                    msg.alertWarning("Import gagal: " + ex.getMessage());
                }
            }
        });



        searchExecutor = Executors.newSingleThreadScheduledExecutor();
        txtId.setText(String.valueOf(karyawanImpl.getLastId() + 1));

        txtNama.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.length() > 50 || !newText.matches("[a-zA-Z ]*")) {
                return null;
            }
            return change;
        }));

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> delaySearch());
        cmbFilterStatus.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());
        cmbFilterKategori.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());
        cmbSortBerdasarkan.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());
        cmbSortUrutan.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());

        cmbFilterStatus.setItems(FXCollections.observableArrayList("Aktif", "Tidak Aktif"));
        cmbFilterStatus.getSelectionModel().select("Aktif");
        cmbSortBerdasarkan.setItems(FXCollections.observableArrayList("Nama", "Kategori"));
        cmbSortUrutan.setItems(FXCollections.observableArrayList("Menaik", "Menurun"));
        cmbSortUrutan.getSelectionModel().select("Menurun");
        cmbFilterKategori.setItems(FXCollections.observableArrayList("Admin", "Manajer", "Kasir"));

        loadRoleComboBox();
        loadDetailsToTable(null, null, null, null, null);
    }

    private void loadRoleComboBox() {
        roleList = settingImpl.getAllData(null, "Role", 1, null, null);
        cmbRole.getItems().clear();
        for (Setting role : roleList) {
            cmbRole.getItems().add(role.getNama());
        }
    }

    private void delaySearch() {
        if (searchTask != null && !searchTask.isDone()) {
            searchTask.cancel(true);
        }
        searchTask = searchExecutor.schedule(() -> {
            Platform.runLater(() -> {
                String search = txtSearch.getText();
                String kategori = cmbFilterKategori.getSelectionModel().getSelectedItem();
                Integer status = cmbFilterStatus.getSelectionModel().getSelectedItem().equals("Aktif") ? 1 : 0;
                String urutan = cmbSortUrutan.getSelectionModel().getSelectedItem().equals("Menaik") ? "ASC" : "DESC";
                String sortBy = cmbSortBerdasarkan.getSelectionModel().getSelectedItem();
                loadDetailsToTable(search, kategori, status, urutan, sortBy);
            });
        }, 500, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    private void loadDetailsToTable(String search, String role, Integer status, String urutan, String sortBy) {
        vbRowTable.getChildren().clear();

        List<Karyawan> dKaryawanList = karyawanImpl.getAllData(search, role, status, urutan, sortBy);

        // ✅ Handle jika list kosong
        if (dKaryawanList.isEmpty()) {
            Label lblKosong = new Label("Tidak ada data karyawan ditemukan.");
            vbRowTable.getChildren().add(lblKosong);
            return;
        }

        // ✅ Load setiap data ke FXML Card
        String fxmlPath = "/cinelux/bioskopcinelux/view/List/KaryawanList.fxml";
        for (Karyawan dKaryawan : dKaryawanList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent node = loader.load();
                KaryawanListCtrl listCtrl = loader.getController();
                listCtrl.setData(dKaryawan);
                listCtrl.setKaryawansController(this);
                vbRowTable.getChildren().add(node);
            } catch (IOException e) {
                System.err.println("Gagal memuat KaryawanList.fxml: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

//    private Setting getSelectedRole() {
//        String selectedRoleName = cmbRole.getSelectionModel().getSelectedItem();
//        return roleList.stream()
//                .filter(role -> role.getNama().equals(selectedRoleName))
//                .findFirst()
//                .orElse(null);
//    }

    public void toogleStatusKaryawan(int id) {
        Karyawan karyawan = karyawanImpl.getById(id);
        if (karyawan == null) {
            msg.alertWarning("Data karyawan tidak ditemukan.");
            return;
        }

        String confirmMsg = karyawan.getStatus() == 1
                ? "Apakah Anda yakin ingin menonaktifkan karyawan ini?"
                : "Apakah Anda yakin ingin mengaktifkan kembali karyawan ini?";

        if (msg.alertConfirm(confirmMsg)) {
            // Ambil user yang sedang login
            String usr = user != null && user.getName() != null && !user.getName().isEmpty()
                    ? user.getName()
                    : "Admin";

            OperationResult result = karyawanImpl.deleteData(id, usr); // ← kirim nama pengubah
            if (result.isSuccess()) {
                msg.alertInfo(result.getMessage());
                delaySearch(); // reload data
            } else {
                msg.alertError(result.getMessage());
            }
        }
    }

    public void loadDatatoForm(int id) {
        btnTambah.setVisible(false);
        btnUpdate.setVisible(true);

        try {
            Karyawan karyawan = karyawanImpl.getById(id);

            if (karyawan != null) {
                txtId.setText(String.valueOf(karyawan.getId()));
                txtNama.setText(karyawan.getNama());
                txtUsername.setText(karyawan.getUsername());
                txtPassword.setText(karyawan.getPassword());
                txtNoTelp.setText(karyawan.getNoTelp());
                txtAlamat.setText(karyawan.getAlamat());

                // Set Role ke ComboBox
                Setting role = karyawan.getRole();
                if (role != null && cmbRole.getItems().contains(role.getNama())) {
                    cmbRole.getSelectionModel().select(role.getNama());
                } else {
                    cmbRole.getSelectionModel().clearSelection();
                    System.out.println("Role tidak ditemukan: " + (role != null ? role.getNama() : "null"));
                }

                // Set Status ke ComboBox filter status (kalau ada)
                if (cmbFilterStatus.getItems().contains(karyawan.getStatus() == 1 ? "Aktif" : "Tidak Aktif")) {
                    cmbFilterStatus.getSelectionModel().select(karyawan.getStatus() == 1 ? "Aktif" : "Tidak Aktif");
                } else {
                    cmbFilterStatus.getSelectionModel().clearSelection();
                }

            } else {
                msg.alertError("Data karyawan tidak ditemukan.");
            }
        } catch (Exception e) {
            msg.alertError("Terjadi kesalahan saat mengambil data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertData() {
        String usr;
        if (user.getName() == null) {
            usr = "Admin";
        } else {
            usr = user.getName();
        }

        // Ambil role yang dipilih
        String selectedRoleName = cmbRole.getSelectionModel().getSelectedItem();
        Setting selectedRole = roleList.stream()
                .filter(role -> role.getNama().equals(selectedRoleName))
                .findFirst()
                .orElse(null);

        if (selectedRole == null) {
            msg.alertWarning("Pilih Role terlebih dahulu.");
            return;
        }

        // Buat objek Karyawan baru
        Karyawan newKaryawan = new Karyawan(
                Integer.parseInt(txtId.getText()),
                selectedRole, // Role (relasi ke dtSetting)
                txtNama.getText(),
                txtUsername.getText(),
                txtPassword.getText(),
                txtNoTelp.getText(),
                txtAlamat.getText(),
                1, // Status default aktif
                usr,
                null // modified_by kosong saat insert
        );

        // Simpan data ke database
        OperationResult result = karyawanImpl.insertData(newKaryawan);

        if (result.isSuccess()) {
            msg.alertInfo(result.getMessage());
            delaySearch(); // Refresh table
        } else {
            msg.alertError(result.getMessage());
        }
        clearForm(); // Reset form input
    }

    private void updateData() {
        // Validasi input
        if (txtNama.getText().trim().isEmpty()) {
            msg.alertWarning("Nama karyawan tidak boleh kosong!");
            return;
        }
        if (txtUsername.getText().trim().isEmpty()) {
            msg.alertWarning("Username tidak boleh kosong!");
            return;
        }

        if (cmbRole.getSelectionModel().isEmpty()) {
            msg.alertWarning("Role harus dipilih!");
            return;
        }

        String usr;
        if (user.getName() == null || user.getName().isEmpty()) {
            usr = "Admin"; // fallback jika user tidak terdeteksi
        } else {
            usr = user.getName();
        }

        int karyawanId;
        try {
            karyawanId = Integer.parseInt(txtId.getText().trim());
        } catch (NumberFormatException e) {
            msg.alertError("ID Karyawan tidak valid.");
            return;
        }

        // Ambil role yang dipilih dari ComboBox
        String selectedRoleName = cmbRole.getSelectionModel().getSelectedItem();
        Setting selectedRole = roleList.stream()
                .filter(role -> role.getNama().equals(selectedRoleName))
                .findFirst()
                .orElse(null);

        if (selectedRole == null) {
            msg.alertWarning("Role yang dipilih tidak valid.");
            return;
        }

        // Buat objek Karyawan baru dengan data dari form
        Karyawan updatedKaryawan = new Karyawan(
                karyawanId,                     // ID
                selectedRole,                   // Role (Setting)
                txtNama.getText().trim(),       // Nama
                txtUsername.getText().trim(),   // Username
                txtPassword.getText().trim(),   // Password
                txtNoTelp.getText().trim(),     // No Telp
                txtAlamat.getText().trim(),     // Alamat
                1,                              // Status aktif (default saat update)
                null,                           // createdBy null saat update
                usr                             // modifiedBy
        );

        // Panggil service untuk update
        OperationResult result = karyawanImpl.updateData(updatedKaryawan);

        if (result.isSuccess()) {
            msg.alertInfo(result.getMessage());
            delaySearch(); // Refresh data table setelah update
        } else {
            msg.alertError(result.getMessage());
        }

        clearForm(); // Kosongkan form setelah update
    }


    private void clearForm() {
        txtNama.clear();
        txtUsername.clear();
        txtPassword.clear();
        txtNoTelp.clear();
        txtAlamat.clear();
        cmbRole.getSelectionModel().clearSelection();
        imgPegawai.setImage(null); // Kalau ada gambar pegawai, reset juga

        // Set ID karyawan berikutnya (auto increment)
        txtId.setText(String.valueOf(karyawanImpl.getLastId() + 1));

        // Atur visibilitas tombol
        btnUpdate.setVisible(false);
        btnTambah.setVisible(true);
    }

    @FXML
    void handleBtnInsertClicked(ActionEvent event) {insertData();}

    @FXML
    void handleBtnUpdateClicked(ActionEvent event) {
        updateData();}

    @FXML
    void handleBtnClearClicked(ActionEvent event) {clearForm();}

}
