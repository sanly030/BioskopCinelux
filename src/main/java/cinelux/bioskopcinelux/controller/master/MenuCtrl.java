package cinelux.bioskopcinelux.controller.master;

import cinelux.bioskopcinelux.controller.list.MenuListCtrl;
import cinelux.bioskopcinelux.model.Menu;
import cinelux.bioskopcinelux.model.Role;
import cinelux.bioskopcinelux.model.Setting;
import cinelux.bioskopcinelux.service.impl.MenuImpl;
import cinelux.bioskopcinelux.service.impl.SettingImpl;
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
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.UnaryOperator;

public class MenuCtrl implements Initializable {
    @FXML
    private Button btnClear, btnFilter, btnSortHapus, btnTambah, btnUpdate, btnUrutan, tnHapusFilter;
    @FXML
    private ComboBox<String> cmbFilterKategori, cmbFilterStatus, cmbKategori, cmbSortBerdasarkan, cmbSortUrutan;
    @FXML
    private TextField txtHarga, txtId, txtNama, txtSearch, txtStok;
    @FXML
    private VBox vbRowTable;

    private ScheduledExecutorService searchExecutor;
    private ScheduledFuture<?> searchTask;
    private final MenuImpl menuImpl = new MenuImpl();
    private final SettingImpl settingImpl = new SettingImpl();
    private final MessageBox msg = new MessageBox();
    private final Role user = new Role();
    private List<Setting> menuList = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchExecutor = Executors.newSingleThreadScheduledExecutor();

        txtId.setText(String.valueOf(menuImpl.getLastId() + 1));

        txtNama.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.length() > 50 || !newText.matches("[a-zA-Z ]*")) return null;
            return change;
        }));

        // Formatter angka ke format Rupiah (tanpa pecahan)
        NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        rupiahFormat.setMaximumFractionDigits(0);

// TextFormatter untuk memformat input menjadi Rupiah
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

