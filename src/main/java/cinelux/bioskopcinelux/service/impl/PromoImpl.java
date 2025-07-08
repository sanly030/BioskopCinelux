package cinelux.bioskopcinelux.service.impl;

import cinelux.bioskopcinelux.connection.DBConnect;
import cinelux.bioskopcinelux.model.Promo;
import cinelux.bioskopcinelux.service.PromoSrvc;
import cinelux.bioskopcinelux.util.OperationResult;
import cinelux.bioskopcinelux.model.Role;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PromoImpl implements PromoSrvc {
    static DBConnect connect = new DBConnect();

    @Override
    public Promo mapResultSetToPromo(ResultSet rs) throws SQLException {
        return new Promo(
                rs.getInt("prm_id"),
                rs.getString("prm_nama"),
                rs.getString("prm_tipe_promo"),
                rs.getDouble("prm_diskon"),
                rs.getDate("prm_tgl_mulai"),
                rs.getDate("prm_tgl_selesai"),
                rs.getInt("prm_status"),
                rs.getString("prm_created_by"),
                rs.getString("prm_created_date"),
                rs.getString("prm_modif_by"),
                rs.getString("prm_modif_date")
        );
    }

    @Override
    public List<Promo> getAllData() {
        return getAllData(null, null, null, null, null);
    }

    @Override
    public List<Promo> getAllData(String search, String tipePromo, Integer status, String urutan, String sortBy) {
        List<Promo> promos = new ArrayList<>();
        DBConnect db = new DBConnect();

        try {
            String sql = "{CALL sp_GetListPromo(?, ?, ?, ?, ?)}";
            db.pstat = db.conn.prepareCall(sql);

            db.pstat.setObject(1, search, java.sql.Types.VARCHAR);
            db.pstat.setObject(2, tipePromo, java.sql.Types.VARCHAR);
            db.pstat.setObject(3, status, java.sql.Types.INTEGER);
            db.pstat.setObject(4, urutan, java.sql.Types.VARCHAR);
            db.pstat.setObject(5, sortBy, java.sql.Types.VARCHAR);

            db.result = db.pstat.executeQuery();
            while (db.result.next()) {
                promos.add(mapResultSetToPromo(db.result));
            }
        } catch (SQLException e) {
            System.out.println("Error saat ambil data promo: " + e.getMessage());
        } finally {
            try {
                if (db.result != null) db.result.close();
                if (db.pstat != null) db.pstat.close();
                if (db.conn != null) db.conn.close();
            } catch (SQLException e) {
                System.out.println("Gagal menutup koneksi: " + e.getMessage());
            }
        }

        return promos;
    }

    @Override
    public Promo getById(int id) {
        Promo promo = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT * FROM udf_getDetailPromoByID(?)";
            connect.pstat = connect.conn.prepareStatement(sql);
            connect.pstat.setInt(1, id);
            rs = connect.pstat.executeQuery();

            if (rs.next()) {
                promo = mapResultSetToPromo(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error getById Promo: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                System.out.println("Error closing getById resources: " + e.getMessage());
            }
        }

        return promo;
    }

    @Override
    public int getLastId() {
        int id = 0;
        ResultSet rs = null;
        try {
            String sql = "SELECT MAX(prm_id) AS id FROM Promo";
            connect.pstat = connect.conn.prepareStatement(sql);
            rs = connect.pstat.executeQuery();

            if (rs.next()) {
                id = rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Error getLastId Promo: " + e.getMessage());
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
    public OperationResult insertData(Promo p) {
        try {
            String sql = "{CALL sp_InsertPromo(?, ?, ?, ?, ?, ?,?)}";
            connect.pstat = connect.conn.prepareCall(sql);
            connect.pstat.setString(1, p.getNama_promo());
            connect.pstat.setString(2, p.getTipe_promo());
            connect.pstat.setDouble(3, p.getDiskon());
            connect.pstat.setDate(4, p.getTanggal_mulai());
            connect.pstat.setDate(5, p.getTanggal_selesai());
            connect.pstat.setInt(6, p.getStatus());
            connect.pstat.setString(7, p.getCreatedBy());

            connect.pstat.execute();
            return OperationResult.success("Data Promo berhasil ditambahkan.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal menambahkan data Promo:\n" + e.getMessage());
        } finally {
            try {
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                System.out.println("Error closing insertData: " + e.getMessage());
            }
        }
    }

    @Override
    public OperationResult deleteData(int id) {
        try {
            String sql = "{CALL sp_DeletePromo(?, ?)}";
            connect.pstat = connect.conn.prepareCall(sql);
            connect.pstat.setInt(1, id);
            connect.pstat.setString(2, Role.getNama());

            connect.pstat.execute();
            return OperationResult.success("Promo berhasil dihapus.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal menghapus promo:\n" + e.getMessage());
        } finally {
            try {
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                System.out.println("Error closing deleteData: " + e.getMessage());
            }
        }
    }

    @Override
    public OperationResult updateData(Promo p) {
        try {
            String sql = "{CALL sp_UpdatePromo(?, ?, ?, ?, ?, ?, ?,?)}";
            connect.pstat = connect.conn.prepareCall(sql);
            connect.pstat.setInt(1, p.getId());
            connect.pstat.setString(2, p.getNama_promo());
            connect.pstat.setString(3, p.getTipe_promo());
            connect.pstat.setDouble(4, p.getDiskon());
            connect.pstat.setDate(5, p.getTanggal_mulai());
            connect.pstat.setDate(  6, p.getTanggal_selesai());
            connect.pstat.setInt(7, p.getStatus());
            connect.pstat.setString(8, p.getModifiedBy());

            connect.pstat.execute();
            return OperationResult.success("Data Promo berhasil diperbarui.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal memperbarui data promo:\n" + e.getMessage());
        } finally {
            try {
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                System.out.println("Error closing updateData: " + e.getMessage());
            }
        }
    }

    @Override
    public OperationResult toogleStatus(int id) {
        try {
            String sql = "{CALL sp_ToogleStatusPromo(?, ?)}";
            connect.pstat = connect.conn.prepareCall(sql);
            connect.pstat.setInt(1, id);
            connect.pstat.setString(2, Role.getNama());

            connect.pstat.execute();
            return OperationResult.success("Status promo berhasil diubah.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal mengubah status promo:\n" + e.getMessage());
        } finally {
            try {
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                System.out.println("Error closing toogleStatus: " + e.getMessage());
            }
        }
    }
}

