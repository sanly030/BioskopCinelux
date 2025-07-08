package cinelux.bioskopcinelux.service.impl;

import cinelux.bioskopcinelux.model.Menu;
import cinelux.bioskopcinelux.connection.DBConnect;
import cinelux.bioskopcinelux.model.Role;
import cinelux.bioskopcinelux.model.Setting;
import cinelux.bioskopcinelux.service.MenuSrvc;
import cinelux.bioskopcinelux.service.SettingSrvc;
import cinelux.bioskopcinelux.util.OperationResult;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MenuImpl implements MenuSrvc {

//    DBConnect connect = new DBConnect();
    private SettingSrvc settingService = new SettingImpl(); // untuk ambil data jenis makanan

    @Override
    public Menu mapResultSetToDetailSetting(ResultSet rs) throws SQLException {
        Setting jenisMakanan = new Setting(
                rs.getInt("tst_id"),
                rs.getString("tst_nama"),
                null,
                rs.getString("tst_kategori"),
                rs.getInt("tst_status"),
                null,
                null
        );

        return new Menu(
                rs.getInt("mnu_id"),
                jenisMakanan,
                rs.getString("mnu_nama"),
                rs.getInt("mnu_stok"),
                rs.getDouble("mnu_harga"),
                rs.getInt("mnu_status"),
                rs.getString("mnu_created_by"),
                rs.getString("mnu_modif_by")
        );
    }

    @Override
    public List<Menu> getAllData() {
        return getAllData(null, null, null, null, null);
    }

    @Override
    public List<Menu> getAllData(String search, String kategori, Integer status, String urutan, String sortBy) {
        DBConnect connect = new DBConnect();
        List<Menu> list = new ArrayList<>();
        try {
            String sql = "{CALL sp_GetListMenu(?, ?, ?, ?, ?)}";
            connect.pstat = connect.conn.prepareCall(sql);

            connect.pstat.setObject(1, search, java.sql.Types.VARCHAR);
            connect.pstat.setObject(2, kategori, java.sql.Types.VARCHAR);
            connect.pstat.setObject(3, status, java.sql.Types.INTEGER);
            connect.pstat.setObject(4, urutan, java.sql.Types.VARCHAR);
            connect.pstat.setObject(5, sortBy, java.sql.Types.VARCHAR);

            connect.result = connect.pstat.executeQuery();
            while (connect.result.next()) {
                list.add(mapResultSetToDetailSetting(connect.result));
            }
        } catch (SQLException e) {
            System.out.println("Error getAllData Menu: " + e.getMessage());
        } finally {
            closeConnection(connect);
        }
        return list;
    }

    @Override
    public Menu getById(int id) {
        DBConnect connect = new DBConnect();
        Menu menu = null;
        try {
            String sql = "SELECT * FROM dbo.udf_GetDetailMenuByID(?)";
            connect.pstat = connect.conn.prepareStatement(sql);
            connect.pstat.setInt(1, id);
            connect.result = connect.pstat.executeQuery();

            if (connect.result.next()) {
                menu = mapResultSetToDetailSetting(connect.result);
            }
        } catch (SQLException e) {
            System.out.println("Error getById Menu: " + e.getMessage());
        } finally {
            closeConnection(connect);
        }
        return menu;
    }

    @Override
    public int getLastId() {
        DBConnect connect = new DBConnect();
        int id = 0;
        try {
            String sql = "SELECT MAX(mnu_id) AS id FROM Menu";
            connect.pstat = connect.conn.prepareStatement(sql);
            connect.result = connect.pstat.executeQuery();

            if (connect.result.next()) {
                id = connect.result.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection(connect);
        }
        return id;
    }

    @Override
    public OperationResult insertData(Menu m) {
        DBConnect connect = new DBConnect();
        try {
            String sql = "{CALL sp_InsertMenu(?, ?, ?, ?, ?, ?)}";
            connect.pstat = connect.conn.prepareCall(sql);
            connect.pstat.setString(1, null); // atau bisa pakai gambar jika ada, sementara null
            connect.pstat.setInt(2, m.getJenis_makanan().getId());
            connect.pstat.setString(3, m.getNama());
            connect.pstat.setInt(4, m.getStok());
            connect.pstat.setDouble(5, m.getHarga());
            connect.pstat.setString(6,
                    (m.getCreatedBy() != null && !m.getCreatedBy().isEmpty())
                            ? m.getCreatedBy()
                            : "Admin");



            connect.pstat.execute();
            return OperationResult.success("Menu berhasil ditambahkan.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal tambah menu: " + e.getMessage());
        } finally {
            closeConnection(connect); // tambahkan parameter
        }
    }


    @Override
    public OperationResult updateData(Menu m) {
        DBConnect connect = new DBConnect();
        try {
            String sql = "{CALL sp_UpdateMenu(?, ?, ?, ?, ?, ?)}";
            connect.pstat = connect.conn.prepareCall(sql);
            connect.pstat.setInt(1, m.getId());
            connect.pstat.setInt(2, m.getJenis_makanan().getId());
            connect.pstat.setString(3, m.getNama());
            connect.pstat.setInt(4, m.getStok());
            connect.pstat.setDouble(5, m.getHarga());
            connect.pstat.setString(6, m.getModifiedBy());

            connect.pstat.execute();
            return OperationResult.success("Menu berhasil diperbarui.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal update menu: " + e.getMessage());
        } finally {
            closeConnection(connect);
        }
    }

    @Override
    public OperationResult deleteData(int id, String modifiedBy) {
        DBConnect connect = new DBConnect();
        try {
            String sql = "{CALL sp_DeleteMenu(?, ?)}";
            connect.pstat = connect.conn.prepareCall(sql);
            connect.pstat.setInt(1, id);
            connect.pstat.setString(2, Role.getNama()); // nama pengguna login

            connect.pstat.execute();
            return OperationResult.success("Status menu berhasil diubah.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal hapus menu: " + e.getMessage());
        } finally {
            closeConnection(connect);
        }
    }

    @Override
    public OperationResult toogleStatus(int id) {
        DBConnect connect = new DBConnect();
        try {
            String sql = "{CALL sp_ToggleStatusMenu(?, ?)}";
            connect.pstat = connect.conn.prepareCall(sql);
            connect.pstat.setInt(1, id);
            connect.pstat.setString(2, Role.getNama());

            connect.pstat.execute();
            return OperationResult.success("Status menu berhasil diubah.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal toggle status menu: " + e.getMessage());
        } finally {
            closeConnection(connect);
        }
    }

    private void closeConnection(DBConnect connect) {
        try {
            if (connect.result != null) connect.result.close();
            if (connect.pstat != null) connect.pstat.close();
            if (connect.conn != null) connect.conn.close();
        } catch (SQLException e) {
            System.out.println("Gagal menutup koneksi MenuImpl: " + e.getMessage());
        }
    }


}

