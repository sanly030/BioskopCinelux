    package cinelux.bioskopcinelux.controller.menu;

    import cinelux.bioskopcinelux.controller.transaksi.PenjualanTiketCtrl;
    import cinelux.bioskopcinelux.model.Karyawan;
    import cinelux.bioskopcinelux.util.MessageBox;
    import cinelux.bioskopcinelux.util.SwapPage;
    import javafx.fxml.FXMLLoader;
    import javafx.fxml.Initializable;
    import javafx.scene.Parent;

    import java.io.IOException;
    import java.io.InputStream;
    import java.net.URL;
    import java.util.ResourceBundle;

    import javafx.fxml.FXML;
    import javafx.scene.control.Label;
    import javafx.scene.image.Image;
    import javafx.scene.image.ImageView;
    import javafx.scene.input.MouseEvent;
    import javafx.scene.layout.AnchorPane;
    import javafx.scene.layout.BorderPane;
    import javafx.scene.layout.HBox;
    import javafx.scene.layout.StackPane;
    import javafx.scene.paint.ImagePattern;
    import javafx.scene.shape.Circle;

    public class MenuKasirController implements Initializable {

        @FXML
        private HBox dashboardMenu;

        @FXML
        private HBox exitMenu;

        @FXML
        private Label headTittle;

        @FXML
        private Circle imgEmployee;

        @FXML
        private ImageView imgTittle;

        @FXML
        private Label lbDashboardMenu;

        @FXML
        private Label lbEmployeeName;

        @FXML
        private Label lbRole;

        @FXML
        private Label lbSubTittle;

        @FXML
        private AnchorPane mainAnchorPane;

        @FXML
        private BorderPane mainBorderPane;

        @FXML
        private HBox menuTransactionMenu;

        @FXML
        private StackPane rootPane;

        @FXML
        private HBox ticketTransactionMenu;

        HBox activeMenu;

        private Karyawan currentUser;

        public void setCurrentUser(Karyawan user) {
            this.currentUser = user;
        }
        public Karyawan getLoggedInKaryawan() {
            return this.currentUser;
        }


        @Override
        public void initialize(URL location, ResourceBundle resources) {
            loadMaster("/cinelux/bioskopcinelux/view/DashboardKasir/DashboardKasir.fxml");
            lbRole.setText("Cashier");
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

                mainAnchorPane.getChildren().setAll(root);

            } catch (IOException e) {
                System.err.println("❌ Gagal memuat halaman FXML: " + fxmlPage);
                e.printStackTrace();
            }
        }

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

        // Untuk memanggil menu yang ada di sidebar
        private void setActiveMenu(HBox menu) {
            if (activeMenu != null) {
                activeMenu.getStyleClass().remove("menu-item-active");
            }

            menu.getStyleClass().add("menu-item-active");

            activeMenu = menu;
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
                        imageStream = getClass().getResourceAsStream("/cinelux/bioskopcinelux/image/LogoTransaksi/"+imagePath);
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

        @FXML
        void handleDashboardCashierClick(MouseEvent event) {
            loadMaster("/cinelux/bioskopcinelux/view/DashboardKasir/DashboardKasir.fxml");
            setActiveMenu(dashboardMenu);
            updateTitleAndImage("Transaction Menu","Transaction","LogoDashboard.png");
        }

        @FXML
        void handleTransactMenuClick(MouseEvent event) {
            loadMaster("/cinelux/bioskopcinelux/view/DashboardKasir/TransaksiPenjualanMenu.fxml");
            setActiveMenu(menuTransactionMenu);
            updateTitleAndImage("Transaction Menu","Create Transaction","LogoTransaksiMenu.png");
        }

//        @FXML
//        void handleTransactTicketClick(MouseEvent event) {
//            loadMaster("/cinelux/bioskopcinelux/view/DashboardKasir/TransaksiPenjualanTiket.fxml");
//            setActiveMenu(ticketTransactionMenu);
//            updateTitleAndImage("Transaction Tiket","Create Transaction","LogoTransaksiTiket.png");
//        }

        @FXML
        void handleTransactTicketClick(MouseEvent event) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/cinelux/bioskopcinelux/view/DashboardKasir/TransaksiPenjualanTiket.fxml"));
                Parent root = loader.load();

                // Dapatkan controllernya
                PenjualanTiketCtrl controller = loader.getController();

                // Set current user di sini (Karyawan yang login)
                Karyawan currentUser = getLoggedInKaryawan(); // Ganti dengan variabel/method penyimpan user login
                controller.setCurrentUser(currentUser);       // ✅ wajib di-set

                // Tampilkan halaman
                mainAnchorPane.getChildren().setAll(root);
                setActiveMenu(ticketTransactionMenu);
                updateTitleAndImage("Transaction Tiket", "Create Transaction", "LogoTransaksiTiket.png");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
