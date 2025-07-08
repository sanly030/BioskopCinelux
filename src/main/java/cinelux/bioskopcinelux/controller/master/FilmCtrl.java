package cinelux.bioskopcinelux.controller.master;

import cinelux.bioskopcinelux.controller.list.FilmListCtrl;
import cinelux.bioskopcinelux.model.Film;
import cinelux.bioskopcinelux.model.Role;
import cinelux.bioskopcinelux.service.impl.FilmImpl;
import cinelux.bioskopcinelux.connection.DBConnect;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FilmCtrl implements Initializable {
    @FXML private Button btnClear, btnFilter, btnPilihGambar, btnSortHapus, btnTambah, btnUpdate, btnUrutan;
    @FXML private ComboBox<String> cmbFilterStatus, cmbFilterKategori, cmbSortBerdasarkan, cmbSortUrutan, cmbGenre, cmbRating;
    @FXML private TextField txtId, txtJudul, txtDurasi, txtSearch;
    @FXML private ImageView imgPoster;
    @FXML private VBox vbRowTable;

    private ScheduledExecutorService searchExecutor;
    private ScheduledFuture<?> searchTask;
    DBConnect connect = new DBConnect();
    FilmImpl filmImpl = new FilmImpl();
    MessageBox msg = new MessageBox();
    Role user = new Role();

    private String currentPosterFileName = null; // [ADDED] menyimpan nama file gambar yang diinput

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

                    Image image = new Image(destination.toUri().toString());
                    imgPoster.setImage(image);
                    currentPosterFileName = selectedFile.getName(); // [ADDED] simpan nama file gambar

                } catch (IOException ex) {
                    msg.alertWarning("Import gagal: " + ex.getMessage());
                }
            }
        });

        searchExecutor = Executors.newSingleThreadScheduledExecutor();
        txtId.setText(String.valueOf(filmImpl.getLastId() + 1));

        cmbFilterStatus.getItems().addAll("Aktif", "Tidak Aktif");
        cmbFilterStatus.getSelectionModel().select("Aktif");
        cmbSortBerdasarkan.getItems().addAll("Judul", "Genre");
        cmbSortUrutan.getItems().addAll("Menaik", "Menurun");
        cmbSortUrutan.getSelectionModel().select("Menurun");

        cmbGenre.getItems().addAll("Aksi", "Drama", "Komedi", "Horror", "Sci-Fi");
        cmbRating.getItems().addAll("SU", "13+", "17+", "21+");

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
            String search = txtSearch.getText();
            String statusText = cmbFilterStatus.getValue();
            String categoryText = cmbFilterKategori.getValue();
            String urutan = cmbSortUrutan.getValue();
            String sortBy = cmbSortBerdasarkan.getValue();

            int status = statusText.equalsIgnoreCase("Aktif") ? 1 : 0;
            loadDetailsToTable(search.isEmpty() ? null : search, categoryText, status, urutan, sortBy);
        }), 0, TimeUnit.MILLISECONDS);
    }

    private void loadDetailsToTable(String search, String genre, Integer status, String urutan, String sortBy) {
        vbRowTable.getChildren().clear();
        List<Film> filmList = filmImpl.getAllData(search, genre, status, urutan, sortBy);

        if (filmList.isEmpty()) {
            vbRowTable.getChildren().add(new Label("Tidak ada data film ditemukan."));
            return;
        }

        for (Film film : filmList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/cinelux/bioskopcinelux/view/List/FilmList.fxml"));
                Parent node = loader.load();
                FilmListCtrl listCtrl = loader.getController();
                listCtrl.setFilm(film);
                listCtrl.setFilmController(this);
                vbRowTable.getChildren().add(node);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void toogleStatusFilm(int id, String modifiedBy) {
        Film film = filmImpl.getById(id);
        if (film == null) {
            msg.alertWarning("Data film tidak ditemukan.");
            return;
        }

        String currentUser = (user.getName() != null) ? user.getName() : "Admin";

        boolean confirm = msg.alertConfirm(
                film.getStatus() == 1 ? "Nonaktifkan film ini?" : "Aktifkan kembali film ini?"
        );

        if (confirm) {
            OperationResult result = filmImpl.deleteData(id, currentUser);
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

        Film film = filmImpl.getById(id);
        if (film != null) {
            txtId.setText(String.valueOf(film.getId()));
            txtJudul.setText(film.getJudul());
            txtDurasi.setText(String.valueOf(film.getDurasi()));
            cmbGenre.getSelectionModel().select(film.getGenre());
            cmbRating.getSelectionModel().select(film.getRating_usia());

            loadImageToImageView(film.getPoster()); // [UPDATED]
            currentPosterFileName = film.getPoster(); // [ADDED] simpan nama file gambar yang sedang diedit
        } else {
            msg.alertError("Data film tidak ditemukan.");
        }
    }

    private void loadImageToImageView(String posterPath) {
        try {
            if (posterPath != null && !posterPath.trim().isEmpty()) {
                File imageFile = new File("src/main/resources/images/" + posterPath);
                if (imageFile.exists()) {
                    imgPoster.setImage(new Image(imageFile.toURI().toString()));
                } else {
                    setDefaultImage();
                }
            } else {
                setDefaultImage();
            }
        } catch (Exception e) {
            setDefaultImage();
        }
    }

    private void setDefaultImage() {
        try {
            Image placeholder = new Image(getClass().getResourceAsStream("/images/no-image-placeholder.png"));
            imgPoster.setImage(placeholder);
        } catch (Exception e) {
            imgPoster.setImage(null);
        }
    }

    private void insertFilm() {
        if (txtJudul.getText().trim().isEmpty() || txtDurasi.getText().trim().isEmpty() ||
                cmbGenre.getSelectionModel().isEmpty() || cmbRating.getSelectionModel().isEmpty()) {
            msg.alertWarning("Lengkapi semua data!");
            return;
        }

        int durasi;
        try {
            durasi = Integer.parseInt(txtDurasi.getText().trim());
            if (durasi <= 60) {
                msg.alertWarning("Durasi harus lebih dari 60 menit.");
                return;
            }
        } catch (NumberFormatException e) {
            msg.alertError("Durasi harus angka.");
            return;
        }

        Film newFilm = new Film(
                0,
                txtJudul.getText().trim(),
                cmbGenre.getValue(),
                durasi,
                cmbRating.getValue(),
                currentPosterFileName, // [UPDATED] simpan nama file poster
                1,
                user.getName() != null ? user.getName() : "Admin",
                user.getName() != null ? user.getName() : "Admin"
        );

        OperationResult result = filmImpl.insertData(newFilm);
        if (result.isSuccess()) {
            msg.alertInfo(result.getMessage());
            delaySearch();
            clearForm();
        } else {
            msg.alertError(result.getMessage());
        }
    }

    private void updateFilm() {
        if (txtJudul.getText().trim().isEmpty() || txtDurasi.getText().trim().isEmpty() ||
                cmbGenre.getSelectionModel().isEmpty() || cmbRating.getSelectionModel().isEmpty()) {
            msg.alertWarning("Lengkapi semua data!");
            return;
        }

        int id = Integer.parseInt(txtId.getText().trim());
        int durasi;
        try {
            durasi = Integer.parseInt(txtDurasi.getText().trim());
        } catch (NumberFormatException e) {
            msg.alertError("Durasi tidak valid.");
            return;
        }

        Film updatedFilm = new Film(
                id,
                txtJudul.getText().trim(),
                cmbGenre.getValue(),
                durasi,
                cmbRating.getValue(),
                currentPosterFileName, // [UPDATED]
                1,
                null,
                user.getName() != null ? user.getName() : "Admin"
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

    private void clearForm() {
        txtJudul.clear();
        txtDurasi.clear();
        cmbRating.getSelectionModel().clearSelection();
        cmbGenre.getSelectionModel().clearSelection();
        imgPoster.setImage(null);
        currentPosterFileName = null; // [ADDED] reset gambar
        txtId.setText(String.valueOf(filmImpl.getLastId() + 1));
        btnUpdate.setVisible(false);
        btnTambah.setVisible(true);
    }

    @FXML void handleBtnClearClick(ActionEvent event) {
        clearForm();
    }

    @FXML void handleBtnInsertClick(ActionEvent event) {
        insertFilm();
    }

    @FXML void handleBtnUpdateClick(ActionEvent event) {
        updateFilm();
    }
}
