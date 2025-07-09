//package cinelux.bioskopcinelux.controller.login;
//
//import cinelux.bioskopcinelux.model.Role;
//import cinelux.bioskopcinelux.util.SwapPage;
//import javafx.application.Platform;
//import javafx.fxml.FXML;
//import javafx.scene.control.Alert;
//import javafx.scene.control.Button;
//import javafx.scene.control.PasswordField;
//import javafx.scene.control.TextField;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.input.MouseEvent;
//import javafx.scene.layout.AnchorPane;
//import cinelux.bioskopcinelux.connection.DBConnect;
//
//import java.sql.SQLException;
//
//public class LoginCtrl {
//    @FXML
//    private AnchorPane pane;
//
//    @FXML
//    private Button btnMasuk;
//
//    @FXML
//    private ImageView eyeIcon;
//
//    @FXML
//    private PasswordField pfPassword;
//
//    @FXML
//    private TextField tfPassword;
//
//    @FXML
//    private TextField tfUsername;
//
//    private boolean passwordVisible = false;
//
//    DBConnect connect = new DBConnect();
//
//    @FXML
//    public void initialize() {
//        // Bind the text properties of both password fields
//        pfPassword.textProperty().bindBidirectional(tfPassword.textProperty());
//
//        // Initially hide the TextField and show the PasswordField
//        tfPassword.setManaged(false);
//        tfPassword.setVisible(false);
//
//        pfPassword.setManaged(true);
//        pfPassword.setVisible(true);
//
//        updateIcon();
//        Platform.runLater(() -> pane.requestFocus());
//    }
//
//    private void updateIcon() {
//        String iconPath = passwordVisible ? "/kelompok5/bioskopcinelux/image/show.png" : "/kelompok5/bioskopcinelux/image/hide.png";
//        try {
//            Image image = new Image(getClass().getResourceAsStream(iconPath));
//            eyeIcon.setImage(image);
//        } catch (Exception e) {
//            System.err.println("Could not load icon: " + iconPath);
//            e.printStackTrace();
//        }
//    }
//
//    @FXML
//    public void showPw(MouseEvent event) {
//        // Get current caret position
//        int caretPosition = passwordVisible ? tfPassword.getCaretPosition() : pfPassword.getCaretPosition();
//
//        // Toggle password visibility
//        passwordVisible = !passwordVisible;
//
//        // Show/hide appropriate fields
//        tfPassword.setManaged(passwordVisible);
//        tfPassword.setVisible(passwordVisible);
//
//        pfPassword.setManaged(!passwordVisible);
//        pfPassword.setVisible(!passwordVisible);
//
//        // Set focus and restore caret position
//        if (passwordVisible) {
//            tfPassword.requestFocus();
//            Platform.runLater(() -> tfPassword.positionCaret(caretPosition));
//        } else {
//            pfPassword.requestFocus();
//            Platform.runLater(() -> pfPassword.positionCaret(caretPosition));
//        }
//
//        updateIcon();
//    }
//
//    @FXML
//    private void handleLogin() {
//        String username = tfUsername.getText();
//        String password = pfPassword.getText();
//
//        if (username.isEmpty() || password.isEmpty()) {
//            showAlert(Alert.AlertType.ERROR, "Input Tidak Lengkap", "Username dan Password tidak boleh kosong.");
//            return;
//        }
//
//        Role usr = validateLogin(username, password);
//        if (usr == null) {
//            showAlert(Alert.AlertType.ERROR, "Login Error", "Username atau Password salah.");
//            return;
//        }
//
//        aksesLogin(usr.getRole());
//    }
//
//    private Role validateLogin(String username, String password) {
//        String sql = "SELECT * FROM dbo.udf_getUserByLogin(?, ?)";
//        try {
//            connect.pstat = connect.conn.prepareStatement(sql);
//            connect.pstat.setString(1, username);
//            connect.pstat.setString(2, password);
//            connect.result = connect.pstat.executeQuery();
//
//            if (connect.result.next()) {
//                int id = connect.result.getInt("kry_id");
//                String jabatan = connect.result.getString("kry_role");
//                String nama = connect.result.getString("kry_nama");
//                String pict = connect.result.getString("kry_pict");
//
//                return new Role(id, nama, jabatan, pict);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            showAlert(Alert.AlertType.ERROR, "Kesalahan Database", "Terjadi kesalahan saat mencoba login.");
//        }
//        return null;
//    }
//
//    private void aksesLogin(String role) {
//        SwapPage swapPage = new SwapPage();
//        if (role.equals("Admin")) {
//            swapPage.openNewWindow("Menu/MenuAdmin.fxml", "Menu Admin", pane);
//        } else if (role.equals("Manager")) {
//            swapPage.openNewWindow("Menu/MenuManager.fxml", "Menu Manager", pane);
//        } else if (role.equals("Kasir")) {
//            swapPage.openNewWindow("Menu/MenuCashier.fxml", "Menu Cashier", pane);
//        }
//        pane.getScene().getWindow().hide(); // Close login window after successful login
//    }
//
//    private void showAlert(Alert.AlertType alertType, String title, String message) {
//        Alert alert = new Alert(alertType);
//        alert.setTitle(title);
//        alert.setHeaderText(null);
//        alert.setContentText(message);
//        alert.showAndWait();
//    }
//}


