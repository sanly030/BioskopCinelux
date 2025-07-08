package cinelux.bioskopcinelux.controller.master;

import cinelux.bioskopcinelux.controller.list.PromoListCtrl;
import cinelux.bioskopcinelux.model.Role;
import cinelux.bioskopcinelux.model.Setting;
import cinelux.bioskopcinelux.util.OperationResult;
import cinelux.bioskopcinelux.model.Promo;
import cinelux.bioskopcinelux.service.impl.PromoImpl;
import cinelux.bioskopcinelux.util.MessageBox;
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

public class PromoCtrl implements Initializable {
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
    private ComboBox<String> cmbKategori;
    @FXML
    private ComboBox<String> cmbSortBerdasarkan;
    @FXML
    private ComboBox<String> cmbSortUrutan;
    @FXML
    private Button tnHapusFilter;
    @FXML
    private TextField txtId;
    @FXML
    private TextField txtNama;
    @FXML
    private TextField txtSearch;
    @FXML
    private TextField txtDiskon;
    @FXML
    private VBox vbRowTable;
    @FXML
    private DatePicker dpTanggalMulai;
    @FXML
    private DatePicker dpTanggalSelesai;

    private ScheduledExecutorService searchExecutor;
    private ScheduledFuture<?> searchTask;

    PromoImpl promoImpl = new PromoImpl();
    MessageBox msg = new MessageBox();
    Role user = new Role();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchExecutor = Executors.newSingleThreadScheduledExecutor();

