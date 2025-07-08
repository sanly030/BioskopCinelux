package cinelux.bioskopcinelux.service;

import cinelux.bioskopcinelux.model.Film;
import cinelux.bioskopcinelux.util.OperationResult;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface FilmSrvc {
    // Mapping dari ResultSet ke objek Film
    Film mapResultSetToFilm(ResultSet rs) throws SQLException;
    // Ambil semua data tanpa filter
    List<Film> getAllData();
    // Ambil data dengan filter pencarian, genre, status, urutan dan sortBy (opsional)
    List<Film> getAllData(String search, String genre, Integer status, String urutan, String sortBy);
    // Ambil data berdasarkan ID
    Film getById(int id);
    // Ambil ID terakhir (untuk generate ID baru)
    int getLastId();
    // Insert data baru
    OperationResult insertData(Film film);
    // Update data
    OperationResult updateData(Film film);
    // Hapus atau toggle status film (aktif/tidak aktif)
    OperationResult deleteData(int id, String modifiedBy);
    // Toggle status aktif/nonaktif
    OperationResult toogleStatus(int id);
}
