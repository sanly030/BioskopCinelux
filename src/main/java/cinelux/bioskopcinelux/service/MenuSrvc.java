package cinelux.bioskopcinelux.service;

import cinelux.bioskopcinelux.model.Menu;
import cinelux.bioskopcinelux.util.OperationResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface MenuSrvc {
    // Untuk mapping ResultSet ke objek Menu
    Menu mapResultSetToDetailSetting(ResultSet rs) throws SQLException;

    // Ambil semua data menu tanpa filter
    List<Menu> getAllData();

    // Ambil semua data menu dengan filter (misalnya pencarian dan sorting)
    List<Menu> getAllData(String search, String kategori, Integer status, String urutan, String sortBy);

    // Ambil menu berdasarkan ID
    Menu getById(int id);

    // Ambil ID terakhir (biasanya untuk auto increment check atau penambahan manual)
    int getLastId();

    // Insert data baru
    OperationResult insertData(Menu menu);

    // Update data menu
    OperationResult updateData(Menu menu);

    // Menghapus atau menonaktifkan menu berdasarkan ID
    OperationResult deleteData(int id, String modifiedBy);

    // Mengaktifkan atau menonaktifkan menu (toggle)
    OperationResult toogleStatus(int id);
}

