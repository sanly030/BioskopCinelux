package cinelux.bioskopcinelux.model;

public class DetailPenjualanMenu {
    private TransaksiPenjualanMenu transaksi; // referensi ke transaksi utama
    private Menu menu;
    private int jumlah;
    private Double subTotal;

    public DetailPenjualanMenu() {
    }

    public DetailPenjualanMenu(TransaksiPenjualanMenu transaksi, Menu menu, int jumlah, Double subTotal) {
        this.transaksi = transaksi;
        this.menu = menu;
        this.jumlah = jumlah;
        this.subTotal = subTotal;
    }

    public TransaksiPenjualanMenu getTransaksi() {
        return transaksi;
    }

    public void setTransaksi(TransaksiPenjualanMenu transaksi) {
        this.transaksi = transaksi;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public Double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(Double subTotal) {
        this.subTotal = subTotal;
    }
}