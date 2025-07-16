package cinelux.bioskopcinelux.controller.master;

import cinelux.bioskopcinelux.controller.list.SettingListCtrl;
import cinelux.bioskopcinelux.model.Role;
import cinelux.bioskopcinelux.model.Setting;
import cinelux.bioskopcinelux.service.impl.SettingImpl;
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
import java.util.concurrent.*;

public class SettingCtrl implements Initializable {
    @FXML private Button btnClear, btnTambah, btnUpdate, btnFilter, btnUrutkan;
    @FXML private ComboBox<String> cmbKategori, cmbFilterKategori, cmbSortUrutan, cmbSortBerdasarkan;
    @FXML private TextField txtId, txtNama, txtSearch, txtValue;
    @FXML private VBox vbFilter, vbRowTable;

    @FXML
    private ComboBox<String> cmbFilterCategory;

    @FXML
    private ComboBox<String> cmbFilterStatus;

    private ScheduledExecutorService searchExecutor;
    private ScheduledFuture<?> searchTask;

    private final SettingImpl settingImpl = new SettingImpl();
    private final MessageBox msg = new MessageBox();
    private final Role user = new Role();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        searchExecutor = Executors.newSingleThreadScheduledExecutor();
        txtId.setText(String.valueOf(settingImpl.getLastId() + 1));

        txtNama.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            // Batasi maksimal 50 karakter
            if (newText.length() > 50) {
                return null; // Batalkan perubahan jika lebih dari 50 karakter
            }
            // Validasi hanya huruf dan spasi
            if (!newText.matches("[a-zA-Z ]*")) {
                return null; // Batalkan perubahan jika ada karakter selain huruf atau spasi
            }
            return change; // Terima perubahan jika valid
        }));

//        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> delaySearch());
//        cmbFilterStatus.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());
//        cmbFilterCategory.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());
//        cmbSortBerdasarkan.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());
//        cmbSortUrutan.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());
//
//        cmbKategori.getItems().addAll("Role", "Jenis Makanan");
//
//        cmbFilterStatus.getItems().addAll("Aktif", "Tidak Aktif");
//        cmbFilterStatus.getSelectionModel().select("Aktif");
//
//        cmbSortBerdasarkan.getItems().addAll("Nama", "Kategori");
//        cmbKategori.getSelectionModel().select("Nama");
//
//        cmbSortUrutan.getItems().addAll("Menaik", "Menurun");
//        cmbSortUrutan.getSelectionModel().select("Menurun");
//
//
//        cmbFilterKategori.getItems().addAll("None", "Jenis Makanan", "Role");

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            delaySearch();
        });

        cmbFilterStatus.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            delaySearch();
        });

        cmbFilterCategory.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            delaySearch();
        });

        cmbKategori.getItems().addAll("Metode Pembayaran", "Role", "Jenis Makanan");

        cmbFilterStatus.getItems().addAll("Aktif", "Tidak Aktif");
        cmbFilterStatus.getSelectionModel().selectFirst();

        cmbFilterCategory.getItems().addAll("None","Metode Pembayaran", "Role", "Jenis Makanan");
        cmbFilterCategory.getSelectionModel().selectFirst();

        loadDetailsToTable(null, null, null);
    }


