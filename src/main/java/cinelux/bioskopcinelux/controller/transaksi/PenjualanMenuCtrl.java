package cinelux.bioskopcinelux.controller.transaksi;

import cinelux.bioskopcinelux.controller.list.MenuCardCtrl;
import cinelux.bioskopcinelux.model.*;
import cinelux.bioskopcinelux.service.impl.MenuImpl;
import cinelux.bioskopcinelux.service.impl.PenjualanMenuImpl;
import javafx.animation.ScaleTransition;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PenjualanMenuCtrl {

    @FXML private FlowPane flowMenuCard;
    @FXML private VBox CardContent;
    @FXML private ScrollPane scrollCard, scrollCart;
    @FXML private Label labelTotalBayar, lblEmptyCart;
    @FXML private TextField tfSearch, tfUangBayar, tfKembalian, tfPelanggan;
    @FXML private ComboBox<Promo> cbPromo;
    @FXML private ComboBox<Setting> cbMetodePembayaran;
    @FXML private Button btnBayar, btnMakanan, btnMinuman, btnAll;

    private final MenuImpl menuImpl = new MenuImpl();
    private final PenjualanMenuImpl penjualanMenuImpl = new PenjualanMenuImpl();
    private final NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));

    private final Map<Integer, Integer> cart = new LinkedHashMap<>();
    private final Map<Integer, Menu> menuCache = new HashMap<>();
    private double totalBayar = 0.0;
    private double totalSebelumDiskon = 0.0;
    private String currentFilter = "All";
    private String currentSearchQuery = "";
    private List<Menu> allMenus = new ArrayList<>(); // Cache all menus

    // Temporary user ID - in real app, this should come from session/login
    private final int currentUserId = 1; // Replace with actual logged-in user ID

    @FXML
    public void initialize() {
        setupComponents();
        loadAllMenus(); // Load all menus first
        loadComboBoxData();
        setupEventHandlers();
        loadAndDisplayMenuItems(); // Use unified loading method
        updateEmptyCartVisibility();
        updateButtonStyles("All"); // Set initial button style
    }

    private void setupComponents() {
        // Setup initial values
        if (labelTotalBayar != null) {
            labelTotalBayar.setText(rupiahFormat.format(0.0));
        }

        if (tfKembalian != null) {
            tfKembalian.setEditable(false);
        }

        // Setup ComboBox converters
        setupComboBoxConverters();
    }

    private void setupComboBoxConverters() {
        // Setup Promo ComboBox converter
        if (cbPromo != null) {
            cbPromo.setConverter(new StringConverter<Promo>() {
                @Override
                public String toString(Promo promo) {
                    if (promo == null) return "Tidak Ada Promo";
                    return promo.getNama_promo() + " (" +
                            (promo.getTipe_promo().equals("PERCENTAGE") ?
                                    String.format("%.0f%%", promo.getDiskon()) :
                                    rupiahFormat.format(promo.getDiskon())) + ")";
                }

                @Override
                public Promo fromString(String string) {
                    return null;
                }
            });
        }

        // Setup Metode Pembayaran ComboBox converter
        if (cbMetodePembayaran != null) {
            cbMetodePembayaran.setConverter(new StringConverter<Setting>() {
                @Override
                public String toString(Setting setting) {
                    if (setting == null) return "";
                    String icon = getPaymentIcon(setting.getNama());
                    return icon + " " + setting.getNama();
                }

                @Override
                public Setting fromString(String string) {
                    return null;
                }
            });
        }
    }

    private String getPaymentIcon(String paymentMethod) {
        switch (paymentMethod.toLowerCase()) {
            case "cash":
            case "tunai":
                return "💵";
            case "qris":
                return "📱";
            case "transfer":
            case "bank transfer":
                return "💳";
            case "debit":
            case "kartu debit":
                return "💳";
            case "credit":
            case "kartu kredit":
                return "💳";
            default:
                return "💰";
        }
    }

    private void loadAllMenus() {
        try {
            // Use consistent service method
            allMenus = menuImpl.getAllData(null, null, 1, null, null);

            // Cache all menus
            menuCache.clear();
            for (Menu menu : allMenus) {
                menuCache.put(menu.getId(), menu);
            }
            System.out.println("Loaded " + allMenus.size() + " menus");
        } catch (Exception e) {
            System.err.println("Error loading all menus: " + e.getMessage());
            e.printStackTrace();
            allMenus = new ArrayList<>();
        }
    }

    private void loadComboBoxData() {
        try {
            // Load Promo data
            if (cbPromo != null) {
                List<Promo> promoList = penjualanMenuImpl.getAllPromo();
                cbPromo.getItems().clear();

                // Add "No Promo" option
                Promo noPromo = new Promo();
                noPromo.setId(0);
                noPromo.setNama_promo("Tidak Ada Promo");
                noPromo.setTipe_promo("NONE");
                noPromo.setDiskon(0.0);
                cbPromo.getItems().add(noPromo);

                // Add actual promos
                cbPromo.getItems().addAll(promoList);
                cbPromo.setValue(noPromo);
            }

            // Load Metode Pembayaran data
            if (cbMetodePembayaran != null) {
                List<Setting> metodePembayaranList = penjualanMenuImpl.getMetodePembayaran();
                cbMetodePembayaran.getItems().clear();
                cbMetodePembayaran.getItems().addAll(metodePembayaranList);

                // Set default to first item if available
                if (!metodePembayaranList.isEmpty()) {
                    cbMetodePembayaran.setValue(metodePembayaranList.get(0));
                }
            }

        } catch (Exception e) {
            System.err.println("Error loading combo box data: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Gagal memuat data combo box!", Alert.AlertType.ERROR);
        }
    }

    private void setupEventHandlers() {
        // Setup search functionality
        if (tfSearch != null) {
            tfSearch.textProperty().addListener((obs, oldV, newV) -> {
                if (newV != null) {
                    currentSearchQuery = newV.trim();
                    loadAndDisplayMenuItems(); // SUDAH BENAR
                }
            });
        }

        // Setup payment calculation
        if (tfUangBayar != null) {
            tfUangBayar.textProperty().addListener((obs, oldV, newV) -> {
                calculateKembalian();
            });
        }

        // Setup promo selection
        if (cbPromo != null) {
            cbPromo.setOnAction(e -> {
                System.out.println("Promo changed: " + cbPromo.getValue());
                updateCartDisplay();
            });
        }

        // Setup payment method selection
        if (cbMetodePembayaran != null) {
            cbMetodePembayaran.setOnAction(e -> {
                updatePaymentMethodVisibility();
            });
        }

        // Setup filter buttons - PERBAIKAN UTAMA
        if (btnMakanan != null) {
            btnMakanan.setOnAction(e -> {
                System.out.println("Filter Makanan clicked");
                currentFilter = "Makanan";
                loadAndDisplayMenuItems(); // GANTI DARI loadMenuItems()
                updateButtonStyles("Makanan");
            });
        }

        if (btnMinuman != null) {
            btnMinuman.setOnAction(e -> {
                System.out.println("Filter Minuman clicked");
                currentFilter = "Minuman";
                loadAndDisplayMenuItems(); // GANTI DARI loadMenuItems()
                updateButtonStyles("Minuman");
            });
        }

        if (btnAll != null) {
            btnAll.setOnAction(e -> {
                System.out.println("Filter All clicked");
                currentFilter = "All";
                loadAndDisplayMenuItems(); // GANTI DARI loadMenuItems()
                updateButtonStyles("All");
            });
        }

        // Setup payment button
        if (btnBayar != null) {
            btnBayar.setOnAction(e -> handlePayment());
        }
    }

    private void updateButtonStyles(String activeFilter) {
        String activeStyle = "-fx-background-color: linear-gradient(to right, #8B4513, #A0522D); -fx-background-radius: 25; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);";
        String inactiveStyle = "-fx-background-color: #382416; -fx-background-radius: 25; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;";

        if (btnMakanan != null) {
            btnMakanan.setStyle(activeFilter.equals("Makanan") ? activeStyle : inactiveStyle);
        }
        if (btnMinuman != null) {
            btnMinuman.setStyle(activeFilter.equals("Minuman") ? activeStyle : inactiveStyle);
        }
        if (btnAll != null) {
            btnAll.setStyle(activeFilter.equals("All") ? activeStyle : inactiveStyle);
        }
    }

    // UNIFIED METHOD: Load and display menu items based on current filter and search
    private void loadAndDisplayMenuItems() {
        try {
            if (flowMenuCard != null) {
                flowMenuCard.getChildren().clear();
            }

            System.out.println("Loading menu items - Filter: " + currentFilter + ", Search: '" + currentSearchQuery + "'");
            int itemCount = 0;

            for (Menu menu : allMenus) {
                if (shouldDisplayMenu(menu)) {
                    createAndAddMenuCard(menu);
                    itemCount++;
                }
            }

            System.out.println("Displayed " + itemCount + " menu items");

        } catch (Exception ex) {
            System.err.println("Error loading menu items: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // IMPROVED: Check if menu should be displayed based on filter and search
    private boolean shouldDisplayMenu(Menu menu) {
        // Check search query first
        if (!currentSearchQuery.isEmpty()) {
            boolean matchesSearch = menu.getNama().toLowerCase().contains(currentSearchQuery.toLowerCase());

            // Also search by category if not found by name
            if (!matchesSearch && menu.getJenis_makanan() != null) {
                matchesSearch = menu.getJenis_makanan().getNama().toLowerCase().contains(currentSearchQuery.toLowerCase());
            }

            if (!matchesSearch) {
                return false;
            }
        }

        // Check category filter
        if (!"All".equals(currentFilter)) {
            if (menu.getJenis_makanan() == null) {
                return false;
            }

            String jenisNama = menu.getJenis_makanan().getNama();

            // PERBAIKAN: Lebih fleksibel dalam pencocokan kategori
            if ("Makanan".equals(currentFilter)) {
                return jenisNama.equalsIgnoreCase("Makanan") ||
                        jenisNama.toLowerCase().contains("makanan") ||
                        jenisNama.toLowerCase().contains("food") ||
                        jenisNama.toLowerCase().contains("snack") ||
                        jenisNama.toLowerCase().contains("makan");
            } else if ("Minuman".equals(currentFilter)) {
                return jenisNama.equalsIgnoreCase("Minuman") ||
                        jenisNama.toLowerCase().contains("minuman") ||
                        jenisNama.toLowerCase().contains("drink") ||
                        jenisNama.toLowerCase().contains("beverage") ||
                        jenisNama.toLowerCase().contains("minum");
            }
        }

        return true;
    }

    private void createAndAddMenuCard(Menu menu) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cinelux/bioskopcinelux/view/List/MenuCard.fxml"));
            Node card = loader.load();
            MenuCardCtrl controller = loader.getController();

            // Set menu data
            controller.lblNama.setText(menu.getNama());
            controller.lblHarga.setText(rupiahFormat.format(menu.getHarga()));

            // Load image
            if (menu.getGambar() != null && !menu.getGambar().isEmpty()) {
                File imageFile = new File("src/main/resources/images/Menu/" + menu.getGambar());
                if (imageFile.exists()) {
                    controller.imgMenu.setImage(new Image(imageFile.toURI().toString()));
                }
            }

            // Add smooth animations
            addCardAnimations(card, menu);

            if (flowMenuCard != null) {
                flowMenuCard.getChildren().add(card);
            }
        } catch (IOException ex) {
            System.err.println("Error loading menu card: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void addCardAnimations(Node card, Menu menu) {
        // Hover effect
        card.setOnMouseEntered(event -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });

        card.setOnMouseExited(event -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        // Click effect
        card.setOnMouseClicked(event -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), card);
            st.setToX(0.95);
            st.setToY(0.95);
            st.setOnFinished(e -> {
                ScaleTransition st2 = new ScaleTransition(Duration.millis(100), card);
                st2.setToX(1.0);
                st2.setToY(1.0);
                st2.play();
            });
            st.play();

            addToCart(menu);
        });
    }

    private void addToCart(Menu menu) {
        if (menu != null) {
            // Check stock availability
            int currentQtyInCart = cart.getOrDefault(menu.getId(), 0);
            if (currentQtyInCart >= menu.getStok()) {
                showAlert("Stok Habis", "Stok " + menu.getNama() + " tidak mencukupi!", Alert.AlertType.WARNING);
                return;
            }

            cart.put(menu.getId(), currentQtyInCart + 1);
            updateCartDisplay();
            showAddToCartAnimation();
        }
    }

    private void showAddToCartAnimation() {
        // Simple fade effect on cart
        if (CardContent != null) {
            FadeTransition ft = new FadeTransition(Duration.millis(300), CardContent);
            ft.setFromValue(0.7);
            ft.setToValue(1.0);
            ft.play();
        }
    }

    private void updateCartDisplay() {
        if (CardContent != null) {
            CardContent.getChildren().clear();
        }

        totalSebelumDiskon = 0.0;
        totalBayar = 0.0;

        if (cart.isEmpty()) {
            updateEmptyCartVisibility();
            updateTotalDisplay();
            return;
        }

        // Calculate subtotal first
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Menu menu = menuCache.get(entry.getKey());
            if (menu == null) {
                menu = menuImpl.getById(entry.getKey());
                if (menu != null) {
                    menuCache.put(entry.getKey(), menu);
                }
            }

            if (menu != null) {
                int quantity = entry.getValue();
                double subtotal = menu.getHarga() * quantity;
                totalSebelumDiskon += subtotal;

                // Create attractive cart item
                VBox cartItem = createEnhancedCartItem(menu, quantity, subtotal);
                if (CardContent != null) {
                    CardContent.getChildren().add(cartItem);
                }
            }
        }

        // Apply discount - FIXED
        applyDiscount();

        updateEmptyCartVisibility();
        updateTotalDisplay();
        calculateKembalian();
    }

    private void updateTotalDisplay() {
        if (labelTotalBayar != null) {
            labelTotalBayar.setText(rupiahFormat.format(totalBayar));
        }
    }

    private VBox createEnhancedCartItem(Menu menu, int quantity, double subtotal) {
        VBox cartItem = new VBox(8);
        cartItem.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 12; -fx-padding: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 3, 0, 0, 1);");

        // Item name and price
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(menu.getNama());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label priceLabel = new Label(rupiahFormat.format(menu.getHarga()));
        priceLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold; -fx-font-size: 12px;");

        headerBox.getChildren().addAll(nameLabel, spacer, priceLabel);

        // Quantity controls
        HBox controlsBox = new HBox(10);
        controlsBox.setAlignment(Pos.CENTER_LEFT);

        Button minusBtn = new Button("−");
        minusBtn.setStyle("-fx-background-color: #FF6B6B; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-min-width: 30; -fx-min-height: 30; -fx-cursor: hand;");
        minusBtn.setOnAction(e -> removeFromCart(menu.getId()));

        Label qtyLabel = new Label("Qty: " + quantity);
        qtyLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");

        Button plusBtn = new Button("+");
        plusBtn.setStyle("-fx-background-color: #4ECDC4; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-min-width: 30; -fx-min-height: 30; -fx-cursor: hand;");
        plusBtn.setOnAction(e -> addToCart(menu));

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, javafx.scene.layout.Priority.ALWAYS);

        Label subtotalLabel = new Label("= " + rupiahFormat.format(subtotal));
        subtotalLabel.setStyle("-fx-text-fill: #90EE90; -fx-font-weight: bold; -fx-font-size: 14px;");

        controlsBox.getChildren().addAll(minusBtn, qtyLabel, plusBtn, spacer2, subtotalLabel);

        cartItem.getChildren().addAll(headerBox, controlsBox);

        return cartItem;
    }

    private void updateEmptyCartVisibility() {
        if (lblEmptyCart != null) {
            lblEmptyCart.setVisible(cart.isEmpty());
        }
    }

    private void removeFromCart(int menuId) {
        if (cart.containsKey(menuId)) {
            int currentQty = cart.get(menuId);
            if (currentQty > 1) {
                cart.put(menuId, currentQty - 1);
            } else {
                cart.remove(menuId);
            }
            updateCartDisplay();
        }
    }

    // FIXED: Apply discount method with proper handling for different promo types
    private void applyDiscount() {
        // Start with subtotal
        totalBayar = totalSebelumDiskon;

        if (cbPromo != null && cbPromo.getValue() != null) {
            Promo selectedPromo = cbPromo.getValue();

            System.out.println("Applying promo: " + selectedPromo.getNama_promo());
            System.out.println("Promo type: " + selectedPromo.getTipe_promo());
            System.out.println("Discount value: " + selectedPromo.getDiskon());
            System.out.println("Subtotal before discount: " + totalSebelumDiskon);

            if (selectedPromo.getId() > 0) { // Not "No Promo"
                String tifePromo = selectedPromo.getTipe_promo();

                // Handle different promo types
                if ("PERCENTAGE".equals(tifePromo)) {
                    // Untuk percentage discount - nilai sudah dalam format desimal (15.00 = 15%)
                    double discountRate = selectedPromo.getDiskon() / 100.0;
                    totalBayar = totalSebelumDiskon * (1 - discountRate);

                } else if ("FIXED".equals(tifePromo)) {
                    // Untuk fixed amount discount
                    totalBayar = Math.max(0, totalSebelumDiskon - selectedPromo.getDiskon());

                } else if ("Pemesanan Menu".equals(tifePromo)) {
                    // PERBAIKAN: Untuk promo "Pemesanan Menu" - treat as percentage
                    // Nilai 15.00 = 15%
                    double discountRate = selectedPromo.getDiskon() / 100.0;
                    totalBayar = totalSebelumDiskon * (1 - discountRate);
                    System.out.println("Pemesanan Menu discount applied: " + selectedPromo.getDiskon() + "%");

                } else {
                    // Untuk types lain, assume percentage
                    double discountRate = selectedPromo.getDiskon() / 100.0;
                    totalBayar = totalSebelumDiskon * (1 - discountRate);
                }

                System.out.println("Total after discount: " + totalBayar);
                System.out.println("Discount amount: " + (totalSebelumDiskon - totalBayar));
            }
        }
    }

    private void updatePaymentMethodVisibility() {
        if (cbMetodePembayaran != null) {
            Setting selectedMethod = cbMetodePembayaran.getValue();

            if (selectedMethod != null) {
                boolean isCash = selectedMethod.getNama().toLowerCase().contains("cash") ||
                        selectedMethod.getNama().toLowerCase().contains("tunai");

                // Show/hide cash payment fields based on selected method
                if (tfUangBayar != null) {
                    tfUangBayar.setVisible(isCash);
                }
                if (tfKembalian != null) {
                    tfKembalian.setVisible(isCash);
                }
            }
        }
    }

    private void calculateKembalian() {
        if (tfUangBayar != null && tfKembalian != null && tfUangBayar.isVisible()) {
            try {
                String uangBayarText = tfUangBayar.getText().trim();
                if (!uangBayarText.isEmpty()) {
                    double uangBayar = Double.parseDouble(uangBayarText);
                    double kembalian = uangBayar - totalBayar;
                    tfKembalian.setText(rupiahFormat.format(Math.max(0, kembalian)));
                } else {
                    tfKembalian.setText(rupiahFormat.format(0.0));
                }
            } catch (NumberFormatException e) {
                tfKembalian.setText(rupiahFormat.format(0.0));
            }
        }
    }

    private void handlePayment() {
        if (cart.isEmpty()) {
            showAlert("Error", "Keranjang belanja masih kosong!", Alert.AlertType.ERROR);
            return;
        }

        if (cbMetodePembayaran == null || cbMetodePembayaran.getValue() == null) {
            showAlert("Error", "Pilih metode pembayaran terlebih dahulu!", Alert.AlertType.ERROR);
            return;
        }

        Setting paymentMethod = cbMetodePembayaran.getValue();

        // Validate cash payment
        if (paymentMethod.getNama().toLowerCase().contains("cash") ||
                paymentMethod.getNama().toLowerCase().contains("tunai")) {
            if (!validateCashPayment()) {
                return;
            }
        }

        // Process payment
        if (processPayment()) {
            showPaymentSuccess();
            clearCart();
        }
    }

    private boolean validateCashPayment() {
        if (tfUangBayar == null || tfUangBayar.getText().trim().isEmpty()) {
            showAlert("Error", "Masukkan jumlah uang tunai yang dibayarkan!", Alert.AlertType.ERROR);
            return false;
        }

        try {
            double uangBayar = Double.parseDouble(tfUangBayar.getText().trim());
            if (uangBayar < totalBayar) {
                showAlert("Error", "Jumlah uang yang dibayarkan kurang dari total belanja!", Alert.AlertType.ERROR);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Format uang tidak valid!", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private boolean processPayment() {
        try {
            // Create main transaction
            TransaksiPenjualanMenu transaksi = new TransaksiPenjualanMenu();

            // Set employee (you should get this from current session)
            Karyawan pegawai = new Karyawan();
            pegawai.setId(currentUserId);
            transaksi.setPegawai(pegawai);

            // Set payment method
            transaksi.setMetodePembayaran(cbMetodePembayaran.getValue());

            // Set promo if selected
            if (cbPromo.getValue() != null && cbPromo.getValue().getId() > 0) {
                transaksi.setPromo(cbPromo.getValue());
            }

            // Calculate total quantity
            int totalQuantity = cart.values().stream().mapToInt(Integer::intValue).sum();
            transaksi.setJumlahMenu(totalQuantity);
            transaksi.setTotalHarga(totalBayar);
            transaksi.setTanggal(LocalDateTime.now());
            transaksi.setCreatedBy("System"); // Should be current user
            transaksi.setCreatedDate(LocalDateTime.now());

            // Create detail transactions
            List<DetailPenjualanMenu> detailList = new ArrayList<>();
            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                Menu menu = menuCache.get(entry.getKey());
                if (menu != null) {
                    DetailPenjualanMenu detail = new DetailPenjualanMenu();
                    detail.setTransaksi(transaksi);
                    detail.setMenu(menu);
                    detail.setJumlah(entry.getValue());
                    detail.setSubTotal(menu.getHarga() * entry.getValue());
                    detailList.add(detail);
                }
            }
            transaksi.setDetailPenjualanMenu(detailList);

            // Save to database
            boolean saved = penjualanMenuImpl.saveData(transaksi);

            if (saved) {
                // Update stock for each menu item
                for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                    penjualanMenuImpl.updateStokMenu(entry.getKey(), entry.getValue());
                }

                System.out.println("Payment processed successfully!");
                System.out.println("Transaction ID: " + transaksi.getId());
                System.out.println("Total: " + rupiahFormat.format(totalBayar));
                System.out.println("Payment method: " + cbMetodePembayaran.getValue().getNama());

                return true;
            } else {
                showAlert("Error", "Gagal menyimpan transaksi ke database!", Alert.AlertType.ERROR);
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error processing payment: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Terjadi kesalahan saat memproses pembayaran: " + e.getMessage(), Alert.AlertType.ERROR);
            return false;
        }
    }

    private void showPaymentSuccess() {
        String customerName = (tfPelanggan != null && !tfPelanggan.getText().trim().isEmpty())
                ? tfPelanggan.getText().trim() : "Customer";

        String promoInfo = "";
        if (cbPromo.getValue() != null && cbPromo.getValue().getId() > 0) {
            promoInfo = "\nPromo: " + cbPromo.getValue().getNama_promo();
            if (totalSebelumDiskon > totalBayar) {
                promoInfo += "\nDiskon: " + rupiahFormat.format(totalSebelumDiskon - totalBayar);
            }
        }

        String message = String.format(
                "Pembayaran berhasil!\n\n" +
                        "Pelanggan: %s\n" +
                        "Metode: %s\n" +
                        "Subtotal: %s%s\n" +
                        "Total: %s\n" +
                        "Waktu: %s\n\n" +
                        "Terima kasih atas pembelian Anda!",
                customerName,
                cbMetodePembayaran.getValue().getNama(),
                rupiahFormat.format(totalSebelumDiskon),
                promoInfo,
                rupiahFormat.format(totalBayar),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );

        showAlert("Pembayaran Berhasil", message, Alert.AlertType.INFORMATION);
    }

    private void clearCart() {
        cart.clear();
        updateCartDisplay();

        // Clear payment fields
        if (tfUangBayar != null) {
            tfUangBayar.clear();
        }
        if (tfKembalian != null) {
            tfKembalian.clear();
        }
        if (tfPelanggan != null) {
            tfPelanggan.clear();
        }

        // Reset promo to "No Promo"
        if (cbPromo != null && !cbPromo.getItems().isEmpty()) {
            cbPromo.setValue(cbPromo.getItems().get(0)); // First item is "No Promo"
        }

        // PERBAIKAN: Refresh menu items to update stock display
        loadAndDisplayMenuItems(); // GANTI DARI loadMenuItems()
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Utility methods for external access
    public void refreshMenuItems() {
        loadAndDisplayMenuItems(); // GANTI DARI loadMenuItems()
    }

    public void refreshComboBoxData() {
        loadComboBoxData();
    }

    public void clearSearch() {
        if (tfSearch != null) {
            tfSearch.clear();
        }
        currentSearchQuery = ""; // TAMBAHKAN ini
        loadAndDisplayMenuItems(); // GANTI DARI loadMenuItems()
    }

    public double getTotalBayar() {
        return totalBayar;
    }

    public double getTotalSebelumDiskon() {
        return totalSebelumDiskon;
    }

    public int getCartItemCount() {
        return cart.values().stream().mapToInt(Integer::intValue).sum();
    }

    public boolean isCartEmpty() {
        return cart.isEmpty();
    }

    public void setCurrentUserId(int userId) {
        // This method can be used to set the current user ID from the login session
        // For now, we're using a hardcoded value
    }
}