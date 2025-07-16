package cinelux.bioskopcinelux.service;

import cinelux.bioskopcinelux.model.TransaksiPenjualanMenu;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface TransaksiPenjualanMenuSrvc {
    TransaksiPenjualanMenu resultTransaksi(ResultSet rs) throws SQLException;

    List<TransaksiPenjualanMenu> getAllData();

    List<TransaksiPenjualanMenu> getAllData(String search, String status, Integer metodePembayaranId, String sortColumn, String sortOrder);

    boolean saveData(TransaksiPenjualanMenu transaksi);
}
