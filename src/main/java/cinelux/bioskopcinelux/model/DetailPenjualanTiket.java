package cinelux.bioskopcinelux.model;

public class DetailPenjualanTiket {
    private TransaksiPenjualanTiket transaksi; // referensi ke transaksi utama
    private Kursi kursi;
    private int jumlah;
    private Double subTotal;

    public DetailPenjualanTiket() {
    }

    public DetailPenjualanTiket(TransaksiPenjualanTiket transaksi, Kursi kursi, int jumlah, Double subTotal) {
        this.transaksi = transaksi;
        this.kursi = kursi;
        this.jumlah = jumlah;
        this.subTotal = subTotal;
    }

    public TransaksiPenjualanTiket getTransaksi() {
        return transaksi;
    }

    public void setTransaksi(TransaksiPenjualanTiket transaksi) {
        this.transaksi = transaksi;
    }

    public Kursi getKursi() {
        return kursi;
    }

    public void setKursi(Kursi kursi) {
        this.kursi = kursi;
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
