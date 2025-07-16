package cinelux.bioskopcinelux.service.impl;

import cinelux.bioskopcinelux.model.*;
import cinelux.bioskopcinelux.service.TransaksiPenjualanMenuSrvc;
import cinelux.bioskopcinelux.connection.DBConnect;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PenjualanMenuImpl implements TransaksiPenjualanMenuSrvc {

    private final Connection connection;

    public PenjualanMenuImpl() {
        this.connection = DBConnect.getConnection();
    }

    @Override
    public TransaksiPenjualanMenu resultTransaksi(ResultSet rs) throws SQLException {
        int id = rs.getInt("tpm_id");
        int pgwId = rs.getInt("pgw_id");
        int tstId = rs.getInt("tst_id");
        Integer prmId = rs.getObject("prm_id") != null ? rs.getInt("prm_id") : null;
        int jumlahMenu = rs.getInt("tpm_jumlah_menu");
        double totalHarga = rs.getDouble("tpm_total_harga");
        LocalDateTime tanggal = rs.getTimestamp("tpm_tanggal").toLocalDateTime();
        String createdBy = rs.getString("tpm_created_by");
        LocalDateTime createdDate = rs.getTimestamp("tpm_created_date").toLocalDateTime();

        Karyawan pegawai = new Karyawan();
        pegawai.setId(pgwId);

        Setting metodePembayaran = new Setting();
        metodePembayaran.setId(tstId);

        Promo promo = null;
        if (prmId != null) {
            promo = new Promo();
            promo.setId(prmId);
        }

        return new TransaksiPenjualanMenu(id, pegawai, metodePembayaran, promo,
                jumlahMenu, totalHarga, tanggal, createdBy, createdDate);
    }

    @Override
    public List<TransaksiPenjualanMenu> getAllData() {
        return getAllData(null, null, null, null, null);
    }

    @Override
    public List<TransaksiPenjualanMenu> getAllData(String search, String status, Integer metodePembayaranId,
                                                   String sortColumn, String sortOrder) {
        List<TransaksiPenjualanMenu> transaksiList = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query.append("""
                SELECT tpm.*, p.pgw_nama, s.tst_nama as metode_pembayaran, pr.prm_nama
                FROM TransaksiPenjualanMenu tpm
                LEFT JOIN Pegawai p ON tpm.pgw_id = p.pgw_id
                LEFT JOIN dtSetting s ON tpm.tst_id = s.tst_id
                LEFT JOIN Promo pr ON tpm.prm_id = pr.prm_id
                WHERE 1=1
            """);

        List<Object> parameters = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            query.append("AND (p.pgw_nama LIKE ? OR s.tst_nama LIKE ?) ");
            parameters.add("%" + search + "%");
            parameters.add("%" + search + "%");
        }

        if (metodePembayaranId != null) {
            query.append("AND tpm.tst_id = ? ");
            parameters.add(metodePembayaranId);
        }

        if (sortColumn != null && !sortColumn.trim().isEmpty()) {
            String order = (sortOrder != null && sortOrder.equalsIgnoreCase("DESC")) ? "DESC" : "ASC";
            query.append("ORDER BY ").append(sortColumn).append(" ").append(order);
        } else {
            query.append("ORDER BY tpm.tpm_created_date DESC");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transaksiList.add(resultTransaksi(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transaksiList;
    }

    @Override
    public boolean saveData(TransaksiPenjualanMenu transaksi) {
        try (Connection conn = DBConnect.getConnection()) {
            conn.setAutoCommit(false);

            String insertTransaksiQuery = """
                INSERT INTO TransaksiPenjualanMenu 
                (pgw_id, tst_id, prm_id, tpm_jumlah_menu, tpm_total_harga, tpm_tanggal, tpm_created_by, tpm_created_date)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement stmtTransaksi = conn.prepareStatement(insertTransaksiQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmtTransaksi.setInt(1, transaksi.getPegawai().getId());
                stmtTransaksi.setInt(2, transaksi.getMetodePembayaran().getId());

                if (transaksi.getPromo() != null) {
                    stmtTransaksi.setInt(3, transaksi.getPromo().getId());
                } else {
                    stmtTransaksi.setNull(3, Types.INTEGER);
                }

                stmtTransaksi.setInt(4, transaksi.getJumlahMenu());
                stmtTransaksi.setDouble(5, transaksi.getTotalHarga());
                stmtTransaksi.setTimestamp(6, Timestamp.valueOf(transaksi.getTanggal()));
                stmtTransaksi.setString(7, transaksi.getCreatedBy());
                stmtTransaksi.setTimestamp(8, Timestamp.valueOf(transaksi.getCreatedDate()));

                int rowsAffected = stmtTransaksi.executeUpdate();
                if (rowsAffected > 0) {
                    try (ResultSet generatedKeys = stmtTransaksi.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int tpmId = generatedKeys.getInt(1);
                            transaksi.setId(tpmId);

                            if (transaksi.getDetailPenjualanMenu() != null) {
                                String insertDetailQuery = """
                                    INSERT INTO DetailPenjualanMenu (tpm_id, mnu_id, jumlah, sub_total)
                                    VALUES (?, ?, ?, ?)
                                """;

                                try (PreparedStatement stmtDetail = conn.prepareStatement(insertDetailQuery)) {
                                    for (DetailPenjualanMenu detail : transaksi.getDetailPenjualanMenu()) {
                                        stmtDetail.setInt(1, tpmId);
                                        stmtDetail.setInt(2, detail.getMenu().getId());
                                        stmtDetail.setInt(3, detail.getJumlah());
                                        stmtDetail.setDouble(4, detail.getSubTotal());
                                        stmtDetail.addBatch();
                                    }
                                    stmtDetail.executeBatch();
                                }
                            }

                            conn.commit();
                            return true;
                        }
                    }
                }

                conn.rollback();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<Promo> getAllPromo() {
        List<Promo> promoList = new ArrayList<>();
        String query = "SELECT * FROM Promo WHERE prm_status = 1 AND prm_tgl_mulai <= GETDATE() AND prm_tgl_selesai >= GETDATE()";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Promo promo = new Promo();
                promo.setId(rs.getInt("prm_id"));
                promo.setNama_promo(rs.getString("prm_nama"));
                promo.setTipe_promo(rs.getString("prm_tipe_promo"));
                promo.setDiskon(rs.getDouble("prm_diskon"));

                // ✅ Perbaikan: gunakan getDate() sesuai class Promo
                promo.setTanggal_mulai(rs.getDate("prm_tgl_mulai"));
                promo.setTanggal_selesai(rs.getDate("prm_tgl_selesai"));

                promo.setStatus(rs.getInt("prm_status"));
                promoList.add(promo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return promoList;
    }


    public List<Setting> getMetodePembayaran() {
        List<Setting> list = new ArrayList<>();
        String query = "SELECT * FROM dtSetting WHERE tst_kategori = 'Metode Pembayaran' AND tst_status = 1";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Setting setting = new Setting();
                setting.setId(rs.getInt("tst_id"));
                setting.setNama(rs.getString("tst_nama"));
                setting.setValue(rs.getString("tst_value"));
                setting.setKategori(rs.getString("tst_kategori"));
                setting.setStatus(rs.getInt("tst_status"));
                list.add(setting);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Menu> getAllMenu() {
        List<Menu> list = new ArrayList<>();
        String query = "SELECT * FROM Menu WHERE mnu_status = 1 AND mnu_stok > 0";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Menu menu = new Menu();
                menu.setId(rs.getInt("mnu_id"));
                menu.setNama(rs.getString("mnu_nama"));
                menu.setStok(rs.getInt("mnu_stok"));
                menu.setHarga(rs.getDouble("mnu_harga"));
                menu.setGambar(rs.getString("mnu_gambar"));
                menu.setStatus(rs.getInt("mnu_status"));
                list.add(menu);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean updateStokMenu(int menuId, int jumlahTerjual) {
        String query = "UPDATE Menu SET mnu_stok = mnu_stok - ? WHERE mnu_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, jumlahTerjual);
            stmt.setInt(2, menuId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<DetailPenjualanMenu> getDetailPenjualanMenu(int tpmId) {
        List<DetailPenjualanMenu> list = new ArrayList<>();
        String query = """
            SELECT d.*, m.mnu_nama, m.mnu_harga
            FROM DetailPenjualanMenu d
            JOIN Menu m ON d.mnu_id = m.mnu_id
            WHERE d.tpm_id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, tpmId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DetailPenjualanMenu detail = new DetailPenjualanMenu();
                    TransaksiPenjualanMenu transaksi = new TransaksiPenjualanMenu();
                    transaksi.setId(tpmId);
                    detail.setTransaksi(transaksi);

                    Menu menu = new Menu();
                    menu.setId(rs.getInt("mnu_id"));
                    menu.setNama(rs.getString("mnu_nama"));
                    menu.setHarga(rs.getDouble("mnu_harga"));
                    detail.setMenu(menu);

                    detail.setJumlah(rs.getInt("jumlah"));
                    detail.setSubTotal(rs.getDouble("sub_total"));
                    list.add(detail);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}
