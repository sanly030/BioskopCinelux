package cinelux.bioskopcinelux.service.impl;

import cinelux.bioskopcinelux.connection.DBConnect;
import cinelux.bioskopcinelux.controller.login.LoginCtrl;
import cinelux.bioskopcinelux.model.*;
import cinelux.bioskopcinelux.service.TransaksiPenjualanTiketSrvc;
import cinelux.bioskopcinelux.util.Session;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PenjualanTiketImpl implements TransaksiPenjualanTiketSrvc {

    private final Connection connection;

    public PenjualanTiketImpl() {
        this.connection = DBConnect.getConnection();
    }

    @Override
    public TransaksiPenjualanTiket resultTransaksi(ResultSet rs) throws SQLException {
        int id = rs.getInt("tpt_id");
        int pgwId = rs.getInt("pgw_id");
        int tstId = rs.getInt("tst_id");
        int jtyId = rs.getInt("jty_id");
        Integer prmId = rs.getObject("prm_id") != null ? rs.getInt("prm_id") : null;

        LocalDateTime tanggal = rs.getTimestamp("tpt_tanggal").toLocalDateTime();
        double hargaTiket = rs.getDouble("jty_harga_tiket");
        int jumlahTiket = rs.getInt("tpt_jumlah_tiket");
        double totalHarga = rs.getDouble("tpt_total_harga");
        String statusKursi = rs.getString("tpt_status_kursi");
        String createdBy = rs.getString("tpt_created_by");
        LocalDateTime createdDate = rs.getTimestamp("tpt_created_date").toLocalDateTime();

        Karyawan pegawai = new Karyawan();
        pegawai.setId(pgwId);

        Setting metodePembayaran = new Setting();
        metodePembayaran.setId(tstId);

        JadwalTayang jadwal = new JadwalTayang();
        jadwal.setId(jtyId);
        jadwal.setHarga(BigDecimal.valueOf(hargaTiket));

        Promo promo = null;
        if (prmId != null) {
            promo = new Promo();
            promo.setId(prmId);
        }

        return new TransaksiPenjualanTiket(id, pegawai, metodePembayaran, promo, jadwal, tanggal,
                hargaTiket, jumlahTiket, totalHarga, statusKursi, createdBy, createdDate);
    }

    @Override
    public List<TransaksiPenjualanTiket> getAllData() {
        return getAllData(null, null, null, null, null);
    }

    @Override
    public List<TransaksiPenjualanTiket> getAllData(String search, String status, Integer metodePembayaranId,
                                                    String sortColumn, String sortOrder) {
        List<TransaksiPenjualanTiket> list = new ArrayList<>();

        StringBuilder query = new StringBuilder("""
            SELECT tpt.*, p.pgw_nama, s.tst_nama, j.jty_jenis_hari
            FROM TransaksiPembayaranTiket tpt
            LEFT JOIN Pegawai p ON tpt.pgw_id = p.pgw_id
            LEFT JOIN dtSetting s ON tpt.tst_id = s.tst_id
            LEFT JOIN JadwalTayang j ON tpt.jty_id = j.jty_id
            WHERE 1=1
        """);

        List<Object> parameters = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            query.append("AND (p.pgw_nama LIKE ? OR j.jty_jenis_hari LIKE ?) ");
            parameters.add("%" + search + "%");
            parameters.add("%" + search + "%");
        }

        if (metodePembayaranId != null) {
            query.append("AND tpt.tst_id = ? ");
            parameters.add(metodePembayaranId);
        }

        if (sortColumn != null && !sortColumn.trim().isEmpty()) {
            String order = (sortOrder != null && sortOrder.equalsIgnoreCase("DESC")) ? "DESC" : "ASC";
            query.append("ORDER BY ").append(sortColumn).append(" ").append(order);
        } else {
            query.append("ORDER BY tpt.tpt_created_date DESC");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(resultTransaksi(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
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

    @Override
    public boolean saveData(TransaksiPenjualanTiket transaksi) {
        if (transaksi == null) {
            throw new IllegalArgumentException("Transaksi tidak boleh null");
        }

        // ✅ Jika pegawai belum diset, ambil dari session login
        if (transaksi.getPegawai() == null || transaksi.getPegawai().getId() <= 0) {
//            Karyawan loggedUser = LoginCtrl.UserSession.getUser();
            Karyawan loggedUser = Session.getLoggedUser();
            if (loggedUser == null || loggedUser.getId() <= 0) {
                throw new IllegalArgumentException("Pegawai tidak boleh null atau id <= 0");
            }
            transaksi.setPegawai(loggedUser); // set otomatis dari session
        }

        if (transaksi.getMetodePembayaran() == null || transaksi.getMetodePembayaran().getId() <= 0) {
            throw new IllegalArgumentException("Metode Pembayaran tidak valid");
        }

        if (transaksi.getJadwal() == null || transaksi.getJadwal().getId() <= 0) {
            throw new IllegalArgumentException("Jadwal tayang tidak valid");
        }

        try (Connection conn = DBConnect.getConnection()) {
            conn.setAutoCommit(false);

            String insertTransaksiQuery = """
                INSERT INTO TransaksiPembayaranTiket 
                (pgw_id, tst_id, jty_id, prm_id, tpt_tanggal, jty_harga_tiket, 
                 tpt_jumlah_tiket, tpt_total_harga, tpt_status_kursi, 
                 tpt_created_by, tpt_created_date)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(insertTransaksiQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, transaksi.getPegawai().getId());
                stmt.setInt(2, transaksi.getMetodePembayaran().getId());
                stmt.setInt(3, transaksi.getJadwal().getId());

                if (transaksi.getPromo() != null && transaksi.getPromo().getId() != null) {
                    stmt.setInt(4, transaksi.getPromo().getId());
                } else {
                    stmt.setNull(4, Types.INTEGER);
                }

                stmt.setTimestamp(5, Timestamp.valueOf(transaksi.getTglTransaksi()));
                stmt.setDouble(6, transaksi.getHargaTiket());
                stmt.setInt(7, transaksi.getJumlahTiket());
                stmt.setDouble(8, transaksi.getTotalHarga());
                stmt.setString(9, transaksi.getStatusKursi());
                stmt.setString(10, transaksi.getCreatedBy());
                stmt.setTimestamp(11, Timestamp.valueOf(transaksi.getCreatedDate()));

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    conn.rollback();
                    throw new SQLException("Gagal menyimpan transaksi, tidak ada baris yang terpengaruh.");
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int tptId = generatedKeys.getInt(1);
                        transaksi.setId(tptId);

                        // Insert detail tiket
                        if (transaksi.getDetailPenjualanTiket() != null && !transaksi.getDetailPenjualanTiket().isEmpty()) {
                            String insertDetail = """
                                INSERT INTO DetailPembelianTiket (tpt_id, krs_id, jumlah, sub_total)
                                VALUES (?, ?, ?, ?)
                            """;

                            try (PreparedStatement detailStmt = conn.prepareStatement(insertDetail)) {
                                for (DetailPenjualanTiket detail : transaksi.getDetailPenjualanTiket()) {
                                    if (detail.getKursi() == null || detail.getKursi().getId() == null) {
                                        throw new IllegalArgumentException("Detail tiket memiliki kursi yang null");
                                    }

                                    detailStmt.setInt(1, tptId);
                                    detailStmt.setInt(2, detail.getKursi().getId());
                                    detailStmt.setInt(3, detail.getJumlah());
                                    detailStmt.setDouble(4, detail.getSubTotal());
                                    detailStmt.addBatch();
                                }
                                detailStmt.executeBatch();
                            }
                        }

                        conn.commit();
                        return true;
                    } else {
                        conn.rollback();
                        throw new SQLException("Gagal mendapatkan ID dari transaksi yang disimpan.");
                    }
                }

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Error saat menyimpan transaksi: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (SQLException e) {
            System.err.println("Koneksi / SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }


    public List<Integer> getKursiYangSudahDipesan(int jadwalId) {
        List<Integer> kursiTerpakai = new ArrayList<>();

        String sql = """
        SELECT dpt.krs_id
        FROM DetailPembelianTiket dpt
        JOIN TransaksiPembayaranTiket tpt ON dpt.tpt_id = tpt.tpt_id
        WHERE tpt.jty_id = ?
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, jadwalId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    kursiTerpakai.add(rs.getInt("krs_id"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return kursiTerpakai;
    }


    public boolean isKursiSudahDipesan(int jadwalId, int kursiId) {
        String query = """
        SELECT COUNT(*) FROM DetailPembelianTiket d
        JOIN TransaksiPembayaranTiket t ON d.tpt_id = t.tpt_id
        WHERE t.jty_id = ? AND d.krs_id = ?
    """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, jadwalId);
            stmt.setInt(2, kursiId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
