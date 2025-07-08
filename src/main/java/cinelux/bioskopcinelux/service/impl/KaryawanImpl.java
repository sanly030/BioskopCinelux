package cinelux.bioskopcinelux.service.impl;

import cinelux.bioskopcinelux.model.Karyawan;
import cinelux.bioskopcinelux.model.Setting;
import cinelux.bioskopcinelux.service.KaryawanSrvc;
import cinelux.bioskopcinelux.connection.DBConnect;
import cinelux.bioskopcinelux.util.OperationResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KaryawanImpl implements KaryawanSrvc {
    DBConnect connect = new DBConnect();
    SettingImpl settingImpl = new SettingImpl();

    @Override
    public Karyawan mapResultSetToDetailKaryawan(ResultSet rs) throws SQLException {
        // ✅ Mapping Setting (Role)
        Setting role = new Setting(
                rs.getInt("role_id"),
                rs.getString("role_nama"),
                rs.getString("role_value"),
                rs.getString("role_kategori"),
                rs.getInt("role_status"),
                rs.getString("role_created_by"),
                rs.getString("role_modified_by")
                // Ambil alias role_modified_by
        );

        // ✅ Mapping Pegawai
        return new Karyawan(
                rs.getInt("pgw_id"),
                role,                               // Relasi ke Setting
                rs.getString("pgw_nama"),
                rs.getString("pgw_username"),
                rs.getString("pgw_password"),
                rs.getString("pgw_no_telp"),
                rs.getString("pgw_alamat"),
                rs.getInt("pgw_status"),
                rs.getString("pgw_created_by"),
                rs.getString("pgw_modif_by")
        );
    }


    @Override
    public List<Karyawan> getAllData() {
        return getAllData(null, null, null, null, null);
    }

    @Override
    public List<Karyawan> getAllData(String search, String role, Integer status, String urutan, String sortBy) {
        List<Karyawan> karyawanList = new ArrayList<>();
        DBConnect db = new DBConnect();

        try {
            String sql = "{CALL sp_GetListPegawai(?, ?, ?, ?, ?)}"; // Stored Procedure
            db.pstat = db.conn.prepareCall(sql);

            // Parameter 1: search
            if (search == null || search.trim().isEmpty()) {
                db.pstat.setNull(1, java.sql.Types.VARCHAR);
            } else {
                db.pstat.setString(1, search.trim());
            }

            // Parameter 2: role
            if (role == null || role.trim().isEmpty()) {
                db.pstat.setNull(2, java.sql.Types.VARCHAR);
            } else {
                db.pstat.setString(2, role.trim());
            }

            // Parameter 3: status
            if (status == null) {
                db.pstat.setNull(3, java.sql.Types.INTEGER);
            } else {
                db.pstat.setInt(3, status);
            }

            // Parameter 4: urutan
            if (urutan == null || urutan.trim().isEmpty()) {
                db.pstat.setNull(4, java.sql.Types.VARCHAR);
            } else {
                db.pstat.setString(4, urutan.trim());
            }

            // Parameter 5: sortBy
            if (sortBy == null || sortBy.trim().isEmpty()) {
                db.pstat.setNull(5, java.sql.Types.VARCHAR);
            } else {
                db.pstat.setString(5, sortBy.trim());
            }

            db.result = db.pstat.executeQuery();
            while (db.result.next()) {
                karyawanList.add(mapResultSetToDetailKaryawan(db.result));
            }

        } catch (SQLException e) {
            System.out.println("Error saat ambil data karyawan: " + e.getMessage());
        } finally {
            try {
                if (db.result != null) db.result.close();
                if (db.pstat != null) db.pstat.close();
                if (db.conn != null) db.conn.close();
            } catch (SQLException e) {
                System.out.println("Error saat tutup koneksi: " + e.getMessage());
            }
        }

        return karyawanList;
    }

    @Override
    public Karyawan getById(int id) {
        Karyawan karyawan = null;
        ResultSet rs = null;

        try {
            String sql = "{CALL dbo.sp_GetDetailKaryawanByID(?)}";
            connect.pstat = connect.conn.prepareStatement(sql);
            connect.pstat.setInt(1, id);
            connect.pstat.execute();
            rs = connect.pstat.getResultSet();

            if (rs.next()) {
                karyawan = this.mapResultSetToDetailKaryawan(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error getById Karyawan: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources in getById: " + e.getMessage());
            }
        }

        return karyawan;
    }

    @Override
    public int getLastId() {
        int id = 0;
        ResultSet rs = null;

        try {
            String sql = "SELECT MAX(pgw_id) AS id FROM Pegawai";
            connect.pstat = connect.conn.prepareStatement(sql);
            rs = connect.pstat.executeQuery();

            if (rs.next()) {
                id = rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Error getLastId Karyawan: " + e.getMessage());
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
    public OperationResult insertData(Karyawan karyawan) {
        try {
            String sql = "{CALL sp_InsertPegawai(?, ?, ?, ?, ?, ?, ?)}";
            connect.pstat = connect.conn.prepareCall(sql);
            connect.pstat.setInt(1, karyawan.getRole().getId());
            connect.pstat.setString(2, karyawan.getNama());
            connect.pstat.setString(3, karyawan.getUsername());
            connect.pstat.setString(4, karyawan.getPassword());
            connect.pstat.setString(5, karyawan.getNoTelp());
            connect.pstat.setString(6, karyawan.getAlamat());
            connect.pstat.setString(7, karyawan.getCreatedBy());

            connect.pstat.execute();
            return OperationResult.success("Data Karyawan berhasil ditambahkan.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal menambahkan Data Karyawan: \n" + e.getMessage());
        } finally {
            try {
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public OperationResult deleteData(int id, String modifiedBy) {
        try {
            String sql = "{CALL sp_DeletePegawai(?, ?)}";
            connect.pstat = connect.conn.prepareCall(sql);

            connect.pstat.setInt(1, id);
            connect.pstat.setString(2, modifiedBy); // kirim pgw_modif_by

            connect.pstat.execute();
            return OperationResult.success("Status Karyawan berhasil diubah.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal menghapus Data Karyawan:\n" + e.getMessage());
        } finally {
            try {
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public OperationResult updateData(Karyawan karyawan) {
        try {
            String sql = "{CALL sp_UpdatePegawai(?, ?, ?, ?, ?, ?, ?)}";
            connect.pstat = connect.conn.prepareCall(sql);

            connect.pstat.setInt(1, karyawan.getId());
//            connect.pstat.setInt(2, karyawan.getRole().getId());
            connect.pstat.setString(2, karyawan.getNama());
            connect.pstat.setString(3, karyawan.getUsername());
            connect.pstat.setString(4, karyawan.getPassword());
            connect.pstat.setString(5, karyawan.getNoTelp());
            connect.pstat.setString(6, karyawan.getAlamat());
            connect.pstat.setString(7, karyawan.getModifiedBy());

            connect.pstat.execute();
            return OperationResult.success("Data Karyawan berhasil diperbarui.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal memperbarui Data Karyawan:\n" + e.getMessage());
        } finally {
            try {
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

//    @Override
//    public OperationResult toggleStatus(int id) {
//        try {
//            String sql = "{CALL sp_ToggleKaryawanStatus(?)}";
//            connect.pstat = connect.conn.prepareCall(sql);
//            connect.pstat.setInt(1, id);
//
//            connect.pstat.execute();
//            return OperationResult.success("Status Karyawan berhasil diubah.");
//        } catch (SQLException e) {
//            return OperationResult.failure("Gagal mengubah status Karyawan:\n" + e.getMessage());
//        } finally {
//            try {
//                if (connect.pstat != null) connect.pstat.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
