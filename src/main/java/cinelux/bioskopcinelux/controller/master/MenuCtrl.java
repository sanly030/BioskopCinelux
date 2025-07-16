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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.UnaryOperator;

public class MenuCtrl implements Initializable {
    @FXML private Button btnClear, btnFilter, btnSortHapus, btnTambah, btnUpdate, btnUrutan, tnHapusFilter, btnPilihGambar;
    @FXML private ComboBox<String> cmbFilterKategori, cmbFilterStatus, cmbKategori, cmbSortBerdasarkan, cmbSortUrutan;
    @FXML private TextField txtHarga, txtId, txtNama, txtSearch, txtStok;
    @FXML private VBox vbRowTable;
    @FXML private ImageView imgMenu;

    private ScheduledExecutorService searchExecutor;
    private ScheduledFuture<?> searchTask;
    private final MenuImpl menuImpl = new MenuImpl();
    private final SettingImpl settingImpl = new SettingImpl();
    private final MessageBox msg = new MessageBox();
    private final Role user = new Role();
    private List<Setting> menuList = new ArrayList<>();

    private String currentImageFileName = null; // ✅ Menyimpan nama file gambar yang dipilih

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchExecutor = Executors.newSingleThreadScheduledExecutor();

        txtId.setText(String.valueOf(menuImpl.getLastId() + 1));

