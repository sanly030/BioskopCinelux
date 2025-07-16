package cinelux.bioskopcinelux.controller.login;

import cinelux.bioskopcinelux.model.Setting;
import cinelux.bioskopcinelux.util.MessageBox;
import cinelux.bioskopcinelux.util.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import cinelux.bioskopcinelux.connection.DBConnect;
import cinelux.bioskopcinelux.model.Role;

import cinelux.bioskopcinelux.model.Karyawan;
import cinelux.bioskopcinelux.util.Feature;
import cinelux.bioskopcinelux.util.SwapPage;
import java.sql.*;

/*================================================================================================================================================*/
public class LoginCtrl {
    @FXML
    private AnchorPane rootPane;

    @FXML
    private Button btnMasuk;

    @FXML
    private ImageView eyeIcon;

    @FXML
    private PasswordField tfHidePassword;

    @FXML
    private TextField tfUsername;

    @FXML
    private TextField tfUnhidePassword;
    /*================================================================================================================================================*/    private boolean passwordVisible = false;
    DBConnect connect = new DBConnect();
    MessageBox msg = new MessageBox();
    /*================================================================================================================================================*/
    @FXML
    public void initialize() {
        Feature.limitTextLength(tfUsername, 15);
        Feature.limitTextLength(tfUnhidePassword, 15);
        Feature.limitTextLength(tfHidePassword, 15);

        // Initial field states
        tfUnhidePassword.setManaged(false);
        tfUnhidePassword.setVisible(false);

        tfHidePassword.setManaged(true);
        tfHidePassword.setVisible(true);

        // Sync password text fields
        tfUnhidePassword.textProperty().bindBidirectional(tfHidePassword.textProperty());

        updateIcon();
        Platform.runLater(() -> rootPane.requestFocus());
    }
    /*================================================================================================================================================*/
    private void updateIcon() {
        String iconPath = passwordVisible ? "/cinelux/bioskopcinelux/image/Icon/IconShowPass.png" : "/cinelux/bioskopcinelux/image/Icon/IconHidePass.png";

        try {
            Image image = new Image(getClass().getResourceAsStream(iconPath));
            eyeIcon.setImage(image);
        } catch (Exception e) {
            System.err.println("Logo gagal dimuat: " + iconPath);
            e.printStackTrace();
        }
    }
    /*================================================================================================================================================*/
    @FXML
    public void unhidePassClick(MouseEvent event) {
        int caretPosition = passwordVisible ? tfUnhidePassword.getCaretPosition() : tfHidePassword.getCaretPosition();

        passwordVisible = !passwordVisible;

        tfUnhidePassword.setManaged(passwordVisible);
        tfUnhidePassword.setVisible(passwordVisible);

        tfHidePassword.setManaged(!passwordVisible);
        tfHidePassword.setVisible(!passwordVisible);

        if (passwordVisible) {
            tfUnhidePassword.requestFocus();
            Platform.runLater(() -> tfUnhidePassword.positionCaret(caretPosition));
        } else {
            tfHidePassword.requestFocus();
            Platform.runLater(() -> tfHidePassword.positionCaret(caretPosition));
        }

        updateIcon();
    }
    /*================================================================================================================================================*/
    @FXML
    private void handleLogin() {
        String username = tfUsername.getText().trim();
        String password = tfHidePassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Gagal", "Username dan Password tidak boleh kosong.");
            return;
        }

        Karyawan user = validateLogin(username, password);
        if (user == null || user.getStatus() != 1) {
            showAlert(Alert.AlertType.ERROR, "Login Gagal", "Username atau Password salah atau akun tidak aktif.");
            return;
        }

        // Simpan user ke session
        UserSession.setUser(user);
        Session.setLoggedUser(user);


        aksesLogin(user); // kirim Karyawan, bukan String role
    }
    /*================================================================================================================================================*/
    public class UserSession {
        private static Karyawan currentUser;

        public static void setUser(Karyawan user) {
            currentUser = user;
        }

        public static Karyawan getUser() {
            return currentUser;
        }
    }
    /*================================================================================================================================================*/
    private Karyawan validateLogin(String username, String password) {
        String sql = "{call sp_LoginPegawai(?, ?)}";

        try {
            connect.pstat = connect.conn.prepareCall(sql);
            connect.pstat.setString(1, username);
            connect.pstat.setString(2, password);
            boolean hasResult = connect.pstat.execute();

            if (hasResult) {
                ResultSet rs = connect.pstat.getResultSet();
                if (rs.next()) {
                    Setting role = new Setting();
                    role.setId(rs.getInt("tst_id"));
                    role.setNama(rs.getString("role"));

                    return new Karyawan(
                            rs.getInt("pgw_id"),
                            role,
                            rs.getString("pgw_nama"),
                            rs.getString("pgw_username"),
                            rs.getString("pgw_password"),
//                            rs.getString("pgw_img"),
                            rs.getString("pgw_no_telp"),
                            rs.getString("pgw_alamat"),
                            rs.getInt("pgw_status"),
                            rs.getString("pgw_created_by"),
                            rs.getString("pgw_modif_by")
                    );
                }
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Username tidak ditemukan")) {
                showAlert(Alert.AlertType.ERROR, "Login Gagal", "Username tidak ditemukan.");
            } else if (e.getMessage().contains("Password salah")) {
                showAlert(Alert.AlertType.ERROR, "Login Gagal", "Password salah.");
            } else {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Kesalahan Database", "Terjadi kesalahan saat login: " + e.getMessage());
            }
        }

        return null;
    }
    /*================================================================================================================================================*/
    private void aksesLogin(Karyawan user) {
        SwapPage swapPage = new SwapPage();
        String roleName = user.getRole().getNama().toLowerCase();

        switch (roleName) {
            case "admin":
                swapPage.openNewWindow("Menu/MenuAdmin.fxml", "Menu Admin", rootPane);
                break;
            case "kasir":
                swapPage.openNewWindow("Menu/MenuCashier.fxml", "Menu Kasir", rootPane);
                break;
            case "manager":
                swapPage.openNewWindow("Menu/MenuManager.fxml", "Menu Manager", rootPane);
                break;
            default:
                showAlert(Alert.AlertType.WARNING, "Akses Ditolak", "Role tidak dikenali: " + roleName);
                break;
        }
    }
    /*================================================================================================================================================*/
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    /*================================================================================================================================================*/
}