package cinelux.bioskopcinelux.controller.login;

import cinelux.bioskopcinelux.util.MessageBox;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import cinelux.bioskopcinelux.connection.DBConnect;
import cinelux.bioskopcinelux.model.Role;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

import cinelux.bioskopcinelux.model.Karyawan;
import cinelux.bioskopcinelux.util.Feature;
import cinelux.bioskopcinelux.util.SwapPage;

import java.sql.*;

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

    private boolean passwordVisible = false;


    DBConnect connect = new DBConnect();
    MessageBox msg = new MessageBox();

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

    private void updateIcon() {
        String iconPath = passwordVisible ? "/cinelux/bioskopcinelux/asset/image/show.png" : "/cinelux/bioskopcinelux/asset/image/hide.png";

        try {
            Image image = new Image(getClass().getResourceAsStream(iconPath));
            eyeIcon.setImage(image);
        } catch (Exception e) {
            System.err.println("Icon gagal dimuat: " + iconPath);
            e.printStackTrace();
        }
    }

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

    @FXML
    private void handleLogin() {
        String username = tfUsername.getText().trim();
        String password = tfHidePassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Gagal", "Username dan Password tidak boleh kosong.");
            return;
        }

        Role user = validateLoginWithSP(username, password);
        if (user == null) {
            showAlert(Alert.AlertType.ERROR, "Login Gagal", "Username atau Password salah atau akun tidak aktif.");
            return;
        }

        aksesLogin(user.getRole());
    }

    public class UserSession {
        private static Role currentUser;

        public static void setUser(Role user) {
            currentUser = user;
        }

        public static Role getUser() {
            return currentUser;
        }
    }

    private Role validateLoginWithSP(String username, String password) {
        String sql = "{call sp_LoginPegawai(?, ?)}";

        try {
            connect.pstat = connect.conn.prepareCall(sql);
            connect.pstat.setString(1, username);
            connect.pstat.setString(2, password);
            boolean hasResult = connect.pstat.execute();

            if (hasResult) {
                ResultSet rs = connect.pstat.getResultSet();
                if (rs.next()) {
                    int id = rs.getInt("pgw_id");
                    String nama = rs.getString("pgw_nama");
                    String role = rs.getString("role");
                    String pict = null;

                    return new Role(id, nama, role, pict);
                }
            }
        } catch (SQLException e) {
            // Cek apakah error karena RAISERROR di SP
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

    private void aksesLogin(String roleId) {
        SwapPage swapPage = new SwapPage();

        if (roleId.equals("Admin")) {
            swapPage.openNewWindow("Menu/MenuAdmin.fxml", "Menu Admin", rootPane);
        } else if (roleId.equals("Manager")) {
            swapPage.openNewWindow("Menu/MenuAdmin.fxml", "Menu Manager", rootPane);
        } else if (roleId.equals("Kasir")) {
            swapPage.openNewWindow("Menu/MenuAdmin.fxml", "Menu Cashier", rootPane);
        }
    }


    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}