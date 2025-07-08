package cinelux.bioskopcinelux.service;

import cinelux.bioskopcinelux.model.Promo;
import cinelux.bioskopcinelux.util.OperationResult;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface PromoSrvc {
    // Mapping dari ResultSet ke objek Promo
    Promo mapResultSetToPromo(ResultSet rs) throws SQLException;
    // Ambil semua data promo
    List<Promo> getAllData();
    // Ambil semua data dengan filter
    List<Promo> getAllData(String search, String tipePromo, Integer status, String urutan, String sortBy);
    // Ambil data berdasarkan ID
    Promo getById(int id);
    // Ambil ID terakhir (biasanya untuk generate ID baru)
    int getLastId();
    // Insert data baru
    OperationResult insertData(Promo promo);
    // Hapus data promo
    OperationResult deleteData(int id);
    // Update data promo
    OperationResult updateData(Promo promo);
    // Toggle status aktif/nonaktif
    OperationResult toogleStatus(int id);
}

