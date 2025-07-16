    package cinelux.bioskopcinelux.controller.transaksi;
    import cinelux.bioskopcinelux.controller.list.JadwalCardCtrl;
    import cinelux.bioskopcinelux.controller.master.PemilihanKursiCtrl;
    import cinelux.bioskopcinelux.model.*;
    import cinelux.bioskopcinelux.util.MessageBox;
    import cinelux.bioskopcinelux.controller.cart.CartTicketItem;
    import cinelux.bioskopcinelux.service.JadwalTayangSrvc;
    import cinelux.bioskopcinelux.service.PromoSrvc;
    import cinelux.bioskopcinelux.service.TransaksiPenjualanTiketSrvc;
    import cinelux.bioskopcinelux.service.impl.JadwalTayangImpl;
    import cinelux.bioskopcinelux.service.impl.PromoImpl;
    import cinelux.bioskopcinelux.service.impl.PenjualanTiketImpl;

    import javafx.animation.FadeTransition;
    import javafx.animation.ScaleTransition;
    import javafx.application.Platform;
    import javafx.fxml.FXML;
    import javafx.fxml.FXMLLoader;
    import javafx.fxml.Initializable;
    import javafx.geometry.Pos;

    import javafx.scene.Parent;
    import javafx.scene.Scene;
    import javafx.scene.control.*;
    import javafx.scene.effect.DropShadow;
    import javafx.scene.layout.*;
    import javafx.scene.paint.Color;

    import javafx.stage.Modality;
    import javafx.stage.Stage;
    import javafx.util.Duration;
    import javafx.util.StringConverter;
    import java.io.IOException;
    import java.math.BigDecimal;
    import java.net.URL;
    import java.text.DecimalFormat;
    import java.text.DecimalFormatSymbols;
    import java.text.NumberFormat;
    import java.time.LocalDateTime;
    import java.util.*;
    import java.util.stream.Collectors;

    public class PenjualanTiketCtrl implements Initializable {
        @FXML
        private VBox cartContent;
        @FXML
        private Button btnBayar;
        @FXML
        private Button btnClearCart;
        @FXML
        private Button btnRefresh;
        @FXML
        private ComboBox<Setting> cbMetodePembayaran;
        @FXML
        private ComboBox<Promo> cbPromo;
        @FXML
        private FlowPane flowTiketCard;
        @FXML
        private Label labelTotalBayar;
        @FXML
        private Label labelSubtotal;
        @FXML
        private Label labelDiskon;
        @FXML
        private Label labelCartCount;
        @FXML
        private ScrollPane scrollCard;
        @FXML
        private ScrollPane scrollCart;
        @FXML
        private TextField tfKembalian;
        @FXML
        private TextField tfSearch;
        @FXML
        private TextField tfUangBayar;
        @FXML
        private TextField tfCustomerName;
        @FXML
        private ProgressIndicator loadingIndicator;
        @FXML
        private Label statusLabel;

        // Services
        private JadwalTayangSrvc jadwalTayangService;
        private PromoSrvc promoService;
        private TransaksiPenjualanTiketSrvc transaksiService;
        private MessageBox messageBox;
        // Data
        private List<JadwalTayang> jadwalTayangList;
        private List<JadwalTayang> filteredJadwalList;
        private List<CartTicketItem> cartItems;
        private DecimalFormat currencyFormat;
        private PenjualanTiketImpl penjualanTiketImpl = new PenjualanTiketImpl();
        private Karyawan currentUser;  // ← pastikan variabel ini sudah diset saat login
        private final int currentUserId = 1;


        @Override
        public void initialize(URL location, ResourceBundle resources) {
            // Initialize collections
            cartItems = new ArrayList<>();
            // Initialize currency format
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
            symbols.setGroupingSeparator('.');
            symbols.setDecimalSeparator(',');
            currencyFormat = new DecimalFormat("#,###", symbols);
            // Initialize services
            initializeServices();
            // Setup components
            setupComponents();
            // Load initial data
            loadInitialData();
            // Setup event handlers
            setupEventHandlers();
            // Setup animations
            setupAnimations();
            // Show welcome message
            showStatusMessage("Selamat datang di sistem penjualan tiket", false);
        }

        private void initializeServices() {
            try {
                jadwalTayangService = new JadwalTayangImpl();
                promoService = new PromoImpl();
                transaksiService = new PenjualanTiketImpl();
                setupEnhancedComponents();
            } catch (Exception e) {
                showErrorMessage("Gagal menginisialisasi layanan: " + e.getMessage());
            }
        }

        public void setCurrentUser(Karyawan user) {
            this.currentUser = user;
        }

        public void addTicketToCart(JadwalTayang jadwal, int quantity, List<Kursi> kursiDipilih) {
            // Cek apakah jadwal sudah ada di keranjang
            CartTicketItem existingItem = null;
            for (CartTicketItem item : cartItems) {
                if (item.getJadwal().equals(jadwal)) {
                    existingItem = item;
                    break;
                }
            }

            if (existingItem != null) {
                // Update quantity jika sudah ada
                existingItem.setQuantity(existingItem.getQuantity() + quantity);

                // Tambahkan kursi baru ke existing item (jika perlu)
                if (kursiDipilih != null && !kursiDipilih.isEmpty()) {
                    existingItem.addKursi(kursiDipilih);
                }
            } else {
                // Tambah item baru ke keranjang
                CartTicketItem newItem = new CartTicketItem(jadwal, quantity, kursiDipilih,
                        this::updateCartTotals, () -> removeFromCartByJadwal(jadwal));
                cartItems.add(newItem);
            }

            updateCartDisplay();
            updateCartTotals();
            showStatusMessage("Tiket berhasil ditambahkan ke keranjang", false);
        }

        // Method overload untuk backward compatibility (jika ada kode lain yang menggunakan method lama)
//        public void addTicketToCart(JadwalTayang jadwal, int quantity) {
//            addTicketToCart(jadwal, quantity, null);
//        }

        private void removeFromCartByJadwal(JadwalTayang jadwal) {
            cartItems.removeIf(item -> item.getJadwal().equals(jadwal));
            updateCartDisplay();
            updateCartCount();
            calculateTotalBayar();
            showStatusMessage("Item dihapus dari keranjang", false);
        }

        private void updateCartTotals() {
            if (cartItems.isEmpty()) {
                labelSubtotal.setText("Rp 0");
                labelDiskon.setText("Rp 0");
                labelTotalBayar.setText("Rp 0");
                btnBayar.setDisable(true);
                return;
            }

            // Calculate subtotal
            BigDecimal subtotal = cartItems.stream()
                    .map(CartTicketItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate discount
            BigDecimal discount = BigDecimal.ZERO;
            Promo selectedPromo = cbPromo.getSelectionModel().getSelectedItem();
            if (selectedPromo != null) {
                if ("percentage".equals(selectedPromo.getTipe_promo())) {
                    discount = subtotal.multiply(BigDecimal.valueOf(selectedPromo.getDiskon()).divide(BigDecimal.valueOf(100)));
                } else {
                    discount = BigDecimal.valueOf(selectedPromo.getDiskon());
                }
            }

            // Calculate total
            BigDecimal total = subtotal.subtract(discount);
            if (total.compareTo(BigDecimal.ZERO) < 0) {
                total = BigDecimal.ZERO;
            }

            // Update labels
            labelSubtotal.setText("Rp " + currencyFormat.format(subtotal));
            labelDiskon.setText("Rp " + currencyFormat.format(discount));
            labelTotalBayar.setText("Rp " + currencyFormat.format(total));

            btnBayar.setDisable(false);
            calculateKembalian();
        }

        private void setupComponents() {
            // Setup promo combobox
            setupPromoComboBox();
            // Setup search field
            setupSearchField();
            // Setup payment method
            setupPaymentMethod();
            // Setup scroll panes
            setupScrollPanes();
            // Setup input fields
            setupInputFields();
            // Setup buttons
            setupButtons();
            // Setup cart display
            setupCartDisplay();
        }

        private void setupPromoComboBox() {
            cbPromo.setConverter(new StringConverter<Promo>() {
                @Override
                public String toString(Promo promo) {
                    if (promo == null) return "Pilih Promo";
                    return promo.getNama_promo() + " (" +
                            (promo.getTipe_promo().equals("percentage") ?
                                    promo.getDiskon().intValue() + "%" :
                                    "Rp " + currencyFormat.format(promo.getDiskon())) + ")";
                }

                @Override
                public Promo fromString(String string) {
                    return cbPromo.getItems().stream()
                            .filter(promo -> promo != null && toString(promo).equals(string))
                            .findFirst()
                            .orElse(null);
                }
            });
            cbPromo.setPromptText("Pilih Promo");
        }

        private void setupSearchField() {
            tfSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                Platform.runLater(() -> filterJadwalTayang(newValue));
            });
            tfSearch.setOnKeyReleased(event -> {
                if (event.getCode().toString().equals("ENTER")) {
                    filterJadwalTayang(tfSearch.getText());
                }
            });
        }

        private boolean processPayment() {
            try {
                // 🔹 Validasi kursi di setiap item
                for (CartTicketItem item : cartItems) {
                    if (item.getKursiDipilih() == null || item.getKursiDipilih().isEmpty()) {
                        showErrorMessage("Beberapa item belum memiliki kursi yang dipilih!");
                        return false;
                    }
                }

                // 🔹 Validasi metode pembayaran
                Setting metodePembayaran = cbMetodePembayaran.getValue();
                if (metodePembayaran == null) {
                    showErrorMessage("Pilih metode pembayaran terlebih dahulu.");
                    return false;
                }

                // 🔹 Ambil promo (boleh null)
                Promo selectedPromo = cbPromo.getValue();

                // 🔹 Hitung total tiket dan total harga
                int jumlahTiket = 0;
                BigDecimal totalHarga = BigDecimal.ZERO;

                for (CartTicketItem item : cartItems) {
                    jumlahTiket += item.getQuantity();
                    totalHarga = totalHarga.add(item.getTotalPrice());
                }

                // 🔹 Gabungkan semua ID kursi yang dipilih
                String statusKursi = cartItems.stream()
                        .flatMap(item -> item.getKursiDipilih().stream())
                        .map(k -> String.valueOf(k.getId()))
                        .reduce((a, b) -> a + "," + b)
                        .orElse("-");

                // 🔹 Ambil pegawai, gunakan dummy jika null
                Karyawan pegawai = (currentUser != null) ? currentUser : new Karyawan();
                if (currentUser == null) {
                    pegawai.setId(-1);
                    pegawai.setNama("Guest");
                }

                // 🔹 Ambil jadwal pertama sebagai representasi (asumsi semua item sama jadwalnya)
                JadwalTayang jadwal = cartItems.get(0).getJadwal();

                // 🔹 Buat objek transaksi
                TransaksiPenjualanTiket transaksi = new TransaksiPenjualanTiket(
                        pegawai,
                        metodePembayaran,
                        selectedPromo,
                        jadwal,
                        LocalDateTime.now(),
                        jadwal.getHarga().doubleValue(),
                        jumlahTiket,
                        totalHarga.doubleValue(),
                        statusKursi,
                        pegawai.getNama(),
                        LocalDateTime.now()
                );

                // 🔹 Buat detail tiket berdasarkan kursi
                List<DetailPenjualanTiket> detailList = new ArrayList<>();
                for (CartTicketItem item : cartItems) {
                    for (Kursi kursi : item.getKursiDipilih()) {
                        DetailPenjualanTiket detail = new DetailPenjualanTiket();
                        detail.setTransaksi(transaksi);
                        detail.setKursi(kursi);
                        detail.setJumlah(1); // 1 kursi per baris
                        detail.setSubTotal(item.getHarga().doubleValue()); // harga per tiket
                        detailList.add(detail);
                    }
                }

                transaksi.setDetailPenjualanTiket(detailList);

                // 🔹 Simpan ke database
                return penjualanTiketImpl.saveData(transaksi);

            } catch (Exception e) {
                e.printStackTrace();
                showErrorMessage("Terjadi kesalahan saat menyimpan transaksi: " + e.getMessage());
                return false;
            }
        }


        private void handlePayment() {
            if (cartItems == null || cartItems.isEmpty()) {
                messageBox.alertWarning("Error! Keranjang tiket masih kosong!");
                return;
            }

            if (cbMetodePembayaran.getValue() == null) {
                messageBox.alertError("Error ! Pilih metode pembayaran terlebih dahulu!");
                return;
            }

            Setting metodePembayaran = cbMetodePembayaran.getValue();

            // Jika metode cash/tunai → validasi pembayaran tunai
            if (metodePembayaran.getNama().toLowerCase().contains("cash") ||
                    metodePembayaran.getNama().toLowerCase().contains("tunai")) {
                if (!validatePayment()) return;
            }

            showLoading(true);
            btnBayar.setDisable(true);

            new Thread(() -> {
                boolean success = processPayment();

                Platform.runLater(() -> {
                    showLoading(false);
                    btnBayar.setDisable(false);
                    if (success) {
                        showSuccessMessage("Pembayaran tiket berhasil!");
                        clearCart(); // Kosongkan keranjang
                        resetForm(); // Reset input
                    } else {
                        showErrorMessage("Gagal menyimpan transaksi ke database.");
                    }
                });
            }).start();
        }


        private boolean validatePayment() {
            if (cartItems.isEmpty()) {  // Validate cart
                showErrorMessage("Keranjang kosong! Silakan pilih tiket terlebih dahulu.");
                return false;
            }
            if (tfCustomerName.getText().trim().isEmpty()) {        // Validate customer name
                showErrorMessage("Nama pelanggan harus diisi!");
                tfCustomerName.requestFocus();
                return false;
            }
            if (cbMetodePembayaran.getSelectionModel().getSelectedItem() == null) { // Validate payment method
                showErrorMessage("Pilih metode pembayaran!");
                return false;
            }
            Setting paymentMethod = cbMetodePembayaran.getSelectionModel().getSelectedItem();    // Validate cash payment
            if ("Cash".equals(paymentMethod)) {
                if (tfUangBayar.getText().trim().isEmpty()) {
                    showErrorMessage("Masukkan jumlah uang bayar!");
                    tfUangBayar.requestFocus();
                    return false;
                }
                try {
                    double uangBayar = Double.parseDouble(tfUangBayar.getText().replaceAll("[^0-9]", ""));
                    double totalBayar = getCurrentTotalBayar();
                    if (uangBayar < totalBayar) {
                        showErrorMessage("Uang bayar kurang! Kekurangan: Rp " +
                                currencyFormat.format(totalBayar - uangBayar));
                        tfUangBayar.requestFocus();
                        return false;
                    }
                } catch (NumberFormatException e) {
                    showErrorMessage("Format uang bayar tidak valid!");
                    tfUangBayar.requestFocus();
                    return false;
                }
            }
            return true;
        }

        private void setupPaymentMethod() {
            // Load Metode Pembayaran data
            if (cbMetodePembayaran != null) {
                List<Setting> metodePembayaranList = penjualanTiketImpl.getMetodePembayaran();
                cbMetodePembayaran.getItems().clear();
                cbMetodePembayaran.getItems().addAll(metodePembayaranList);

                // Set default to first item if available
                if (!metodePembayaranList.isEmpty()) {
                    cbMetodePembayaran.setValue(metodePembayaranList.get(0));
                }
            }
        }

        private void setupScrollPanes() {
            scrollCard.setFitToWidth(true);
            scrollCard.setFitToHeight(false);
            scrollCard.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollCard.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            scrollCart.setFitToWidth(true);
            scrollCart.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollCart.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        }

        private void setupInputFields() {
            // Setup currency input field
            tfUangBayar.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    tfUangBayar.setText(newValue.replaceAll("[^\\d]", ""));
                }
                calculateKembalian();
            });
            // Setup customer name field
            tfCustomerName.setPromptText("Masukkan nama pelanggan");

            // Setup kembalian field as read-only
            tfKembalian.setEditable(false);
            tfKembalian.setStyle("-fx-background-color: #f0f0f0;");
        }

        private void setupButtons() {
            // Setup bayar button
            btnBayar.setDefaultButton(true);
            btnBayar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
            // Setup clear cart button
            btnClearCart.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            // Setup refresh button
            btnRefresh.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        }

        private void setupCartDisplay() {
            updateCartCount();
            labelSubtotal.setText("Rp 0");
            labelDiskon.setText("Rp 0");
            labelTotalBayar.setText("Rp 0");
        }

        private void setupAnimations() {
            // Add subtle fade-in animation for cards
            flowTiketCard.setOpacity(0);
            FadeTransition fade = new FadeTransition(Duration.millis(500), flowTiketCard);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        }

        private void loadInitialData() {
            showLoading(true);
            // Load data in background thread
            new Thread(() -> {
                try {
                    loadJadwalTayang();
                    loadPromoData();
                    Platform.runLater(() -> {
                        showLoading(false);
                        showStatusMessage("Data berhasil dimuat", false);
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showLoading(false);
                        showErrorMessage("Gagal memuat data: " + e.getMessage());
                    });
                }
            }).start();
        }

        private void loadJadwalTayang() {
            try {
                jadwalTayangList = jadwalTayangService.getAllData();

                if (jadwalTayangList != null) {
                    filteredJadwalList = jadwalTayangList.stream()
                            .filter(j -> j.getStatus() == 1)
                            .collect(Collectors.toList());

                    Platform.runLater(() -> displayJadwalCards(filteredJadwalList));
                }
            } catch (Exception e) {
                Platform.runLater(() -> showErrorMessage("Gagal memuat jadwal tayang: " + e.getMessage()));
            }
        }

        private void displayJadwalCards(List<JadwalTayang> jadwalList) {
            flowTiketCard.getChildren().clear();
            if (jadwalList == null || jadwalList.isEmpty()) {
                showEmptyState();
                return;
            }

            for (JadwalTayang jadwal : jadwalList) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/cinelux/bioskopcinelux/view/List/JadwalTiketCard.fxml"));
                    AnchorPane cardPane = loader.load();

                    JadwalCardCtrl cardController = loader.getController();

                    // ⬅️ Ini penting!
                    cardController.setMainController(this);

                    // Jalankan saat tiket dipilih
                    cardController.setData(jadwal, () -> handleTiketSelection(jadwal));

                    addHoverEffect(cardPane);

                    flowTiketCard.getChildren().add(cardPane);

                } catch (IOException e) {
                    showErrorMessage("Gagal memuat card jadwal: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        private List<Kursi> showKursiDialog(JadwalTayang jadwal) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/cinelux/bioskopcinelux/view/DashboardKasir/PemilihanKursi.fxml"));
                Parent root = loader.load();

                PemilihanKursiCtrl controller = loader.getController();
                Stage dialogStage = new Stage();
                dialogStage.initModality(Modality.APPLICATION_MODAL);
                dialogStage.setTitle("Pilih Kursi");
                dialogStage.setScene(new Scene(root));
                controller.setData(jadwal, dialogStage);
                controller.setMainController(this); // agar bisa callback

                dialogStage.showAndWait();

                // Setelah dialog ditutup, ambil kursi yang dipilih
                return controller.getKursiDipilih();

            } catch (IOException e) {
                e.printStackTrace();
                showErrorMessage("Gagal menampilkan dialog kursi: " + e.getMessage());
                return Collections.emptyList();
            }
        }


        public void handleTiketSelection(JadwalTayang jadwal) {
            try {
                // Tampilkan dialog pilih kursi
                List<Kursi> kursiTerpilih = showKursiDialog(jadwal);

                if (kursiTerpilih == null || kursiTerpilih.isEmpty()) {
                    showErrorMessage("Silakan pilih kursi terlebih dahulu.");
                    return;
                }

                CartTicketItem item = new CartTicketItem(jadwal, kursiTerpilih.size(),
                        this::updateCartDisplay,
                        () -> removeFromCartByJadwal(jadwal));

                item.setKursiDipilih(kursiTerpilih); // ✅ Set kursi ke item!

                cartItems.add(item);
                updateCartDisplay();
                updateCartCount();
                calculateTotalBayar();
                showStatusMessage("Tiket berhasil ditambahkan ke keranjang", false);

            } catch (Exception e) {
                showErrorMessage("Gagal menambahkan tiket: " + e.getMessage());
            }
        }

        private void updateQuantity(CartTicketItem item, int change) {
            int newQuantity = item.getQuantity() + change;
            if (newQuantity <= 0) {
                removeFromCart(item);
            } else {
                item.setQuantity(newQuantity);
                updateCartDisplay();
                updateCartCount();
                calculateTotalBayar();
            }
        }

        private void removeFromCart(CartTicketItem item) {
            cartItems.remove(item);
            updateCartDisplay();
            updateCartCount();
            calculateTotalBayar();
            showStatusMessage("Item dihapus dari keranjang", false);
        }

        private void updateCartCount() {
            int totalItems = cartItems.stream().mapToInt(CartTicketItem::getQuantity).sum();
            labelCartCount.setText(String.valueOf(totalItems));
        }

        private void filterJadwalTayang(String searchText) {
            if (jadwalTayangList == null) return;
            if (searchText == null || searchText.trim().isEmpty()) {
                filteredJadwalList = jadwalTayangList.stream()
                        .filter(j -> j.getStatus() == 1)
                        .collect(Collectors.toList());
            } else {
                filteredJadwalList = jadwalTayangList.stream()
                        .filter(j -> j.getStatus() == 1)
                        .filter(jadwal ->
                                jadwal.getNamaFilm().getJudul().toLowerCase().contains(searchText.toLowerCase()) ||
                                        jadwal.getNamaStudio().getNama().toLowerCase().contains(searchText.toLowerCase()) ||
                                        jadwal.getJenisHari().toLowerCase().contains(searchText.toLowerCase())
                        )
                        .collect(Collectors.toList());
            }
            displayJadwalCards(filteredJadwalList);
        }

        private void loadPromoData() {
            try {
                List<Promo> promoList = promoService.getAllData();

                Platform.runLater(() -> {
                    cbPromo.getItems().clear();
                    if (promoList != null && !promoList.isEmpty()) {
                        cbPromo.getItems().addAll(promoList);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showErrorMessage("Gagal memuat data promo: " + e.getMessage()));
            }
        }

        private void setupEventHandlers() {
            // Promo selection
            cbPromo.setOnAction(event -> calculateTotalBayar());
            // Payment button
            btnBayar.setOnAction(event -> handlePayment());
            // Clear cart button
            btnClearCart.setOnAction(event -> clearCart());
            // Refresh button
            btnRefresh.setOnAction(event -> refreshData());
            // Real-time calculation
            tfUangBayar.textProperty().addListener((obs, oldVal, newVal) -> calculateKembalian());
        }

        private void calculateKembalian() {
            try {
                if (tfUangBayar.getText().isEmpty()) {
                    tfKembalian.setText("Rp 0");
                    return;
                }
                double uangBayar = Double.parseDouble(tfUangBayar.getText().replaceAll("[^0-9]", ""));
                double totalBayar = getCurrentTotalBayar();
                double kembalian = uangBayar - totalBayar;
                if (kembalian >= 0) {
                    tfKembalian.setText("Rp " + currencyFormat.format(kembalian));
                    tfKembalian.setStyle("-fx-background-color: #e8f5e8; -fx-text-fill: #2e7d32;");
                } else {
                    tfKembalian.setText("Kurang: Rp " + currencyFormat.format(Math.abs(kembalian)));
                    tfKembalian.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828;");
                }
            } catch (NumberFormatException e) {
                tfKembalian.setText("Rp 0");
                tfKembalian.setStyle("-fx-background-color: #f0f0f0;");
            }
        }

        private void calculateTotalBayar() {
            BigDecimal subtotal = getCartSubtotal();
            BigDecimal discount = BigDecimal.ZERO;
            Promo selectedPromo = cbPromo.getSelectionModel().getSelectedItem();
            if (selectedPromo != null) {
                discount = calculatePromoDiscount(subtotal, selectedPromo);
            }
            BigDecimal total = subtotal.subtract(discount);
            labelSubtotal.setText("Rp " + currencyFormat.format(subtotal.doubleValue()));
            labelDiskon.setText("Rp " + currencyFormat.format(discount.doubleValue()));
            labelTotalBayar.setText("Rp " + currencyFormat.format(total.doubleValue()));
            // Recalculate kembalian
            calculateKembalian();
        }

        private double getCurrentTotalBayar() {
            try {
                String totalText = labelTotalBayar.getText().replaceAll("[^0-9]", "");
                return Double.parseDouble(totalText);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }

        private BigDecimal getCartSubtotal() {
            return cartItems.stream()
                    .map(item -> item.getHarga().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

        }

        private BigDecimal calculatePromoDiscount(BigDecimal subtotal, Promo promo) {
            if (promo == null) return BigDecimal.ZERO;

            BigDecimal discount = BigDecimal.ZERO;

            if ("percentage".equalsIgnoreCase(promo.getTipe_promo())) {
                // subtotal * (diskon / 100)
                BigDecimal percentage = BigDecimal.valueOf(promo.getDiskon()).divide(BigDecimal.valueOf(100));
                discount = subtotal.multiply(percentage);
            } else if ("fixed".equalsIgnoreCase(promo.getTipe_promo())) {
                discount = BigDecimal.valueOf(promo.getDiskon());
                // maksimal sebesar subtotal
                if (discount.compareTo(subtotal) > 0) {
                    discount = subtotal;
                }
            }
            return discount;
        }

        private void addHoverEffect(AnchorPane cardPane) {
            cardPane.setOnMouseEntered(event -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(100), cardPane);
                scale.setToX(1.05);
                scale.setToY(1.05);
                scale.play();

                DropShadow shadow = new DropShadow();
                shadow.setColor(Color.rgb(0, 0, 0, 0.3));
                shadow.setBlurType(javafx.scene.effect.BlurType.GAUSSIAN);
                shadow.setRadius(10);
                cardPane.setEffect(shadow);
            });
            cardPane.setOnMouseExited(event -> {
                ScaleTransition scale = new ScaleTransition(Duration.millis(100), cardPane);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();
                cardPane.setEffect(null);
            });
        }

        private void clearCart() {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Konfirmasi");
            alert.setHeaderText("Kosongkan Keranjang");
            alert.setContentText("Apakah Anda yakin ingin mengosongkan keranjang?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    cartItems.clear();
                    updateCartDisplay();
                    updateCartCount();
                    calculateTotalBayar();
                    showStatusMessage("Keranjang dikosongkan", false);
                }
            });
        }

        private void refreshData() {
            showStatusMessage("Memuat ulang data...", false);
            loadInitialData();
        }

        public void refreshJadwalTayang() {
            refreshData();
        }

        private void resetForm() {
            tfSearch.clear();
            tfUangBayar.clear();
            tfKembalian.clear();
            tfCustomerName.clear();
            cbPromo.getSelectionModel().clearSelection();
            cbMetodePembayaran.getSelectionModel().selectFirst();
            cartItems.clear();
            updateCartDisplay();
            updateCartCount();
            calculateTotalBayar();
        }

        private void showLoading(boolean show) {
            loadingIndicator.setVisible(show);
            btnBayar.setDisable(show);
        }

        private void showEmptyState() {
            VBox emptyBox = new VBox(20);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setStyle("-fx-padding: 50;");
            Label emptyLabel = new Label("Tidak ada jadwal tayang tersedia");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #888888; -fx-font-weight: bold;");
            Button refreshBtn = new Button("Muat Ulang");
            refreshBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
            refreshBtn.setOnAction(e -> refreshData());
            emptyBox.getChildren().addAll(emptyLabel, refreshBtn);
            flowTiketCard.getChildren().add(emptyBox);
        }
