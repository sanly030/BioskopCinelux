package cinelux.bioskopcinelux.controller.master;

import cinelux.bioskopcinelux.controller.list.FilmListCtrl;
import cinelux.bioskopcinelux.model.Film;
import cinelux.bioskopcinelux.model.Role;
import cinelux.bioskopcinelux.service.impl.FilmImpl;
import cinelux.bioskopcinelux.service.impl.PromoImpl;
import cinelux.bioskopcinelux.util.MessageBox;
import cinelux.bioskopcinelux.util.OperationResult;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FilmCtrl implements Initializable {
    @FXML
    private Button btnClear, btnFilter, btnPilihGambar, btnSortHapus, btnTambah, btnUpdate, btnUrutan;
    @FXML
    private ComboBox<String> cmbFilterStatus, cmbFilterKategori,cmbSortBerdasarkan, cmbSortUrutan, cmbGenre, cmbRating;
    @FXML
    private TextField txtId, txtJudul, txtDurasi, txtSearch;
    @FXML
    private ImageView imgPoster;

    @FXML
    private VBox vbRowTable;

    private ScheduledExecutorService searchExecutor;
    private ScheduledFuture<?> searchTask;

    FilmImpl filmImpl = new FilmImpl();
    MessageBox msg = new MessageBox();
    Role user = new Role();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchExecutor = Executors.newSingleThreadScheduledExecutor();
        txtId.setText(String.valueOf(filmImpl.getLastId() + 1));

        cmbFilterStatus.getItems().addAll("Aktif", "Tidak Aktif");
        cmbFilterStatus.getSelectionModel().select("Aktif");
        cmbSortBerdasarkan.getItems().addAll("Judul", "Genre");
        cmbSortUrutan.getItems().addAll("Menaik", "Menurun");
        cmbSortUrutan.getSelectionModel().select("Menurun");

        cmbGenre.getItems().addAll("Aksi", "Drama", "Komedi", "Horror", "Sci-Fi");
        cmbRating.getItems().addAll(
                "G - General Audiences",
                "PG - Parental Guidance Suggested",
                "PG-13 - Parents Strongly Cautioned",
                "R - Restricted",
                "NC-17 - Adults Only"
        );


        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> delaySearch());
        cmbFilterStatus.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());
        cmbSortBerdasarkan.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());
        cmbSortUrutan.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());
        cmbFilterKategori.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> delaySearch());

        loadDetailsToTable(null, null, null, null, null);
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

    private void loadDetailsToTable(String search, String genre, Integer status, String urutan, String sortBy) {
        vbRowTable.getChildren().clear();
        List<Film> filmList = filmImpl.getAllData(search, genre, status, urutan, sortBy);

        if (filmList.isEmpty()) {
            vbRowTable.getChildren().add(new Label("Tidak ada data film ditemukan."));
            return;
        }

        String fxmlPath = "/cinelux/bioskopcinelux/view/List/FilmList.fxml"; // Pastikan path ini sesuai

        for (Film film : filmList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent node = loader.load();
                FilmListCtrl listCtrl = loader.getController();
                listCtrl.setFilm(film);
                listCtrl.setFilmController(this); // Agar bisa panggil delete/edit
                vbRowTable.getChildren().add(node);
            } catch (IOException e) {
                System.err.println("Gagal memuat FilmList.fxml: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void toogleStatusFilm(int id) {
        Film film = filmImpl.getById(id);
        if (film == null) {
            msg.alertWarning("Data film tidak ditemukan.");
            return;
        }

        String confirmMsg = film.getStatus() == 1
                ? "Apakah Anda yakin ingin menonaktifkan film ini?"
                : "Apakah Anda yakin ingin mengaktifkan kembali film ini?";

        if (msg.alertConfirm(confirmMsg)) {
            OperationResult result = filmImpl.toogleStatus(id);
            if (result.isSuccess()) {
                msg.alertInfo(result.getMessage());
                delaySearch(); // reload data film
            } else {
                msg.alertError(result.getMessage());
            }
        }
    }

    public void loadDatatoForm(int id) {
        btnTambah.setVisible(false);
        btnUpdate.setVisible(true);

        try {
            Film film = filmImpl.getById(id);

            if (film != null) {
                txtId.setText(String.valueOf(film.getId()));
                txtJudul.setText(film.getJudul());
                txtDurasi.setText(film.getDurasi());

                // Set genre ke ComboBox
                if (cmbGenre.getItems().contains(film.getGenre())) {
                    cmbGenre.getSelectionModel().select(film.getGenre());
                } else {
                    cmbGenre.getSelectionModel().clearSelection();
                    System.out.println("Genre tidak ditemukan: " + film.getGenre());
                }

                // Set rating usia ke ComboBox
                if (cmbRating.getItems().contains(film.getRating_usia())) {
                    cmbRating.getSelectionModel().select(film.getRating_usia());
                } else {
                    cmbRating.getSelectionModel().clearSelection();
                    System.out.println("Rating usia tidak ditemukan: " + film.getRating_usia());
                }

                // Kalau kamu punya input poster, bisa juga ditambahkan
                // txtPoster.setText(film.getPoster());

            } else {
                msg.alertError("Data film tidak ditemukan.");
            }
        } catch (Exception e) {
            msg.alertError("Terjadi kesalahan saat mengambil data film:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertFilm() {
        String usr = (user.getName() != null) ? user.getName() : "Admin";

        // Validasi input kosong
        if (txtJudul.getText().trim().isEmpty()) {
            msg.alertWarning("Judul film tidak boleh kosong.");
            return;
        }

        if (txtDurasi.getText().trim().isEmpty()) {
            msg.alertWarning("Durasi film tidak boleh kosong.");
            return;
        }

        if (cmbGenre.getSelectionModel().isEmpty()) {
            msg.alertWarning("Genre film harus dipilih.");
            return;
        }

        if (cmbRating.getSelectionModel().isEmpty()) {
            msg.alertWarning("Rating usia harus dipilih.");
            return;
        }

        // Validasi angka durasi
        int durasi;
        try {
            durasi = Integer.parseInt(txtDurasi.getText().trim());
            if (durasi <= 0) {
                msg.alertWarning("Durasi harus lebih dari 0 menit.");
                return;
            }
        } catch (NumberFormatException e) {
            msg.alertError("Durasi harus berupa angka (dalam menit).");
            return;
        }

        try {
            // Ambil nilai dari inputan
            String judul = txtJudul.getText().trim();
            String genre = cmbGenre.getSelectionModel().getSelectedItem();
            String ratingUsia = cmbRating.getSelectionModel().getSelectedItem();

            // Jika ada field poster (optional)
            String poster = null; // bisa tambahkan logic upload file kalau dibutuhkan

            // Buat objek Film
            Film newFilm = new Film(judul, genre, String.valueOf(durasi), ratingUsia, poster, 1, usr);

            OperationResult result = filmImpl.insertData(newFilm);

            if (result.isSuccess()) {
                msg.alertInfo(result.getMessage());
                delaySearch();
                clearForm(); // buat method ini jika belum ada
            } else {
                msg.alertError(result.getMessage());
            }

        } catch (Exception e) {
            msg.alertError("Terjadi kesalahan saat menyimpan data film:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateFilm() {
        // Validasi input kosong
        if (txtJudul.getText().trim().isEmpty()) {
            msg.alertWarning("Judul film tidak boleh kosong!");
            return;
        }

        if (txtDurasi.getText().trim().isEmpty()) {
            msg.alertWarning("Durasi film tidak boleh kosong!");
            return;
        }

        if (cmbGenre.getSelectionModel().isEmpty()) {
            msg.alertWarning("Genre film harus dipilih!");
            return;
        }

        if (cmbRating.getSelectionModel().isEmpty()) {
            msg.alertWarning("Rating usia harus dipilih!");
            return;
        }

        // Validasi durasi numerik
        int durasi;
        try {
            durasi = Integer.parseInt(txtDurasi.getText().trim());
            if (durasi <= 0) {
                msg.alertWarning("Durasi film harus lebih dari 0 menit.");
                return;
            }
        } catch (NumberFormatException e) {
            msg.alertError("Durasi harus berupa angka (dalam menit).");
            return;
        }

        // Validasi ID
        int filmId;
        try {
            filmId = Integer.parseInt(txtId.getText().trim());
        } catch (NumberFormatException e) {
            msg.alertError("ID film tidak valid.");
            return;
        }

        // Ambil data inputan
        String judul = txtJudul.getText().trim();
        String genre = cmbGenre.getSelectionModel().getSelectedItem();
        String rating = cmbRating.getSelectionModel().getSelectedItem();
        String poster = null; // Atur jika kamu sudah memiliki mekanisme upload gambar

        String usr = (user.getName() != null && !user.getName().isEmpty()) ? user.getName() : "Admin";

        // Buat objek film
        Film updatedFilm = new Film(
                filmId,
                judul,
                genre,
                String.valueOf(durasi),
                rating,
                poster,
                1, // status aktif
                null,
                usr // modifiedBy
        );

        OperationResult result = filmImpl.updateData(updatedFilm);

        if (result.isSuccess()) {
            msg.alertInfo(result.getMessage());
            delaySearch();
        } else {
            msg.alertError(result.getMessage());
        }

        clearForm();
    }



    private void clearForm(){
        txtJudul.clear();
        txtDurasi.clear();
        cmbRating.getSelectionModel().clearSelection();
        cmbGenre.getSelectionModel().clearSelection();
        txtId.setText(""+(filmImpl.getLastId()+1));

        btnUpdate.setVisible(false);
        btnTambah.setVisible(true);
    }


    @FXML
    void handleBtnClearClick(ActionEvent event) {

    }

    @FXML
    void handleBtnInsertClick(ActionEvent event) {

    }

    @FXML
    void handleBtnUpdateClick(ActionEvent event) {

    }

}
