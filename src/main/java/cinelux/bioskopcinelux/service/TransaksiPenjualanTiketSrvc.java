package cinelux.bioskopcinelux.service;

import cinelux.bioskopcinelux.model.TransaksiPenjualanTiket;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface TransaksiPenjualanTiketSrvc {
    TransaksiPenjualanTiket resultTransaksi(ResultSet rs) throws SQLException;

    List<TransaksiPenjualanTiket> getAllData();

    List<TransaksiPenjualanTiket> getAllData(String search, String status, Integer metodePembayaranId, String sortColumn, String sortOrder);

    boolean saveData(TransaksiPenjualanTiket transaksi);
}
