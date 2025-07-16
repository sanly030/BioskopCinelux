package cinelux.bioskopcinelux.controller.menu;

import cinelux.bioskopcinelux.model.Karyawan;
import cinelux.bioskopcinelux.util.MessageBox;
import cinelux.bioskopcinelux.util.SwapPage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class MenuAdminCtrl implements Initializable {
    @FXML
    private HBox dashboardMenu, chairMenu, filmMenu, scheduleMenu, promoMenu, studioMenu, menuMenu, settingMenu, employeeMenu;

    @FXML
    private AnchorPane panelMaster;

    @FXML
    private HBox btnSetting;

    @FXML
    private AnchorPane rootPane;

    @FXML
    private HBox exitMenu;

    @FXML
    private HBox btnPegawai;

    @FXML
    private Label headTittle;

    @FXML
    private Circle imgEmployee;

    @FXML
    private ImageView imgTittle;

    @FXML
    private Label lbEmployeeName;

    @FXML
    private Label lbRole, lbSubTittle;


    HBox activeMenu;

    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadMaster("/cinelux/bioskopcinelux/view/DashboardAdmin/DashboardMenu.fxml");
        lbRole.setText("Admin");
        updateTitleAndImage("Dashboard","Transact Overview","icon_dashboard.png");
        setActiveMenu(dashboardMenu);
//        setProfile(user.getName(), "/projekkelompok4/manajementokofurniture_4niture/image/KaryawanImage/"+user.getPict());
        setProfile("User", "/cinelux/bioskopcinelux/asset/image/EmployeeImg/defaultprofile.png");
    }


    private void loadMaster(String fxmlPage) {
        try {
            // Pastikan path fxml diawali dengan slash "/"
            URL fxmlUrl = getClass().getResource(fxmlPage);

            if (fxmlUrl == null) {
                System.err.println("❌ Tidak dapat menemukan file FXML: " + fxmlPage);
                return;
            }

            Parent root = FXMLLoader.load(fxmlUrl);

            if (root == null) {
                System.err.println("❌ Root node dari FXML adalah null: " + fxmlPage);
                return;
            }

            panelMaster.getChildren().setAll(root);

        } catch (IOException e) {
            System.err.println("❌ Gagal memuat halaman FXML: " + fxmlPage);
            e.printStackTrace();
        }
    }

    // Untuk memanggil menu yang ada di sidebar
    private void setActiveMenu(HBox menu) {
        if (activeMenu != null) {
            activeMenu.getStyleClass().remove("menu-item-active");
        }

        menu.getStyleClass().add("menu-item-active");

        activeMenu = menu;
    }

    // Method set profile
    public void setProfile(String staffName, String imagePath) {
        lbEmployeeName.setText(staffName);

        Image profileImage = null;
        InputStream imageStream = null;

        try {
            if (imagePath != null && !imagePath.isEmpty()) {
                imageStream = getClass().getResourceAsStream(imagePath);
            }

            if (imageStream == null) {
                System.err.println("Gambar di path '" + imagePath + "' tidak ditemukan. Memuat gambar default.");
                imageStream = getClass().getResourceAsStream("cinelux/bioskopcinelux/asset/image/defaultProfile.png");
            }

            if (imageStream != null) {
                profileImage = new Image(imageStream);
            } else {
                System.err.println("GAGAL MEMUAT GAMBAR: Path utama dan default tidak dapat ditemukan.");
            }

        } catch (Exception e) {
            System.err.println("Terjadi error saat memuat gambar: " + e.getMessage());
            e.printStackTrace();
        }

        if (profileImage != null) {
            imgEmployee.setFill(new ImagePattern(profileImage));
        }
    }


    private void updateTitleAndImage(String headTitleText, String subTitleText, String imagePath) {
        if (headTittle != null) {
            headTittle.setText(headTitleText);
        }

        if (lbSubTittle != null) {
            lbSubTittle.setText(subTitleText);
        }

        if (imgTittle != null) {
            InputStream imageStream = null;
            try {
                if (imagePath != null && !imagePath.isEmpty()) {
                    imageStream = getClass().getResourceAsStream("/cinelux/bioskopcinelux/asset/image/IconMasterData/"+imagePath);
                }

                if (imageStream != null) {
                    Image image = new Image(imageStream);
                    imgTittle.setImage(image);
                }else {
                    imgTittle.setImage(null);
                    System.err.println("Gambar untuk imgTittle di path '" + imagePath + "' tidak ditemukan.");
                }
            } catch (Exception e) {
                System.err.println("Terjadi error saat memuat gambar untuk imgTittle: " + e.getMessage());
                e.printStackTrace();
                imgTittle.setImage(null);
            } finally {
                if (imageStream != null) {
                    try {
                        imageStream.close();
                    } catch (IOException e) {
                        System.err.println("Error menutup stream gambar: " + e.getMessage());
                    }
                }
            }
        }
    }


    // Button Exit
    @FXML
    private void btnExitClicked(MouseEvent event) {
        MessageBox msg = new MessageBox();
        SwapPage swapPage = new SwapPage();

        boolean result = msg.alertConfirm("Are you sure you want to quit?");
        if (result) {
            swapPage.openNewWindow("Login/Login.fxml", "Menu Login", rootPane);
        }
    }

    // Dashboard Menu
    @FXML
    void handleDashboardClick(MouseEvent event) {
        loadMaster("/cinelux/bioskopcinelux/view/DashboardAdmin/DashboardMenu.fxml");
        setActiveMenu(dashboardMenu);
        updateTitleAndImage("Dashboard","Transact Overview","icon_dashboard.png");
    }

    // Chair Menu
    @FXML
    void handleChairClick(MouseEvent event) {
        loadMaster("/cinelux/bioskopcinelux/view/DashboardAdmin/Kursi.fxml");
        setActiveMenu(chairMenu);
        updateTitleAndImage("Employee","Create, Read, Update, Delete Chair","icon_employee.png");
    }

    // Film Menu
    @FXML
    void handleFilmClick(MouseEvent event) {
        loadMaster("/cinelux/bioskopcinelux/view/DashboardAdmin/Film.fxml");
        setActiveMenu(filmMenu);
        updateTitleAndImage("Film","Create, Read, Update, Delete Film","icon_film.png");
    }

    // Schedule Menu
    @FXML
    void handleScheduleClick(MouseEvent event) {
        loadMaster("/cinelux/bioskopcinelux/view/DashboardAdmin/JadwalTayang.fxml");
        setActiveMenu(scheduleMenu);
        updateTitleAndImage("Boardcast Schedule","Create, Read, Update, Delete Schedule","icon_schedule.png");
    }

    // Menu
    @FXML
    void handleMenuClick(MouseEvent event) {
        loadMaster("/cinelux/bioskopcinelux/view/DashboardAdmin/Menu.fxml");
        setActiveMenu(menuMenu);
        updateTitleAndImage("Menu","Create, Read, Update, Delete Menu","icon_menu.png");
    }

    // Promo Menu
    @FXML
    void handlePromoClick(MouseEvent event) {
        loadMaster("/cinelux/bioskopcinelux/view/DashboardAdmin/Promo.fxml");
        setActiveMenu(promoMenu);
        updateTitleAndImage("Promo","Create, Read, Update, Delete Promo","icon_promo.png");
    }

    // Studio Menu
    @FXML
    void handleStudioClick(MouseEvent event) {
        loadMaster("/cinelux/bioskopcinelux/view/DashboardAdmin/Studio.fxml");
        setActiveMenu(studioMenu);
        updateTitleAndImage("Studio","Create, Read, Update, Delete Studio","icon_studio.png");
    }

    // Pegawai Menu
    @FXML
    void handlePegawaiClick(MouseEvent event) {
        loadMaster("/cinelux/bioskopcinelux/view/DashboardAdmin/Karyawan.fxml");
        setActiveMenu(employeeMenu);
        updateTitleAndImage("Employee","Create, Read, Update, Delete Employee","icon_employee.png");

    }

    // Setting Menu
    @FXML
    void handleSettingClick(MouseEvent event) {
        loadMaster("/cinelux/bioskopcinelux/view/DashboardAdmin/Setting.fxml");
        setActiveMenu(settingMenu);
        updateTitleAndImage("Table Setting","Create, Read, Update, Delete Role, Menu, Payment Method","icon_tableSetting.png");
    }
}