        txtNama.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.length() > 50 || !newText.matches("[a-zA-Z ]*")) return null;
            return change;
        }));

        txtStok.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            return newText.matches("\\d*") ? change : null;
        }));

        txtHarga.setText("Rp ");

        UnaryOperator<TextFormatter.Change> hargaFormatter = change -> {
            String currentText = txtHarga.getText();

            // Preserve "Rp " prefix
            String newText = change.getControlNewText().replace("Rp ", "").replaceAll("[^\\d]", "");

            if (newText.isEmpty()) {
                change.setText("Rp ");
                change.setRange(0, currentText.length());
                change.setCaretPosition(3); // after "Rp "
                change.setAnchor(3);
                return change;
            }

            try {
                long value = Long.parseLong(newText);
                NumberFormat formatter = NumberFormat.getInstance(new Locale("in", "ID"));
                formatter.setMaximumFractionDigits(0);

                String formatted = "Rp " + formatter.format(value);
                change.setText(formatted);
                change.setRange(0, currentText.length());
                change.setCaretPosition(formatted.length());
                change.setAnchor(formatted.length());
                return change;

            } catch (NumberFormatException e) {
                return null;
            }
        };

        txtHarga.setTextFormatter(new TextFormatter<>(hargaFormatter));

        cmbFilterStatus.setItems(FXCollections.observableArrayList("Aktif", "Tidak Aktif"));
        cmbFilterStatus.getSelectionModel().select("Aktif");
        cmbSortBerdasarkan.setItems(FXCollections.observableArrayList("Nama", "Kategori"));
        cmbSortUrutan.setItems(FXCollections.observableArrayList("Menaik", "Menurun"));
        cmbSortUrutan.getSelectionModel().select("Menurun");
        cmbFilterKategori.setItems(FXCollections.observableArrayList("Makanan", "Minuman"));

        loadJenisMakananComboBox();
        loadDetailsToTable(null, null, 1, "DESC", "Nama");

        // ✅ Event untuk memilih gambar
        btnPilihGambar.setOnAction(e -> handleBtnPilihGambar());
    }

    private void handleBtnPilihGambar() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Gambar", "*.png", "*.jpg", "*.jpeg")
        );

        File selected = chooser.showOpenDialog(btnPilihGambar.getScene().getWindow());
        if (selected != null) {
            try {
                Path dest = Paths.get("src/main/resources/images/Menu", selected.getName());
                Files.createDirectories(dest.getParent());
                Files.copy(selected.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

                currentImageFileName = selected.getName(); // ✅ simpan nama gambar
                imgMenu.setImage(new Image(dest.toUri().toString()));
            } catch (IOException ex) {
                msg.alertWarning("Gagal simpan gambar: " + ex.getMessage());
            }
        }
    }

    private void loadJenisMakananComboBox() {
        menuList = settingImpl.getAllData(null, "Jenis Makanan", 1);
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

        for (Menu menu : dataMenu) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/cinelux/bioskopcinelux/view/List/MenuList.fxml"));
                Parent node = loader.load();
                MenuListCtrl listCtrl = loader.getController();
                listCtrl.setData(menu);
                listCtrl.setMenusController(this);
                vbRowTable.getChildren().add(node);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void handleBtnInsertClick(ActionEvent event) {
        if (txtNama.getText().isEmpty() || txtHarga.getText().isEmpty() || txtStok.getText().isEmpty()) {
            msg.alertWarning("Nama, Harga, dan Stok tidak boleh kosong.");
            return;
        }

        Setting jenisMakanan = getSelectedJenisMakanan();
        if (jenisMakanan == null) {
            msg.alertWarning("Pilih jenis makanan.");
            return;
        }

        double harga = parseHarga();
        int stok = parseStok();
        if (harga < 0 || stok < 0) return;

        String usr = (user.getName() == null) ? "Admin" : user.getName();

        // ✅ Menyertakan gambar ke dalam objek Menu
        Menu newMenu =new Menu(null, jenisMakanan, txtNama.getText(), stok, harga, 1, currentImageFileName, usr);
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
        if (txtNama.getText().isEmpty() || txtHarga.getText().isEmpty() || txtStok.getText().isEmpty()) {
            msg.alertWarning("Data tidak lengkap.");
            return;
        }

        int id = Integer.parseInt(txtId.getText());
        Setting jenisMakanan = getSelectedJenisMakanan();
        if (jenisMakanan == null) {
            msg.alertWarning("Jenis makanan belum dipilih.");
            return;
        }

        double harga = parseHarga();
        int stok = parseStok();
        if (harga < 0 || stok < 0) return;

        String usr = (user.getName() == null) ? "Admin" : user.getName();

        // ✅ Menyertakan gambar saat update
        Menu updated = new Menu(id, jenisMakanan, txtNama.getText(), stok, harga, 1, currentImageFileName, usr);
        OperationResult result = menuImpl.updateData(updated);

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
        imgMenu.setImage(null); // ✅ reset image preview
        currentImageFileName = null;
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
        txtHarga.setText(rupiahFormat.format(menu.getHarga()));

        txtStok.setText(String.valueOf(menu.getStok()));
        cmbKategori.getSelectionModel().select(menu.getJenis_makanan().getNama());

        currentImageFileName = menu.getGambar(); // ✅ simpan nama gambar
        loadImageToImageView(currentImageFileName);

        btnTambah.setVisible(false);
        btnUpdate.setVisible(true);
    }

    private void loadImageToImageView(String fileName) {
        if (fileName != null && !fileName.trim().isEmpty()) {
            File imageFile = new File("src/main/resources/images/Menu/" + fileName);
            if (imageFile.exists()) {
                imgMenu.setImage(new Image(imageFile.toURI().toString()));
            } else {
                imgMenu.setImage(null);
            }
        } else {
            imgMenu.setImage(null);
        }
    }

    private Setting getSelectedJenisMakanan() {
        String kategoriNama = cmbKategori.getSelectionModel().getSelectedItem();
        return menuList.stream()
                .filter(k -> k.getNama().equals(kategoriNama))
                .findFirst()
                .orElse(null);
    }

    private double parseHarga() {
        try {
            return Double.parseDouble(txtHarga.getText().replaceAll("[^\\d]", ""));
        } catch (NumberFormatException e) {
            msg.alertError("Harga tidak valid.");
            return -1;
        }
    }

    private int parseStok() {
        try {
            return Integer.parseInt(txtStok.getText());
        } catch (NumberFormatException e) {
            msg.alertError("Stok tidak valid.");
            return -1;
        }
    }

    public void toogleStatusMenu(int id) {
        Menu menu = menuImpl.getById(id);
        if (menu == null) {
            msg.alertWarning("Data tidak ditemukan.");
            return;
        }

        if (msg.alertConfirm((menu.getStatus() == 1) ? "Nonaktifkan menu ini?" : "Aktifkan kembali menu ini?")) {
            String usr = (user.getName() != null) ? user.getName() : "Admin";
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
