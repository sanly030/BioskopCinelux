package cinelux.bioskopcinelux.service.impl;

import cinelux.bioskopcinelux.connection.DBConnect;
import cinelux.bioskopcinelux.model.Studio;
import cinelux.bioskopcinelux.service.StudioSrvc;
import cinelux.bioskopcinelux.util.OperationResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudioImpl implements StudioSrvc {
    static DBConnect connect = new DBConnect();

    @Override
    public Studio mapResultSetToStudio(ResultSet rs) throws SQLException {
        return new Studio(
                rs.getInt("std_id"),
                rs.getString("std_nama"),
                rs.getInt("std_baris"),     // <- penting!
                rs.getInt("std_kolom"),
                rs.getInt("std_kapasitas"), // <- penting!
                rs.getInt("std_status"),
                rs.getString("std_created_by"),
                rs.getString("std_modif_by")
        );
    }

    @Override
    public List<Studio> getAllData() {
        return getAllData(null, null, null, null);
    }

    @Override
    public List<Studio> getAllData(String search, Integer status, String urutan, String sortBy) {
        List<Studio> studios = new ArrayList<>();
        DBConnect db = new DBConnect();

        try {
            String sql = "{CALL sp_GetListStudio(?, ?, ?, ?)}";
            db.pstat = db.conn.prepareCall(sql);
            db.pstat.setObject(1, search, Types.VARCHAR);
            db.pstat.setObject(2, status, Types.VARCHAR);
            db.pstat.setObject(3, urutan, Types.VARCHAR);
            db.pstat.setObject(4, sortBy, Types.VARCHAR);

            db.result = db.pstat.executeQuery();
            while (db.result.next()) {
                studios.add(mapResultSetToStudio(db.result));
            }
        } catch (SQLException e) {
            System.out.println("Error getAllData Studio: " + e.getMessage());
        } finally {
            try {
                if (db.result != null) db.result.close();
                if (db.pstat != null) db.pstat.close();
                if (db.conn != null) db.conn.close();
            } catch (SQLException e) {
                System.out.println("Gagal menutup koneksi: " + e.getMessage());
            }
        }

        return studios;
    }

    @Override
    public Studio getById(int id) {
        Studio studio = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT * FROM udf_getDetailStudioByID(?)";
            connect.pstat = connect.conn.prepareStatement(sql);
            connect.pstat.setInt(1, id);
            rs = connect.pstat.executeQuery();

            if (rs.next()) {
                studio = mapResultSetToStudio(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error getById Studio: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                System.out.println("Error closing getById resources: " + e.getMessage());
            }
        }

        return studio;
    }

    @Override
    public int getLastId() {
        int id = 0;
        ResultSet rs = null;

        try {
            String sql = "SELECT MAX(std_id) AS id FROM Studio";
            connect.pstat = connect.conn.prepareStatement(sql);
            rs = connect.pstat.executeQuery();

            if (rs.next()) {
                id = rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Error getLastId Studio: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                System.out.println("Error closing getLastId resources: " + e.getMessage());
            }
        }

        return id;
    }

    @Override
    public OperationResult InsertStudioWithKursi(Studio studio) {
        try {
            String sql = "{CALL sp_InsertStudioWithKursi(?, ?, ?, ?)}";
            connect.pstat = connect.conn.prepareCall(sql);
            connect.pstat.setString(1, studio.getNama());
            connect.pstat.setInt(2, studio.getBaris());
            connect.pstat.setInt(3, studio.getKolom());
            connect.pstat.setString(4, studio.getCreatedBy());

            connect.pstat.execute();
            return OperationResult.success("Studio berhasil ditambahkan dengan " +
                    (studio.getBaris() * studio.getKolom()) + " kursi.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal menambahkan studio: " + e.getMessage());
        } finally {
            try {
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                System.out.println("Error closing insertStudioWithKursi: " + e.getMessage());
            }
        }
    }

    @Override
    public OperationResult updateStudioWithKursi(Studio studio) {
        try {
            // HANYA 5 parameter: id, nama, baris, kolom, modifiedBy
            String sql = "{CALL sp_UpdateStudioWithKursi(?, ?, ?, ?, ?)}";
            connect.pstat = connect.conn.prepareCall(sql);
            connect.pstat.setInt(1, studio.getId());
            connect.pstat.setString(2, studio.getNama());
            connect.pstat.setInt(3, studio.getBaris());
            connect.pstat.setInt(4, studio.getKolom());
            connect.pstat.setString(5, studio.getModifiedBy());

            connect.pstat.execute();

            return OperationResult.success("Studio berhasil diperbarui dengan " +
                    (studio.getBaris() * studio.getKolom()) + " kursi.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal memperbarui studio: " + e.getMessage());
        } finally {
            try {
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                System.out.println("Error closing updateStudioWithKursi: " + e.getMessage());
            }
        }
    }

    @Override
    public OperationResult deleteData(int id, String modifiedBy) {
        try {
            String sql = "{CALL sp_DeleteStudioWithKursi(?, ?)}";
            connect.pstat = connect.conn.prepareCall(sql);
            connect.pstat.setInt(1, id);
            connect.pstat.setString(2, modifiedBy);
            connect.pstat.execute();
            return OperationResult.success("Studio berhasil dihapus.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal menghapus studio: " + e.getMessage());
        } finally {
            try {
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                System.out.println("Error closing deleteData: " + e.getMessage());
            }
        }
    }

    @Override
    public OperationResult toogleStatus(int id) {
        try {
            String sql = "{CALL sp_ToogleStatusStudio(?)}";
            connect.pstat = connect.conn.prepareCall(sql);
            connect.pstat.setInt(1, id);
            connect.pstat.execute();
            return OperationResult.success("Status studio berhasil diubah.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal mengubah status studio: " + e.getMessage());
        } finally {
            try {
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                System.out.println("Error closing toogleStatus: " + e.getMessage());
            }
        }
    }
}
