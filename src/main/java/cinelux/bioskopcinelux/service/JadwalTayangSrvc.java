package cinelux.bioskopcinelux.service;

import cinelux.bioskopcinelux.model.JadwalTayang;
import cinelux.bioskopcinelux.util.OperationResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface JadwalTayangSrvc {
    // Mapping dari ResultSet ke objek JadwalTayang
    JadwalTayang mapResultSetToJadwalTayang(ResultSet rs) throws SQLException;

    // Ambil semua data jadwal tayang
    List<JadwalTayang> getAllData();

    // Ambil semua data dengan filter
    List<JadwalTayang> getAllData(String search, String jenisHari, Integer status, String urutan, String sortBy);

    // Ambil data berdasarkan ID
    JadwalTayang getById(int id);

    // Ambil ID terakhir (biasanya untuk generate ID baru)
    int getLastId();

    // Insert data baru
    OperationResult insertData(JadwalTayang jadwalTayang);

    // Hapus data jadwal tayang
    OperationResult deleteData(int id, String modifiedby);

    // Update data jadwal tayang
    OperationResult updateData(JadwalTayang jadwalTayang);

    // Toggle status aktif/nonaktif
    OperationResult toogleStatus(int id);

    // Cek bentrok jadwal berdasarkan studio dan jam
    boolean isJadwalBentrok(String jamMulai, String jamSelesai, int idStudio);
}