//            private void showErrorMessage(String message) {
//                Alert alert = new Alert(Alert.AlertType.ERROR);
//                alert.setTitle("Error");
//                alert.setHeaderText(null);
//                alert.setContentText(message);
//                alert.showAndWait();
//                showStatusMessage(message, true);
//            }

        private void showErrorMessage(String message) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Terjadi Kesalahan");
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            });
        }

        private void showSuccessMessage(String message) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sukses");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
            showStatusMessage(message, false);
        }

        private void showStatusMessage(String message, boolean isError) {
            statusLabel.setText(message);
            statusLabel.setStyle(isError ?
                    "-fx-text-fill: #f44336; -fx-font-weight: bold;" :
                    "-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
            new Thread(() -> {      // Auto-hide after 3 seconds
                try {
                    Thread.sleep(3000);
                    Platform.runLater(() -> statusLabel.setText(""));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        private boolean isPromoValid(Promo promo) {
            if (promo == null) return false;

            // Validasi tanggal aktif promo
            java.time.LocalDate now = java.time.LocalDate.now();
            if (promo.getTanggal_mulai() != null && now.isBefore(promo.getTanggal_mulai().toLocalDate())) {
                return false;
            }
            if (promo.getTanggal_selesai() != null && now.isAfter(promo.getTanggal_selesai().toLocalDate())) {
                return false;
            }
            return true;
        }

        // 5. Method untuk animasi button
        private void animateButton(Button button) {
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(100), button);
            scaleOut.setToX(0.95);
            scaleOut.setToY(0.95);
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(100), button);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            scaleOut.setOnFinished(e -> scaleIn.play());
            scaleOut.play();
        }

        // 8. Method untuk setup tooltip
        private void setupTooltips() {
            // Tooltip untuk search
            Tooltip searchTooltip = new Tooltip("Cari berdasarkan judul film, studio, atau hari");
            searchTooltip.setShowDelay(Duration.millis(500));
            tfSearch.setTooltip(searchTooltip);
            // Tooltip untuk promo
            Tooltip promoTooltip = new Tooltip("Pilih promo untuk mendapatkan diskon");
            promoTooltip.setShowDelay(Duration.millis(500));
            cbPromo.setTooltip(promoTooltip);
            // Tooltip untuk payment method
            Tooltip paymentTooltip = new Tooltip("Pilih metode pembayaran");
            paymentTooltip.setShowDelay(Duration.millis(500));
            cbMetodePembayaran.setTooltip(paymentTooltip);
            // Tooltip untuk customer name
            Tooltip customerTooltip = new Tooltip("Masukkan nama pelanggan untuk transaksi");
            customerTooltip.setShowDelay(Duration.millis(500));
            tfCustomerName.setTooltip(customerTooltip);
        }

        private String formatCurrency(BigDecimal amount) {
            NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("id", "ID"));
            currencyFormat.setMaximumFractionDigits(0);
            return "Rp " + currencyFormat.format(amount);
        }

        private VBox createEnhancedCartItemComponent(CartTicketItem item) {
            VBox itemBox = new VBox(8);
            itemBox.setStyle("-fx-background-color: linear-gradient(to bottom, #ffffff, #f8f9fa); " +
                    "-fx-background-radius: 12; -fx-padding: 15; " +
                    "-fx-border-color: #e3f2fd; -fx-border-radius: 12; " +
                    "-fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
            // Header dengan film info
            HBox headerBox = new HBox(10);
            headerBox.setAlignment(Pos.CENTER_LEFT);
            // Film title dengan styling yang lebih baik
            Label titleLabel = new Label(item.getJadwal().getNamaFilm().getJudul());
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1976d2;");
            titleLabel.setMaxWidth(200);
            titleLabel.setWrapText(true);
            // Status badge
            Label statusBadge = new Label("ACTIVE");
            statusBadge.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; " +
                    "-fx-font-size: 10px; -fx-padding: 2 8; -fx-background-radius: 10;");
            headerBox.getChildren().addAll(titleLabel, statusBadge);
            VBox detailBox = new VBox(4);   //// Detail info dengan icon-style
            Label studioLabel = new Label("🎬 " + item.getJadwal().getNamaStudio().getNama());
            studioLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");
            Label timeLabel = new Label("🕐 " + item.getJadwal().getJamMulai().toLocalTime());
            timeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");
            Label dateLabel = new Label("📅 " + item.getJadwal().getJamMulai().toString());
            dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");
            detailBox.getChildren().addAll(studioLabel, timeLabel, dateLabel);
            // Quantity control dengan styling yang lebih baik
            HBox quantityBox = new HBox(15);
            quantityBox.setAlignment(Pos.CENTER_LEFT);
            quantityBox.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 8; -fx-padding: 8;");

            Button btnMinus = new Button("−");
            btnMinus.setStyle("-fx-background-color: #ff5722; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-pref-width: 30; -fx-pref-height: 30; " +
                    "-fx-background-radius: 15; -fx-font-size: 16px;");
            btnMinus.setOnAction(e -> {
                animateButton(btnMinus);
                updateQuantity(item, -1);
            });
            Label quantityLabel = new Label(String.valueOf(item.getQuantity()));
            quantityLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 40; -fx-alignment: center; " +
                    "-fx-font-size: 16px; -fx-text-fill: #1976d2;");
            Button btnPlus = new Button("+");
            btnPlus.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-pref-width: 30; -fx-pref-height: 30; " +
                    "-fx-background-radius: 15; -fx-font-size: 16px;");
            btnPlus.setOnAction(e -> {
                animateButton(btnPlus);
                updateQuantity(item, 1);
            });
            // Price dengan highlighting
            VBox priceBox = new VBox(2);
            priceBox.setAlignment(Pos.CENTER_RIGHT);

            Label unitPriceLabel = new Label("@ " + formatCurrency(item.getHarga()));
            unitPriceLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888;");

            BigDecimal total = item.getHarga().multiply(BigDecimal.valueOf(item.getQuantity()));
            Label totalPriceLabel = new Label(formatCurrency(total));
            totalPriceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1976d2; -fx-font-size: 16px;");
            priceBox.getChildren().addAll(unitPriceLabel, totalPriceLabel);

            // Remove button dengan styling yang lebih baik
            Button btnRemove = new Button("🗑️");
            btnRemove.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; " +
                    "-fx-font-size: 12px; -fx-pref-width: 35; -fx-pref-height: 35; " +
                    "-fx-background-radius: 17; -fx-cursor: hand;");
            btnRemove.setOnAction(e -> {
                animateButton(btnRemove);
                removeFromCart(item);
            });
            btnRemove.setOnMouseEntered(e -> btnRemove.setStyle(btnRemove.getStyle() + "-fx-scale-x: 1.1; -fx-scale-y: 1.1;"));
            btnRemove.setOnMouseExited(e -> btnRemove.setStyle(btnRemove.getStyle().replace("-fx-scale-x: 1.1; -fx-scale-y: 1.1;", "")));
            quantityBox.getChildren().addAll(btnMinus, quantityLabel, btnPlus,
                    createSpacer(), priceBox, btnRemove);
            itemBox.getChildren().addAll(headerBox, detailBox, quantityBox);
            return itemBox;
        }

        private Region createSpacer() {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            return spacer;
        }

        private BigDecimal calculateEnhancedPromoDiscount(BigDecimal subtotal, Promo promo) {
            if (promo == null || !isPromoValid(promo)) return BigDecimal.ZERO;

            BigDecimal discount = BigDecimal.ZERO;

            switch (promo.getTipe_promo().toLowerCase()) {
                case "percentage" -> {
                    BigDecimal percentage = BigDecimal.valueOf(promo.getDiskon()).divide(BigDecimal.valueOf(100));
                    discount = subtotal.multiply(percentage);

                    // Cek maksimal diskon (jika promo.getDiskon() digunakan juga sebagai max discount)
                    if (promo.getDiskon() != null && promo.getDiskon() > 0) {
                        BigDecimal maxDiskon = BigDecimal.valueOf(promo.getDiskon());
                        if (discount.compareTo(maxDiskon) > 0) {
                            discount = maxDiskon;
                        }
                    }
                }
                case "fixed" -> {
                    discount = BigDecimal.valueOf(promo.getDiskon());
                    if (discount.compareTo(subtotal) > 0) {
                        discount = subtotal;
                    }
                }
            }

            return discount;
        }

        private void setupEnhancedComponents() {
            setupTooltips();         // Setup tooltips
            setupEnhancedButtonStyles();    // Enhanced button styles
            setupEnhancedInputStyles();  // Enhanced input field styles
            setupKeyboardShortcuts();   // Setup keyboard shortcuts
        }

        // 16. Method untuk enhanced button styles
        private void setupEnhancedButtonStyles() {
            // Bayar button dengan gradient
            btnBayar.setStyle("-fx-background-color: linear-gradient(to bottom, #66bb6a, #4caf50); " +
                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; " +
                    "-fx-background-radius: 8; -fx-padding: 12 24; -fx-cursor: hand;");
            // Clear cart button
            btnClearCart.setStyle("-fx-background-color: linear-gradient(to bottom, #ef5350, #f44336); " +
                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; " +
                    "-fx-padding: 8 16; -fx-cursor: hand;");
            // Refresh button
            btnRefresh.setStyle("-fx-background-color: linear-gradient(to bottom, #42a5f5, #2196f3); " +
                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; " +
                    "-fx-padding: 8 16; -fx-cursor: hand;");
        }

        // 17. Method untuk enhanced input styles
        private void setupEnhancedInputStyles() {
            // Search field
            tfSearch.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e3f2fd; " +
                    "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; " +
                    "-fx-font-size: 14px;");
            // Customer name field
            tfCustomerName.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e3f2fd; " +
                    "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; " +
                    "-fx-font-size: 14px;");
            // Uang bayar field
            tfUangBayar.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e3f2fd; " +
                    "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; " +
                    "-fx-font-size: 14px;");
            // Kembalian field
            tfKembalian.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #e0e0e0; " +
                    "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; " +
                    "-fx-font-size: 14px; -fx-font-weight: bold;");
        }

        // 18. Method untuk keyboard shortcuts
        private void setupKeyboardShortcuts() {
            // F1 untuk refresh
            Platform.runLater(() -> {
                tfSearch.getScene().setOnKeyPressed(event -> {
                    switch (event.getCode()) {
                        case F1:
                            refreshData();
                            break;
                        case F2:
                            tfCustomerName.requestFocus();
                            break;
                        case F3:
                            if (!cartItems.isEmpty()) {
                                btnBayar.fire();
                            }
                            break;
                        case ESCAPE:
                            tfSearch.clear();
                            break;
                    }
                });
            });
        }

        // Method untuk update cart display yang enhanced
        private void updateEnhancedCartDisplay() {
            cartContent.getChildren().clear();

            if (cartItems.isEmpty()) {
                VBox emptyCartBox = new VBox(20);
                emptyCartBox.setAlignment(Pos.CENTER);
                emptyCartBox.setStyle("-fx-padding: 30;");

                Label emptyIcon = new Label("🛒");
                emptyIcon.setStyle("-fx-font-size: 48px;");

                Label emptyLabel = new Label("Keranjang Kosong");
                emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #888888; -fx-font-weight: bold;");

                Label emptySubLabel = new Label("Pilih tiket untuk memulai transaksi");
                emptySubLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");

                emptyCartBox.getChildren().addAll(emptyIcon, emptyLabel, emptySubLabel);
                cartContent.getChildren().add(emptyCartBox);
                return;
            }

            for (CartTicketItem item : cartItems) {
                VBox cartItemBox = createEnhancedCartItemComponent(item);
                cartContent.getChildren().add(cartItemBox);
            }
        }

        private void updateCartDisplay() {
            updateEnhancedCartDisplay();
        }
    }