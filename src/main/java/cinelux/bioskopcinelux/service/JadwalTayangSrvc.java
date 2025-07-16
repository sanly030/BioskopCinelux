package cinelux.bioskopcinelux.service;

import cinelux.bioskopcinelux.model.JadwalTayang;
import cinelux.bioskopcinelux.util.OperationResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface JadwalTayangSrvc {

    // Mapping dari ResultSet ke objek JadwalTayang
    JadwalTayang mapResultSetToJadwalTayang(ResultSet rs) throws SQLException;

    // Ambil semua data jadwal tayang (tanpa filter)
    List<JadwalTayang> getAllData();

    // Ambil semua data jadwal tayang dengan filter pencarian
    List<JadwalTayang> getAllData(String search, String jenisHari, Integer status, String urutan, String sortBy);

    // Ambil data berdasarkan ID
    JadwalTayang getById(int id);

    // Ambil ID terakhir yang di-generate
    int getLastId();

    // Tambah jadwal tayang baru
    OperationResult insertData(JadwalTayang jadwalTayang);

    // Hapus data jadwal tayang (soft delete atau hard delete)
    OperationResult deleteData(int id, String modifiedBy);

    // Update data jadwal tayang
    OperationResult updateData(JadwalTayang jadwalTayang);

    // Ubah status aktif/nonaktif jadwal tayang
    OperationResult toogleStatus(int id);

    // Cek apakah jadwal bentrok berdasarkan studio dan waktu tayang
    boolean isJadwalBentrok(String jamMulai, String jamSelesai, int idStudio);
}