//    private void delaySearch() {
//        if (searchTask != null && !searchTask.isDone()) {
//            searchTask.cancel(true);
//        }
//
//        searchTask = searchExecutor.schedule(() -> javafx.application.Platform.runLater(() -> {
//            String searchText = txtSearch.getText();
//            String statusText = cmbFilterStatus.getSelectionModel().getSelectedItem();
//            String categoryText = cmbFilterKategori.getSelectionModel().getSelectedItem();
//            String urutan = cmbSortUrutan.getSelectionModel().getSelectedItem(); // Get the sorting order
//            String sortBy = cmbSortBerdasarkan.getSelectionModel().getSelectedItem(); // Get the sorting criteria
//
//            int status = statusText.equalsIgnoreCase("Aktif") ? 1 : 0;
//            String search = searchText.isEmpty() ? null : searchText;
//            String kategori = (categoryText != null && !categoryText.equals("None")) ? categoryText : null;
//
//            loadDetailsToTable(search, kategori, status, urutan, sortBy);
//        }), 0, TimeUnit.MILLISECONDS);
//    }

    private void delaySearch() {
        if (searchTask != null && !searchTask.isDone()) {
            searchTask.cancel(true);
        }

        searchTask = searchExecutor.schedule(() -> {
            javafx.application.Platform.runLater(() -> {
                String currentSearchText = txtSearch.getText();
                Integer currentStatusFilter = null;
                String selectedStatus = cmbFilterStatus.getSelectionModel().getSelectedItem();
                String currentCategoryFilter = null;

                if (selectedStatus != null) {
                    currentStatusFilter = selectedStatus.equals("Aktif") ? 1 : 0;
                }

                if (cmbFilterCategory != null && cmbFilterCategory.getSelectionModel().getSelectedItem() != null && !cmbFilterCategory.getSelectionModel().getSelectedItem().equals("None")) {
                    currentCategoryFilter = cmbFilterCategory.getSelectionModel().getSelectedItem();
                }

                loadDetailsToTable(
                        currentSearchText.isEmpty() ? null : currentSearchText,
                        currentCategoryFilter,
                        currentStatusFilter
                );
            });
        }, 100, TimeUnit.MILLISECONDS);
    }


    private void loadDetailsToTable(String search, String category, Integer status) {
        vbRowTable.getChildren().clear();
        List<Setting> dtSettingList = settingImpl.getAllData(search, category, status);

        if (dtSettingList.isEmpty()) {
            vbRowTable.getChildren().add(new Label("Tidak ada data setting ditemukan."));
            return;
        }

        String FxmlPath = "/cinelux/bioskopcinelux/view/List/SettingList.fxml";

        for (Setting dtSetting : dtSettingList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(FxmlPath));
                Parent supplierRowNode = loader.load();

                SettingListCtrl listController = loader.getController();
                listController.setSettingsController(this);
                listController.setData(dtSetting);

                vbRowTable.getChildren().add(supplierRowNode);

            } catch (IOException e) {
                System.err.println("Gagal memuat SettingRow.fxml atau mengatur data: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }



//    private void loadDetailsToTable(String search, String kategori, Integer status, String urutan, String sortBy) {
//        vbRowTable.getChildren().clear();
//        List<Setting> dSettingList = settingImpl.getAllData(search, kategori, status, urutan, sortBy);
//
//        if (dSettingList.isEmpty()) {
//            vbRowTable.getChildren().add(new Label("Tidak ada data setting ditemukan."));
//            return;
//        }
//
//        String fxmlPath = "/cinelux/bioskopcinelux/view/List/SettingList.fxml";
//
//        for (Setting dSetting : dSettingList) {
//            try {
//                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
//                Parent node = loader.load();
//                SettingListCtrl listCtrl = loader.getController();
//                listCtrl.setData(dSetting);
//                listCtrl.setSettingsController(this);
//                vbRowTable.getChildren().add(node);
//            } catch (IOException e) {
//                System.err.println("Gagal memuat SettingList.fxml: " + e.getMessage());
//                e.printStackTrace();
//            }
//        }
//    }

    public void toogleStatusSetting(int id) {
        Setting st = settingImpl.getById(id);
        if (st == null) {
            msg.alertWarning("Data setting tidak ditemukan.");
            return;
        }

        String confirmMsg = st.getStatus() == 1
                ? "Apakah Anda yakin ingin menonaktifkan setting ini?"
                : "Apakah Anda yakin ingin mengaktifkan kembali setting ini?";

        if (msg.alertConfirm(confirmMsg)) {
            OperationResult result = settingImpl.deleteData(id);
            if (result.isSuccess()) {
                msg.alertInfo(result.getMessage());
                delaySearch(); // reload dengan filter yang sama
            } else {
                msg.alertError(result.getMessage());
            }
        }
    }

    public void loadDatatoForm(int id) {
        btnTambah.setVisible(false);
        btnUpdate.setVisible(true);

        try {
            Setting st = settingImpl.getById(id);

            if (st != null) {
                txtId.setText(String.valueOf(st.getId()));
                txtNama.setText(st.getNama());
                txtValue.setText(st.getValue());

                // Set kategori ke ComboBox
                if (cmbKategori.getItems().contains(st.getKategori())) {
                    cmbKategori.getSelectionModel().select(st.getKategori());
                } else {
                    cmbKategori.getSelectionModel().clearSelection(); // Clear jika tidak ada
                    // Anda bisa menambahkan logika untuk menampilkan pesan jika kategori tidak ditemukan
                    System.out.println("Kategori tidak ditemukan: " + st.getKategori());
                }
            } else {
                msg.alertError("Data Setting tidak ditemukan.");
            }
        } catch (Exception e) {
            msg.alertError("Terjadi kesalahan saat mengambil data: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private void insertData(){
        String usr;
        if (user.getName() == null) {
             usr = "Admin";
        }
        else {
             usr = user.getName();
        }
        Setting newDSetting = new Setting(txtNama.getText(), (txtValue.getText()), cmbKategori.getSelectionModel().getSelectedItem(),usr);

        OperationResult result = settingImpl.insertData(newDSetting);

        if (result.isSuccess()) {

            msg.alertInfo(result.getMessage());
            delaySearch();
        }else {
            msg.alertError(result.getMessage());
        }
        clearForm();
    }

    private void updateData() {
        // Validasi input
        if (txtNama.getText().trim().isEmpty()) {
            msg.alertWarning("Nama Setting tidak boleh kosong!");
            return;
        }
        if (cmbKategori.getSelectionModel().isEmpty()) {
            msg.alertWarning("Kategori harus dipilih!");
            return;
        }

        String usr;
        if (user.getName() == null || user.getName().isEmpty()) {
            usr = "Admin"; // fallback jika user tidak terdeteksi
        } else {
            usr = user.getName();
        }

        int settingId;
        try {
            settingId = Integer.parseInt(txtId.getText().trim());
        } catch (NumberFormatException e) {
            msg.alertError("ID Setting tidak valid.");
            return;
        }

        // Buat objek Setting baru dengan data dari form
        Setting updatedDSetting = new Setting(
                settingId, // Ambil ID dari field txtId
                txtNama.getText().trim(),
                txtValue.getText().trim(),
                cmbKategori.getSelectionModel().getSelectedItem(),
                usr // modified_by
        );

        // Panggil service untuk update
        OperationResult result = settingImpl.updateData(updatedDSetting);

        if (result.isSuccess()) {
            msg.alertInfo(result.getMessage());
            delaySearch(); // refresh data table setelah update
        } else {
            msg.alertError(result.getMessage());
        }

        clearForm(); // kosongkan form setelah update
    }


    private void clearForm(){
        txtNama.clear();
        txtValue.clear();
        cmbKategori.getSelectionModel().clearSelection();

        txtId.setText(""+(settingImpl.getLastId()+1));

        btnUpdate.setVisible(false);
        btnTambah.setVisible(true);
    }

    @FXML
    void handleBtnInsertClick(ActionEvent event) {
        insertData();
    }

    @FXML
    void handleBtnClearClick(ActionEvent event) {
        clearForm();
    }

    @FXML
    void handleBtnFilterClick(ActionEvent event) {
        vbFilter.setVisible(!vbFilter.isVisible());
    }

    @FXML
    void handleBtnUpdateClick(ActionEvent event) {
        updateData();
    }
}