        txtId.setText(String.valueOf(promoImpl.getLastId() + 1));
        // Batasi panjang teks
        txtNama.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.length() > 50) {
                txtNama.setText(oldText); // maksimal 50 karakter
            }
        });

        // Inisialisasi tanggal mulai ke hari ini
        dpTanggalMulai.setValue(java.time.LocalDate.now());

        // Validasi txtDiskon: hanya angka & 1 titik, otomatis tambahkan '%'
        txtDiskon.textProperty().addListener((obs, oldText, newText) -> {
            // Bersihkan input dari karakter yang bukan angka atau titik
            String cleaned = newText.replaceAll("[^\\d.]", "");

            // Pastikan hanya 1 titik desimal
            int dotIndex = cleaned.indexOf(".");
            if (dotIndex != -1) {
                String beforeDot = cleaned.substring(0, dotIndex + 1);
                String afterDot = cleaned.substring(dotIndex + 1).replace(".", ""); // hapus titik ganda
                cleaned = beforeDot + afterDot;
            }

            // Tambahkan % jika tidak kosong
            if (!cleaned.isEmpty()) {
                txtDiskon.setText(cleaned + "%");
            } else {
                txtDiskon.clear();
            }
        });

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> delaySearch());
        cmbFilterStatus.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());
        cmbFilterKategori.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());
        cmbSortBerdasarkan.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());
        cmbSortUrutan.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());

        cmbFilterStatus.getItems().addAll("Aktif", "Tidak Aktif");
        cmbFilterStatus.getSelectionModel().select("Aktif");
        cmbSortBerdasarkan.getItems().addAll("Nama", "Kategori");
        cmbSortUrutan.getItems().addAll("Menaik", "Menurun");
        cmbSortUrutan.getSelectionModel().select("Menurun");
        cmbKategori.getItems().addAll("Pemesanan Menu", "Pemesanan Tiket");

        loadDetailsToTable(null, null, null,null,null);
    }

    private void delaySearch() {
        if (searchTask != null && !searchTask.isDone()) {
            searchTask.cancel(true);
        }

        searchTask = searchExecutor.schedule(() -> javafx.application.Platform.runLater(() -> {
            String searchText = txtSearch.getText();
            String statusText = cmbFilterStatus.getSelectionModel().getSelectedItem();
            String categoryText = cmbFilterKategori.getSelectionModel().getSelectedItem();
            String urutan = cmbSortUrutan.getSelectionModel().getSelectedItem(); // Get the sorting order
            String sortBy = cmbSortBerdasarkan.getSelectionModel().getSelectedItem(); // Get the sorting criteria

            int status = statusText.equalsIgnoreCase("Aktif") ? 1 : 0;
            String search = searchText.isEmpty() ? null : searchText;
            String kategori = (categoryText != null && !categoryText.equals("None")) ? categoryText : null;

            loadDetailsToTable(search, kategori, status, urutan, sortBy);
        }), 0, TimeUnit.MILLISECONDS);
    }

    private void loadDetailsToTable(String search, String kategori, Integer status, String urutan, String sortBy) {
        vbRowTable.getChildren().clear();
        List<Promo> promoList = promoImpl.getAllData(search, kategori, status, urutan, sortBy);

        if (promoList.isEmpty()) {
            vbRowTable.getChildren().add(new Label("Tidak ada data promo ditemukan."));
            return;
        }

        String fxmlPath = "/cinelux/bioskopcinelux/view/List/PromoList.fxml";

        for (Promo promo : promoList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent node = loader.load();
                PromoListCtrl listCtrl = loader.getController();
                listCtrl.setData(promo);
                listCtrl.setPromoController(this);
                vbRowTable.getChildren().add(node);
            } catch (IOException e) {
                System.err.println("Gagal memuat PromoList.fxml: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void toogleStatusPromo(int id) {
        Promo promo = promoImpl.getById(id);
        if (promo == null) {
            msg.alertWarning("Data promo tidak ditemukan.");
            return;
        }

        String confirmMsg = promo.getStatus() == 1
                ? "Apakah Anda yakin ingin menonaktifkan promo ini?"
                : "Apakah Anda yakin ingin mengaktifkan kembali promo ini?";

        if (msg.alertConfirm(confirmMsg)) {
            OperationResult result = promoImpl.deleteData(id);
            if (result.isSuccess()) {
                msg.alertInfo(result.getMessage());
                delaySearch(); // reload data dengan filter yang sama
            } else {
                msg.alertError(result.getMessage());
            }
        }
    }

    public void loadDatatoForm(int id) {
        btnTambah.setVisible(false);
        btnUpdate.setVisible(true);

        try {
            Promo prm = promoImpl.getById(id);

            if (prm != null) {
                txtId.setText(String.valueOf(prm.getId()));
                txtNama.setText(prm.getNama_promo());

                // Pastikan txtDiskon diisi sebagai teks dari angka
                txtDiskon.setText(String.valueOf(prm.getDiskon()));

                // Set kategori ke ComboBox
                if (cmbKategori.getItems().contains(prm.getTipe_promo())) {
                    cmbKategori.getSelectionModel().select(prm.getTipe_promo());
                } else {
                    cmbKategori.getSelectionModel().clearSelection();
                    System.out.println("Kategori tidak ditemukan: " + prm.getTipe_promo());
                }

                // Set tanggal mulai dan tanggal selesai
                if (prm.getTanggal_mulai() != null) {
                    dpTanggalMulai.setValue(prm.getTanggal_mulai().toLocalDate());
                } else {
                    dpTanggalMulai.setValue(null);
                }

                if (prm.getTanggal_selesai() != null) {
                    dpTanggalSelesai.setValue(prm.getTanggal_selesai().toLocalDate());
                } else {
                    dpTanggalSelesai.setValue(null);
                }

            } else {
                msg.alertError("Data promo tidak ditemukan.");
            }
        } catch (Exception e) {
            msg.alertError("Terjadi kesalahan saat mengambil data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertData() {
        String usr = (user.getName() != null) ? user.getName() : "Admin";

        // Validasi input kosong
        if (txtNama.getText().isEmpty() || txtDiskon.getText().isEmpty()) {
            msg.alertWarning("Nama dan diskon harus diisi.");
            return;
        }

        if (cmbKategori.getSelectionModel().isEmpty()) {
            msg.alertWarning("Kategori harus dipilih.");
            return;
        }

        if (dpTanggalMulai.getValue() == null || dpTanggalSelesai.getValue() == null) {
            msg.alertWarning("Tanggal mulai dan tanggal selesai harus diisi.");
            return;
        }

        if (dpTanggalSelesai.getValue().isBefore(dpTanggalMulai.getValue())) {
            msg.alertWarning("Tanggal selesai tidak boleh lebih awal dari tanggal mulai.");
            return;
        }

        try {
            String nama = txtNama.getText().trim();
            String tipe = cmbKategori.getSelectionModel().getSelectedItem();
            double diskon = Double.parseDouble(txtDiskon.getText().replace("%", "").trim());

            java.sql.Date tanggalMulai = java.sql.Date.valueOf(dpTanggalMulai.getValue());
            java.sql.Date tanggalSelesai = java.sql.Date.valueOf(dpTanggalSelesai.getValue());

            Promo newPromo = new Promo(nama, tipe, diskon, tanggalMulai, tanggalSelesai, 1, usr);

            OperationResult result = promoImpl.insertData(newPromo);

            if (result.isSuccess()) {
                msg.alertInfo(result.getMessage());
                delaySearch();
                clearForm();
            } else {
                msg.alertError(result.getMessage());
            }
        } catch (NumberFormatException e) {
            msg.alertError("Diskon harus berupa angka desimal, contoh: 15 atau 10.5");
        } catch (Exception e) {
            msg.alertError("Terjadi kesalahan saat menyimpan data promo:\n" + e.getMessage());
            e.printStackTrace();
        }
    }


    private void updatePromo() {
        // Validasi field kosong
        if (txtNama.getText().trim().isEmpty()) {
            msg.alertWarning("Nama Promo tidak boleh kosong!");
            return;
        }

        if (txtDiskon.getText().trim().isEmpty()) {
            msg.alertWarning("Diskon harus diisi!");
            return;
        }

        if (cmbKategori.getSelectionModel().isEmpty()) {
            msg.alertWarning("Kategori harus dipilih!");
            return;
        }

        if (dpTanggalMulai.getValue() == null || dpTanggalSelesai.getValue() == null) {
            msg.alertWarning("Tanggal mulai dan tanggal selesai harus diisi!");
            return;
        }

        // Validasi tanggal: selesai tidak boleh sebelum mulai
        if (dpTanggalSelesai.getValue().isBefore(dpTanggalMulai.getValue())) {
            msg.alertWarning("Tanggal selesai tidak boleh lebih awal dari tanggal mulai!");
            return;
        }

        // Validasi diskon numerik
        double diskon;
        try {
            diskon = Double.parseDouble(txtDiskon.getText().trim());
        } catch (NumberFormatException e) {
            msg.alertError("Diskon harus berupa angka desimal, contoh: 10 atau 15.5");
            return;
        }

        // Validasi ID
        int promoId;
        try {
            promoId = Integer.parseInt(txtId.getText().trim());
        } catch (NumberFormatException e) {
            msg.alertError("ID promo tidak valid.");
            return;
        }

        // Dapatkan user
        String usr = (user.getName() != null && !user.getName().isEmpty()) ? user.getName() : "Admin";

        // Buat objek Promo baru
        Promo updatedPromo = new Promo(
                promoId,
                txtNama.getText().trim(),
                cmbKategori.getSelectionModel().getSelectedItem(),
                diskon,
                java.sql.Date.valueOf(dpTanggalMulai.getValue()),
                java.sql.Date.valueOf(dpTanggalSelesai.getValue()),
                1,
                usr // modifiedBy
        );

        // Panggil update
        OperationResult result = promoImpl.updateData(updatedPromo);

        if (result.isSuccess()) {
            msg.alertInfo(result.getMessage());
            delaySearch();
        } else {
            msg.alertError(result.getMessage());
        }

        clearForm();
    }


    private void clearForm(){
        txtNama.clear();
        txtDiskon.clear();
        cmbKategori.getSelectionModel().clearSelection();
        dpTanggalMulai.setValue(java.time.LocalDate.now());
        dpTanggalSelesai.setValue(null);
        txtId.setText(""+(promoImpl.getLastId()+1));

        btnUpdate.setVisible(false);
        btnTambah.setVisible(true);
    }


    @FXML
    void handleBtnClearClick(ActionEvent event) {
        clearForm();
    }

    @FXML
    void handleBtnInsertClick(ActionEvent event) {
        insertData();
    }

    @FXML
    void handleBtnUpdateClick(ActionEvent event) {
        updatePromo();
    }
}
