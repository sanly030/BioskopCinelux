package cinelux.bioskopcinelux.controller.list;

import cinelux.bioskopcinelux.controller.master.FilmCtrl;
import cinelux.bioskopcinelux.model.Film;
import cinelux.bioskopcinelux.model.Role;
import cinelux.bioskopcinelux.util.MessageBox;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;

public class FilmListCtrl {
    @FXML
    private Tooltip Information;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnInformation;
    @FXML
    private Button btnUpdate;
    @FXML
    private ImageView imgStatus;
    @FXML
    private Label lbDurasi;
    @FXML
    private Label lbGenre;
    @FXML
    private Label lbNama;
    @FXML
    private Label lbRating;
    @FXML
    private Tooltip toolTipDelete;

    private Film film;
    private FilmCtrl filmsCtrl;
    Role user = new Role();

    public void setFilmController(FilmCtrl controller) {
        this.filmsCtrl = controller;
    }

    public void setFilm(Film film) {
        this.film = film;
        lbNama.setText(film.getJudul());
        lbGenre.setText(film.getGenre());
        lbDurasi.setText(String.valueOf(film.getDurasi()));
        lbRating.setText(film.getRating_usia());

        int status = film.getStatus();

        String imgPath;
        if ( status == 1) {
            imgPath = "/cinelux/bioskopcinelux/asset/image/icon_remove.png";
            toolTipDelete.setText("Hapus");
        }else {
            imgPath = "/cinelux/bioskopcinelux/asset/image/icon_update.png";
        }
        setImage(imgPath);
    }

    private void setImage(String imagePath) {
        try (InputStream imageStream = getClass().getResourceAsStream(imagePath)) {
            if (imageStream != null) {
                Image image = new Image(imageStream);
                imgStatus.setImage(image);
            } else {
                System.err.println("Gagal memuat gambar: " + imagePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnDeleteClick(ActionEvent event) {
        int id = film.getId();

        String modifiedBy = (user.getName() != null) ? user.getName() : "Admin"; // ✅ Ambil user yang aktif

        if (filmsCtrl != null) {
            filmsCtrl.toogleStatusFilm(id, modifiedBy); // ✅ Kirim ID dan user
        }
    }


    @FXML
    void btnUpdateClick(ActionEvent event) {
        if (film.getStatus() == 0) {
            MessageBox msg = new MessageBox();
            msg.alertWarning("Tidak bisa mengedit data yang tidak aktif. Silakan aktifkan kembali terlebih dahulu.");
            return;
        }

        if (filmsCtrl != null && film != null) {
            filmsCtrl.loadDatatoForm(film.getId());
        }

    }

}
