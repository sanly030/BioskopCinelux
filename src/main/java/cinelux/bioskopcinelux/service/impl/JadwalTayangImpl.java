package cinelux.bioskopcinelux.service.impl;

import cinelux.bioskopcinelux.model.JadwalTayang;
import cinelux.bioskopcinelux.model.Film;
import cinelux.bioskopcinelux.model.Studio;
import cinelux.bioskopcinelux.model.Role;
import cinelux.bioskopcinelux.connection.DBConnect;
import cinelux.bioskopcinelux.service.JadwalTayangSrvc;
import cinelux.bioskopcinelux.util.OperationResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JadwalTayangImpl implements JadwalTayangSrvc {

    @Override
    public JadwalTayang mapResultSetToJadwalTayang(ResultSet rs) throws SQLException {
        Film film = new Film(
                rs.getInt("flm_id"),
                rs.getString("flm_judul"),
                rs.getString("flm_genre"),
                rs.getInt("flm_durasi"),
                rs.getString("flm_rating_usia"),
                rs.getString("flm_poster"),
                rs.getInt("flm_status"),
                rs.getString("flm_created_by"),
                rs.getString("flm_modif_by")
        );

        Studio studio = new Studio(
                rs.getInt("std_id"),
                rs.getString("std_nama"),
                rs.getInt("std_kapasitas"),
                rs.getInt("std_status"),
                rs.getString("std_created_by"),
                rs.getString("std_modif_by")
        );

        return new JadwalTayang(
                rs.getInt("jty_id"),
                film,
                studio,
                rs.getTime("jty_jam_mulai"),
                rs.getTime("jty_jam_selesai"),
                rs.getString("jty_jenis_hari"),
                rs.getBigDecimal("jty_harga_tiket"),
                rs.getInt("jty_status"),
                rs.getString("jty_created_by"),
                rs.getString("jty_modif_by")
        );
    }

    @Override
    public List<JadwalTayang> getAllData() {
        return getAllData(null, null, 1, null, null);
    }

    @Override
    public List<JadwalTayang> getAllData(String search, String jenisHari, Integer status, String urutan, String sortBy) {
        DBConnect connect = new DBConnect();
        List<JadwalTayang> list = new ArrayList<>();
        try {
            String sql = "{CALL sp_GetListJadwalTayang(?, ?, ?, ?, ?)}";
            connect.pstat = connect.conn.prepareCall(sql);

            connect.pstat.setObject(1, search, Types.VARCHAR);
            connect.pstat.setObject(2, jenisHari, Types.VARCHAR);
            connect.pstat.setObject(3, status, Types.INTEGER);
            connect.pstat.setObject(4, urutan, Types.VARCHAR);
            connect.pstat.setObject(5, sortBy, Types.VARCHAR);

            connect.result = connect.pstat.executeQuery();
            while (connect.result.next()) {
                list.add(mapResultSetToJadwalTayang(connect.result));
            }
        } catch (SQLException e) {
            System.out.println("Error getAllData JadwalTayang: " + e.getMessage());
        } finally {
            closeConnection(connect);
        }
        return list;
    }

    @Override
    public JadwalTayang getById(int id) {
        DBConnect connect = new DBConnect();
        JadwalTayang jadwalTayang = null;
        try {
            String sql = "SELECT * FROM dbo.udf_GetDetailJadwalTayangByID(?)";
            connect.pstat = connect.conn.prepareStatement(sql);
            connect.pstat.setInt(1, id);
            connect.result = connect.pstat.executeQuery();

            if (connect.result.next()) {
                jadwalTayang = mapResultSetToJadwalTayang(connect.result);
            }
        } catch (SQLException e) {
            System.out.println("Error getById JadwalTayang: " + e.getMessage());
        } finally {
            closeConnection(connect);
        }
        return jadwalTayang;
    }

    @Override
    public int getLastId() {
        DBConnect connect = new DBConnect();
        int id = 0;
        try {
            String sql = "SELECT ISNULL(MAX(jty_id), 0) AS id FROM JadwalTayang";
            connect.pstat = connect.conn.prepareStatement(sql);
            connect.result = connect.pstat.executeQuery();

            if (connect.result.next()) {
                id = connect.result.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Error getLastId: " + e.getMessage());
        } finally {
            closeConnection(connect);
        }
        return id;
    }


    @Override
    public OperationResult insertData(JadwalTayang jadwalTayang) {
        DBConnect connect = new DBConnect();
        try {
            if (isJadwalBentrok(jadwalTayang.getJamMulai().toString(),
                    jadwalTayang.getJamSelesai().toString(),
                    jadwalTayang.getNamaStudio().getId())) {
                return OperationResult.failure("Jadwal bentrok dengan jadwal lain di studio yang sama.");
            }

            String sql = "{CALL sp_InsertJadwalTayang(?, ?, ?, ?, ?, ?, ?)}";
            connect.pstat = connect.conn.prepareCall(sql);
            connect.pstat.setInt(1, jadwalTayang.getNamaFilm().getId());
            connect.pstat.setInt(2, jadwalTayang.getNamaStudio().getId());
            connect.pstat.setTime(3, jadwalTayang.getJamMulai());
            connect.pstat.setTime(4, jadwalTayang.getJamSelesai());
            connect.pstat.setString(5, jadwalTayang.getJenisHari());
            connect.pstat.setBigDecimal(6, jadwalTayang.getHarga());
            connect.pstat.setString(7, jadwalTayang.getCreatedBy());


            connect.pstat.execute();

            return OperationResult.success("Jadwal tayang berhasil ditambahkan.");

        } catch (SQLException e) {
            return OperationResult.failure("Gagal tambah jadwal tayang: " + e.getMessage());
        } finally {
            closeConnection(connect);
        }
    }

    @Override
    public OperationResult updateData(JadwalTayang jadwalTayang) {
        DBConnect connect = new DBConnect();
        try {
            String sql = "{CALL sp_UpdateJadwalTayang(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
            connect.pstat = connect.conn.prepareCall(sql);
            connect.pstat.setInt(1, jadwalTayang.getId());
            connect.pstat.setInt(2, jadwalTayang.getNamaFilm().getId());
            connect.pstat.setInt(3, jadwalTayang.getNamaStudio().getId());
            connect.pstat.setTime(4, jadwalTayang.getJamMulai());
            connect.pstat.setTime(5, jadwalTayang.getJamSelesai());
            connect.pstat.setString(6, jadwalTayang.getJenisHari());
            connect.pstat.setBigDecimal(7, jadwalTayang.getHarga());
            connect.pstat.setInt(8, jadwalTayang.getStatus());
            connect.pstat.setString(9, jadwalTayang.getModifiedBy());

            connect.pstat.execute();
            return OperationResult.success("Jadwal tayang berhasil diperbarui.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal update jadwal tayang: " + e.getMessage());
        } finally {
            closeConnection(connect);
        }
    }

    @Override
    public OperationResult deleteData(int id, String modifiedBy) {
        DBConnect connect = new DBConnect();
        try {
            String sql = "{CALL sp_DeleteJadwalTayang(?, ?)}";
            connect.pstat = connect.conn.prepareCall(sql);
            connect.pstat.setInt(1, id);
            connect.pstat.setString(2, modifiedBy);
            connect.pstat.execute();

            return OperationResult.success("Jadwal tayang berhasil dihapus.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal hapus jadwal tayang: " + e.getMessage());
        } finally {
            closeConnection(connect);
        }
    }

    @Override
    public OperationResult toogleStatus(int id) {
        DBConnect connect = new DBConnect();
        try {
            String sql = "{CALL sp_ToggleStatusJadwalTayang(?, ?)}";
            connect.pstat = connect.conn.prepareCall(sql);
            connect.pstat.setInt(1, id);
            connect.pstat.setString(2, Role.getNama());
            connect.pstat.execute();

            return OperationResult.success("Status jadwal tayang berhasil diubah.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal toggle status jadwal tayang: " + e.getMessage());
        } finally {
            closeConnection(connect);
        }
    }

    @Override
    public boolean isJadwalBentrok(String jamMulai, String jamSelesai, int idStudio) {
        DBConnect connect = new DBConnect();
        try {
            String sql = "SELECT COUNT(*) as total FROM JadwalTayang " +
                    "WHERE std_id = ? AND jty_status = 1 " +
                    "AND ((jty_jam_mulai < ? AND jty_jam_selesai > ?) " +
                    "OR (jty_jam_mulai < ? AND jty_jam_selesai > ?) " +
                    "OR (jty_jam_mulai >= ? AND jty_jam_selesai <= ?))";

            connect.pstat = connect.conn.prepareStatement(sql);
            connect.pstat.setInt(1, idStudio);
            connect.pstat.setString(2, jamSelesai);
            connect.pstat.setString(3, jamMulai);
            connect.pstat.setString(4, jamMulai);
            connect.pstat.setString(5, jamSelesai);
            connect.pstat.setString(6, jamMulai);
            connect.pstat.setString(7, jamSelesai);

            connect.result = connect.pstat.executeQuery();
            if (connect.result.next()) {
                return connect.result.getInt("total") > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking jadwal bentrok: " + e.getMessage());
        } finally {
            closeConnection(connect);
        }
        return false;
    }

    private void closeConnection(DBConnect connect) {
        try {
            if (connect.result != null) connect.result.close();
            if (connect.pstat != null) connect.pstat.close();
            if (connect.conn != null) connect.conn.close();
        } catch (SQLException e) {
            System.out.println("Gagal menutup koneksi JadwalTayangImpl: " + e.getMessage());
        }
    }
}
