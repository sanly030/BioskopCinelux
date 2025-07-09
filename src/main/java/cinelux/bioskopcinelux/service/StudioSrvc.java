package cinelux.bioskopcinelux.service;

import cinelux.bioskopcinelux.model.Studio;
import cinelux.bioskopcinelux.util.OperationResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface StudioSrvc {
    // Mapping dari ResultSet ke objek Studio
    Studio mapResultSetToStudio(ResultSet rs) throws SQLException;

    // Ambil semua data studio tanpa filter
    List<Studio> getAllData();

    // Ambil semua data studio dengan filter pencarian dan sorting
    List<Studio> getAllData(String search, Integer status, String urutan, String sortBy);

    // Ambil studio berdasarkan ID
    Studio getById(int id);

    // Ambil ID terakhir studio
    int getLastId();

    // Insert data studio baru
    OperationResult InsertStudioWithKursi(Studio studio);

    // Update data studio
    OperationResult updateStudioWithKursi(Studio studio);

    // Hapus / nonaktifkan studio
    OperationResult deleteData(int id, String modifiedBy);

    // Toggle status aktif/nonaktif studio
    OperationResult toogleStatus(int id);
}