// Buat TextFormatter dengan filter
        TextFormatter<String> formatter = new TextFormatter<>(filter);
        txtHarga.setTextFormatter(formatter);


        // Listener filter pencarian
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
        cmbFilterKategori.setItems(FXCollections.observableArrayList("Makanan", "Minuman"));

        loadJenisMakananComboBox();
        loadDetailsToTable(null, null, 1, "DESC", "Nama");
    }


    private void loadJenisMakananComboBox() {
        menuList = settingImpl.getAllData(null, "Jenis Makanan", 1, null, null);
        cmbKategori.getItems().clear();
        for (Setting jenis : menuList) {
            cmbKategori.getItems().add(jenis.getNama());
        }
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
        List<Menu> dataMenu = menuImpl.getAllData(search, kategori, status, urutan, sortBy);

        if (dataMenu.isEmpty()) {
            vbRowTable.getChildren().add(new Label("Tidak ada data ditemukan."));
            return;
        }

        String fxmlPath = "/cinelux/bioskopcinelux/view/List/MenuList.fxml";
        for (Menu menu : dataMenu) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent node = loader.load();
                MenuListCtrl listCtrl = loader.getController();
                listCtrl.setData(menu);
                listCtrl.setMenusController(this);
                vbRowTable.getChildren().add(node);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Gagal load MenuList.fxml: " + e.getMessage());
            }
        }
    }

    @FXML
    void handleBtnInsertClick(ActionEvent event) {
        if (txtNama.getText().trim().isEmpty() || txtHarga.getText().trim().isEmpty() || txtStok.getText().trim().isEmpty()) {
            msg.alertWarning("Nama, Harga, dan Stok tidak boleh kosong.");
            return;
        }

        String kategoriNama = cmbKategori.getSelectionModel().getSelectedItem();
        Setting jenisMakanan = menuList.stream()
                .filter(k -> k.getNama().equals(kategoriNama))
                .findFirst()
                .orElse(null);

        if (jenisMakanan == null) {
            msg.alertWarning("Pilih jenis makanan terlebih dahulu.");
            return;
        }

        double harga;
        int stok;
        try {
            String hargaText = txtHarga.getText().replaceAll("[^\\d]", "");
            harga = Double.parseDouble(hargaText);
            stok = Integer.parseInt(txtStok.getText());
        } catch (NumberFormatException e) {
            msg.alertError("Harga atau stok tidak valid.");
            return;
        }

        String usr = (user.getName() == null) ? "Admin" : user.getName();

        Menu newMenu = new Menu(null, jenisMakanan, txtNama.getText(), stok, harga, 1, usr);
        OperationResult result = menuImpl.insertData(newMenu);

        if (result.isSuccess()) {
            msg.alertInfo(result.getMessage());
            delaySearch();
        } else {
            msg.alertError(result.getMessage());
        }

        clearForm();
    }

    @FXML
    void handleBtnUpdateClick(ActionEvent event) {
        if (txtNama.getText().trim().isEmpty() || txtHarga.getText().trim().isEmpty() || txtStok.getText().trim().isEmpty()) {
            msg.alertWarning("Nama, Harga, dan Stok tidak boleh kosong.");
            return;
        }

        int id = Integer.parseInt(txtId.getText());

        String kategoriNama = cmbKategori.getSelectionModel().getSelectedItem();
        Setting jenisMakanan = menuList.stream()
                .filter(k -> k.getNama().equals(kategoriNama))
                .findFirst()
                .orElse(null);

        if (jenisMakanan == null) {
            msg.alertWarning("Jenis makanan belum dipilih.");
            return;
        }

        double harga;
        int stok;
        try {
            String hargaText = txtHarga.getText().replaceAll("[^\\d]", "");
            harga = Double.parseDouble(hargaText);
            stok = Integer.parseInt(txtStok.getText());
        } catch (NumberFormatException e) {
            msg.alertError("Harga atau stok tidak valid.");
            return;
        }

        String usr = (user.getName() == null) ? "Admin" : user.getName();

        Menu updatedMenu = new Menu(id, jenisMakanan, txtNama.getText(), stok, harga, 1, usr);
        OperationResult result = menuImpl.updateData(updatedMenu);

        if (result.isSuccess()) {
            msg.alertInfo(result.getMessage());
            delaySearch();
        } else {
            msg.alertError(result.getMessage());
        }

        clearForm();
    }


    @FXML
    void handleBtnClearClick(ActionEvent event) {
        clearForm();
    }

    private void clearForm() {
        txtNama.clear();
        txtHarga.clear();
        txtStok.clear();
        cmbKategori.getSelectionModel().clearSelection();
        txtId.setText(String.valueOf(menuImpl.getLastId() + 1));
        btnUpdate.setVisible(false);
        btnTambah.setVisible(true);
    }

    public void loadDataToForm(int id) {
        Menu menu = menuImpl.getById(id);
        if (menu == null) {
            msg.alertError("Data menu tidak ditemukan.");
            return;
        }

        txtId.setText(String.valueOf(menu.getId()));
        txtNama.setText(menu.getNama());

        NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        rupiahFormat.setMaximumFractionDigits(0);
        String formattedHarga = rupiahFormat.format(menu.getHarga());
        txtHarga.setText(formattedHarga);

        txtStok.setText(String.valueOf(menu.getStok())); // Tambahkan stok

        Setting jenis = menu.getJenis_makanan();
        if (jenis != null && cmbKategori.getItems().contains(jenis.getNama())) {
            cmbKategori.getSelectionModel().select(jenis.getNama());
        } else {
            cmbKategori.getSelectionModel().clearSelection();
        }

        btnTambah.setVisible(false);
        btnUpdate.setVisible(true);
    }

    public void toogleStatusMenu(int id) {
        Menu menu = menuImpl.getById(id);
        if (menu == null) {
            msg.alertWarning("Data tidak ditemukan.");
            return;
        }

        String confirmMsg = (menu.getStatus() == 1) ? "Nonaktifkan menu ini?" : "Aktifkan kembali menu ini?";
        if (msg.alertConfirm(confirmMsg)) {
            String usr = user != null && user.getName() != null && !user.getName().isEmpty()
                    ? user.getName()
                    : "Admin";
            OperationResult result = menuImpl.deleteData(id, usr);
            if (result.isSuccess()) {
                msg.alertInfo(result.getMessage());
                delaySearch();
            } else {
                msg.alertError(result.getMessage());
            }
        }
    }
}
