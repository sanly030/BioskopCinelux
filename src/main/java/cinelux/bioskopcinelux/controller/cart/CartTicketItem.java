package cinelux.bioskopcinelux.controller.cart;

import cinelux.bioskopcinelux.model.JadwalTayang;
import cinelux.bioskopcinelux.model.Kursi;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.time.LocalTime;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CartTicketItem extends VBox {

    private JadwalTayang jadwal;
    private int quantity;
    private BigDecimal totalPrice;
    private Runnable onQuantityChanged;
    private Runnable onRemove;

    private List<Kursi> kursiDipilih = new ArrayList<>();

    private TextField tfQuantity;
    private Label totalLabel;

    public CartTicketItem(JadwalTayang jadwal, int quantity, Runnable onQuantityChanged, Runnable onRemove) {
        this.jadwal = jadwal;
        this.quantity = quantity;
        this.onQuantityChanged = onQuantityChanged;
        this.onRemove = onRemove;
        this.totalPrice = jadwal.getHarga().multiply(BigDecimal.valueOf(quantity));

        initializeItem();
        setupStyling();
    }

    // Overload constructor jika ingin langsung set kursi
    public CartTicketItem(JadwalTayang jadwal, int quantity, List<Kursi> kursiDipilih, Runnable onQuantityChanged, Runnable onRemove) {
        this(jadwal, quantity, onQuantityChanged, onRemove);
        this.kursiDipilih = kursiDipilih != null ? new ArrayList<>(kursiDipilih) : new ArrayList<>();
    }

    public void
    setKursiDipilih(List<Kursi> kursiDipilih) {
        this.kursiDipilih = kursiDipilih != null ? new ArrayList<>(kursiDipilih) : new ArrayList<>();
    }

    public List<Kursi> getKursiDipilih() {
        return kursiDipilih;
    }

    // Method untuk menambah kursi ke item yang sudah ada
    public void addKursi(List<Kursi> kursiTambahan) {
        if (kursiTambahan != null && !kursiTambahan.isEmpty()) {
            if (this.kursiDipilih == null) {
                this.kursiDipilih = new ArrayList<>();
            }
            this.kursiDipilih.addAll(kursiTambahan);
        }
    }

    // Method untuk mendapatkan string nomor kursi
    public String getKursiString() {
        if (kursiDipilih == null || kursiDipilih.isEmpty()) {
            return "Kursi belum dipilih";
        }

        List<String> nomorKursi = kursiDipilih.stream()
                .map(Kursi::getNomor)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());

        return nomorKursi.isEmpty() ? "Kursi belum dipilih" : String.join(", ", nomorKursi);
    }

    private void initializeItem() {
        this.setPrefWidth(450);
        this.setSpacing(10);
        this.setPadding(new Insets(15));

        HBox headerBox = createHeaderBox();
        VBox detailsBox = createDetailsBox();
        HBox controlsBox = createControlsBox();

        this.getChildren().addAll(headerBox, detailsBox, controlsBox);
    }

    private HBox createHeaderBox() {
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(jadwal.getNamaFilm() != null ? jadwal.getNamaFilm().getJudul() : "Film Tidak Diketahui");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        titleLabel.setStyle("-fx-text-fill: white;");
        titleLabel.setMaxWidth(300);
        titleLabel.setWrapText(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button removeButton = new Button("🗑️");
        removeButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        removeButton.setStyle(getRemoveStyle(false));

        removeButton.setOnMouseEntered(e -> removeButton.setStyle(getRemoveStyle(true)));
        removeButton.setOnMouseExited(e -> removeButton.setStyle(getRemoveStyle(false)));
        removeButton.setOnAction(e -> {
            if (onRemove != null) onRemove.run();
        });

        headerBox.getChildren().addAll(titleLabel, spacer, removeButton);
        return headerBox;
    }

    private VBox createDetailsBox() {
        VBox detailsBox = new VBox(5);
        detailsBox.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8; -fx-padding: 8;");

        Label studioLabel = new Label("🎬 " + (jadwal.getNamaStudio() != null ? jadwal.getNamaStudio().getNama() : "Studio"));
        studioLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        studioLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8);");

        HBox dateTimeBox = new HBox(15);
        dateTimeBox.setAlignment(Pos.CENTER_LEFT);

        Label dateLabel = new Label("📅 " + (jadwal.getJenisHari() != null ? jadwal.getJenisHari().toString() : "Tanggal"));
        dateLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        dateLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8);");

        String formattedTime = "-";
        if (jadwal.getJamMulai() != null) {
            try {
                LocalTime localTime = jadwal.getJamMulai().toLocalTime();
                formattedTime = localTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception ignored) {}
        }
        Label timeLabel = new Label("🕐 " + formattedTime);
        timeLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        timeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8);");

        dateTimeBox.getChildren().addAll(dateLabel, timeLabel);

        Label priceLabel = new Label("💰 Rp " + String.format("%,.0f", jadwal.getHarga()) + " /tiket");
        priceLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        priceLabel.setStyle("-fx-text-fill: #FFD700;");

        // Tambahkan label untuk menampilkan kursi yang dipilih
        Label kursiLabel = new Label("🪑 Kursi: " + getKursiString());
        kursiLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        kursiLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8);");
        kursiLabel.setMaxWidth(400);
        kursiLabel.setWrapText(true);

        detailsBox.getChildren().addAll(studioLabel, dateTimeBox, priceLabel, kursiLabel);
        return detailsBox;
    }

    private HBox createControlsBox() {
        HBox controlsBox = new HBox(10);
        controlsBox.setAlignment(Pos.CENTER_LEFT);

        Label qtyLabel = new Label("Jumlah:");
        qtyLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        qtyLabel.setStyle("-fx-text-fill: white;");

        Button minusButton = createQtyButton("-", () -> {
            if (quantity > 1) {
                quantity--;
                updateQuantity();
            }
        });

        tfQuantity = new TextField(String.valueOf(quantity));
        tfQuantity.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        tfQuantity.setStyle("""
            -fx-background-color: rgba(255,255,255,0.9);
            -fx-text-fill: #333;
            -fx-background-radius: 8;
            -fx-alignment: center;
            -fx-pref-width: 50;
            -fx-max-width: 50;
        """);
        tfQuantity.setEditable(false);

        Button plusButton = createQtyButton("+", () -> {
            quantity++;
            updateQuantity();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        totalLabel = new Label("Total: Rp " + String.format("%,.0f", totalPrice));
        totalLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        totalLabel.setStyle("-fx-text-fill: #FFD700;");

        controlsBox.getChildren().addAll(qtyLabel, minusButton, tfQuantity, plusButton, spacer, totalLabel);
        return controlsBox;
    }

    private Button createQtyButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btn.setStyle(getQtyButtonStyle(false));
        btn.setOnMouseEntered(e -> btn.setStyle(getQtyButtonStyle(true)));
        btn.setOnMouseExited(e -> btn.setStyle(getQtyButtonStyle(false)));
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private String getRemoveStyle(boolean hovered) {
        return hovered ?
                "-fx-background-color: rgba(255, 69, 58, 1.0); -fx-text-fill: white; -fx-background-radius: 15;" :
                "-fx-background-color: rgba(255, 69, 58, 0.8); -fx-text-fill: white; -fx-background-radius: 15;";
    }

    private String getQtyButtonStyle(boolean hovered) {
        String baseColor = hovered ? "0.3" : "0.2";
        return "-fx-background-color: rgba(255,255,255," + baseColor + ");" +
                "-fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand;" +
                "-fx-min-width: 30; -fx-min-height: 30; -fx-max-width: 30; -fx-max-height: 30;";
    }

    private void updateQuantity() {
        tfQuantity.setText(String.valueOf(quantity));
        totalPrice = jadwal.getHarga().multiply(BigDecimal.valueOf(quantity));
        totalLabel.setText("Total: Rp " + String.format("%,.0f", totalPrice));
        if (onQuantityChanged != null) onQuantityChanged.run();
    }

    private void setupStyling() {
        this.setStyle("""
            -fx-background-color: linear-gradient(to right, rgba(139, 69, 19, 0.3), rgba(160, 82, 45, 0.3));
            -fx-background-radius: 12;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);
            -fx-border-color: rgba(255,255,255,0.2);
            -fx-border-width: 1;
            -fx-border-radius: 12;
        """);

        this.setOnMouseEntered(e -> this.setStyle("""
            -fx-background-color: linear-gradient(to right, rgba(139, 69, 19, 0.4), rgba(160, 82, 45, 0.4));
            -fx-background-radius: 12;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 8, 0, 0, 3);
            -fx-border-color: rgba(255,255,255,0.3);
            -fx-border-width: 1;
            -fx-border-radius: 12;
        """));

        this.setOnMouseExited(e -> this.setStyle("""
            -fx-background-color: linear-gradient(to right, rgba(139, 69, 19, 0.3), rgba(160, 82, 45, 0.3));
            -fx-background-radius: 12;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);
            -fx-border-color: rgba(255,255,255,0.2);
            -fx-border-width: 1;
            -fx-border-radius: 12;
        """));
    }

    public JadwalTayang getJadwal() {
        return jadwal;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        updateQuantity();
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public BigDecimal getHarga() {
        return jadwal.getHarga();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CartTicketItem that = (CartTicketItem) obj;
        return Objects.equals(jadwal, that.jadwal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jadwal);
    }
}