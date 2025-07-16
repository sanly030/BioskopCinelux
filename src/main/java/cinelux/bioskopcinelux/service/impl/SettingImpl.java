package cinelux.bioskopcinelux.service.impl;

import cinelux.bioskopcinelux.model.Setting;
import cinelux.bioskopcinelux.service.SettingSrvc;
import cinelux.bioskopcinelux.util.OperationResult;
import cinelux.bioskopcinelux.connection.DBConnect;
import cinelux.bioskopcinelux.model.Role;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SettingImpl implements SettingSrvc {

    static DBConnect connect = new DBConnect();

    @Override
    public Setting mapResultSetToDetailSetting(ResultSet rs) throws SQLException {
        return new Setting(
                rs.getInt("tst_id"),
                rs.getString("tst_nama"),
                rs.getString("tst_value"),
                rs.getString("tst_kategori"),
                rs.getInt("tst_status"),
                rs.getString("tst_created_by"),
                rs.getString("tst_modif_by")
        );
    }

//    @Override
//    public List<Setting> getAllData() {
//        return getAllData(null, null, null, null, null);
//    }

    public List<Setting> getAllData() {
        return getAllData(null, null, null);
    }

    @Override
//    public List<Setting> getAllData(String search, String kategori, Integer status, String urutan, String sortBy) {
    public List<Setting> getAllData(String search, String kategori, Integer status) {
        List<Setting> settings = new ArrayList<>();
        DBConnect db = new DBConnect();

        try {
//            String sql = "{CALL sp_GetListSetting(?, ?, ?, ?, ?)}"; // Panggil SP dengan 5 parameter
            String sql = "{CALL sp_GetListSetting(?, ?, ?)}";
            db.pstat = db.conn.prepareCall(sql);

            // Parameter 1: search (nullable)
            if (search == null || search.trim().isEmpty()) {
                db.pstat.setNull(1, java.sql.Types.VARCHAR);
            } else {
                db.pstat.setString(1, search.trim());
            }

            // Parameter 2: kategori (nullable)
            if (kategori == null || kategori.trim().isEmpty()) {
                db.pstat.setNull(2, java.sql.Types.VARCHAR);
            } else {
                db.pstat.setString(2, kategori.trim());
            }

            // Parameter 3: status (nullable)
            if (status == null) {
                db.pstat.setNull(3, java.sql.Types.INTEGER);
            } else {
                db.pstat.setInt(3, status);
            }

//            // Parameter 4: urutan (nullable)
//            if (urutan == null || urutan.trim().isEmpty()) {
//                db.pstat.setNull(4, java.sql.Types.VARCHAR);
//            } else {
//                db.pstat.setString(4, urutan.trim());
//            }

//            // Parameter 5: sortBy (nullable)
//            if (sortBy == null || sortBy.trim().isEmpty()) {
//                db.pstat.setNull(5, java.sql.Types.VARCHAR);
//            } else {
//                db.pstat.setString(5, sortBy.trim());
//            }

            db.result = db.pstat.executeQuery();
            while (db.result.next()) {
                settings.add(mapResultSetToDetailSetting(db.result));
            }

        } catch (SQLException e) {
            System.out.println("Error saat ambil data setting: " + e.getMessage());
        } finally {
            try {
                if (db.result != null) db.result.close();
                if (db.pstat != null) db.pstat.close();
                if (db.conn != null) db.conn.close();
            } catch (SQLException e) {
                System.out.println("Error saat tutup koneksi: " + e.getMessage());
            }
        }

        return settings;
    }


    @Override
    public int getLastId() {
        int id = 0;
        ResultSet rs = null;

        try {
            String sql = "SELECT MAX(tst_id) AS id FROM dtSetting";
            connect.pstat = connect.conn.prepareStatement(sql);
            rs = connect.pstat.executeQuery();

            if (rs.next()) {
                id = rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return id;
    }

    @Override
    public OperationResult insertData(Setting s) {
        try {
            String sql = "{CALL sp_InsertDtSetting(?, ?, ?, ?)}";
            connect.pstat = connect.conn.prepareCall(sql);
            connect.pstat.setString(1, s.getNama());
            connect.pstat.setString(2, s.getValue());
            connect.pstat.setString(3, s.getKategori());
            connect.pstat.setString(4, s.getCreated_by());

            connect.pstat.execute();
            return OperationResult.success("Data Setting berhasil ditambahkan.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal menambahkan Data Setting: \n" + e.getMessage());
        } finally {
            try {
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public OperationResult deleteData(int id) {
        try {
            String sql = "{CALL sp_DeleteDtSetting(?, ?)}";
            connect.pstat = connect.conn.prepareCall(sql);
            connect.pstat.setInt(1, id);
            connect.pstat.setString(2, Role.getNama());

            connect.pstat.execute();
            return OperationResult.success("Status setting berhasil diubah.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal mengubah status setting:\n" + e.getMessage());
        } finally {
            try {
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public OperationResult updateData(Setting s) {
        try {
            String sql = "{CALL sp_UpdateDtSetting(?, ?, ?, ?, ?)}";
            connect.pstat = connect.conn.prepareCall(sql);

            connect.pstat.setInt(1, s.getId());
            connect.pstat.setString(2, s.getNama());
            connect.pstat.setString(3, s.getValue());
            connect.pstat.setString(4, s.getKategori());
            connect.pstat.setString(5, s.getModified_by());

            connect.pstat.execute();
            return OperationResult.success("Data berhasil diperbarui.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal memperbarui data Setting:\n" + e.getMessage());
        } finally {
            try {
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Setting getById(int id) {
        Setting setting = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT * FROM dbo.udf_getDetailSettingByID(?)";
            connect.pstat = connect.conn.prepareStatement(sql);
            connect.pstat.setInt(1, id);
            connect.pstat.execute();
            rs = connect.pstat.getResultSet();

            if (rs.next()) {
                setting = this.mapResultSetToDetailSetting(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error getting detailSetting by id: \n" + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources in getById: " + e.getMessage());
            }
        }

        return setting;
    }

    @Override
    public OperationResult toogleStatus(int id) {
        try {
            String sql = "{CALL sp_toogleStatusSetting(?, ?)}";
            connect.pstat = connect.conn.prepareStatement(sql);
            connect.pstat.setInt(1, id);
            connect.pstat.setString(2, Role.getNama());
            connect.pstat.execute();

            return OperationResult.success("Status setting berhasil diubah.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal mengubah status setting:\n" + e.getMessage());
        } finally {
            try {
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                System.out.println("Kesalahan saat menutup statement: " + e.getMessage());
            }
        }
    }
}
