package cinelux.bioskopcinelux.service.impl;

import cinelux.bioskopcinelux.connection.DBConnect;
import cinelux.bioskopcinelux.model.Film;
import cinelux.bioskopcinelux.service.FilmSrvc;
import cinelux.bioskopcinelux.util.OperationResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FilmImpl implements FilmSrvc {
    static DBConnect connect = new DBConnect();

    @Override
    public Film mapResultSetToFilm(ResultSet rs) throws SQLException {
        return new Film(
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
    }

    @Override
    public List<Film> getAllData() {
        return getAllData(null, null, null, null, null);
    }

    @Override
    public List<Film> getAllData(String search, String genre, Integer status, String urutan, String sortBy) {
        List<Film> films = new ArrayList<>();
        DBConnect db = new DBConnect();

        try {
            String sql = "{CALL sp_GetListFilm(?, ?, ?, ?, ?)}";
            db.pstat = db.conn.prepareCall(sql);
            db.pstat.setObject(1, search, Types.VARCHAR);
            db.pstat.setObject(2, genre, Types.VARCHAR);
            db.pstat.setObject(3, status, Types.VARCHAR);
            db.pstat.setObject(4, urutan, Types.VARCHAR);
            db.pstat.setObject(5, sortBy, Types.VARCHAR);

            db.result = db.pstat.executeQuery();
            while (db.result.next()) {
                films.add(mapResultSetToFilm(db.result));
            }
        } catch (SQLException e) {
            System.out.println("Error getAllData Film: " + e.getMessage());
        } finally {
            try {
                if (db.result != null) db.result.close();
                if (db.pstat != null) db.pstat.close();
                if (db.conn != null) db.conn.close();
            } catch (SQLException e) {
                System.out.println("Gagal menutup koneksi: " + e.getMessage());
            }
        }

        return films;
    }

    @Override
    public Film getById(int id) {
        Film film = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT * FROM udf_getDetailFilmByID(?)";
            connect.pstat = connect.conn.prepareStatement(sql);
            connect.pstat.setInt(1, id);
            rs = connect.pstat.executeQuery();

            if (rs.next()) {
                film = mapResultSetToFilm(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error getById Film: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                System.out.println("Error closing getById resources: " + e.getMessage());
            }
        }

        return film;
    }

    @Override
    public int getLastId() {
        int id = 0;
        ResultSet rs = null;

        try {
            String sql = "SELECT MAX(flm_id) AS id FROM Film";
            connect.pstat = connect.conn.prepareStatement(sql);
            rs = connect.pstat.executeQuery();

            if (rs.next()) {
                id = rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Error getLastId Film: " + e.getMessage());
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
    public OperationResult insertData(Film film) {
        try {
            String sql = "{CALL sp_InsertFilm(?, ?, ?, ?, ?, ?, ?)}";
            connect.pstat = connect.conn.prepareCall(sql);
            connect.pstat.setString(1, film.getJudul());
            connect.pstat.setString(2, film.getGenre());
            connect.pstat.setInt(3, film.getDurasi());
            connect.pstat.setString(4, film.getRating_usia());
            connect.pstat.setString(5, film.getPoster());
            connect.pstat.setInt(6, film.getStatus());
            connect.pstat.setString(7, film.getCreatedBy());

            connect.pstat.execute();
            return OperationResult.success("Film berhasil ditambahkan.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal menambahkan film: " + e.getMessage());
        } finally {
            try {
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                System.out.println("Error closing insertData: " + e.getMessage());
            }
        }
    }

    @Override
    public OperationResult updateData(Film film) {
        try {
            String sql = "{CALL sp_UpdateFilm(?, ?, ?, ?, ?, ?, ?, ?)}"; // jumlah argumen disesuaikan
            connect.pstat = connect.conn.prepareCall(sql);

            connect.pstat.setInt(1, film.getId());
            connect.pstat.setString(2, film.getJudul());
            connect.pstat.setString(3, film.getGenre());
            connect.pstat.setInt(4, film.getDurasi());
            connect.pstat.setString(5, film.getRating_usia());
            connect.pstat.setString(6, film.getPoster()); // ✅ poster ditambahkan
            connect.pstat.setInt(7, film.getStatus());
            connect.pstat.setString(8, film.getModifiedBy());

            connect.pstat.execute();
            return OperationResult.success("Film berhasil diperbarui.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal memperbarui film: " + e.getMessage());
        } finally {
            try {
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                System.out.println("Error closing updateData: " + e.getMessage());
            }
        }
    }


    @Override
    public OperationResult deleteData(int id, String modifBy) {
        try {
            String sql = "{CALL sp_DeleteFilm(?, ?)}";
            connect.pstat = connect.conn.prepareCall(sql);
            connect.pstat.setInt(1, id);
            connect.pstat.setString(2, modifBy); // ✅ tambahkan user yg menghapus
            connect.pstat.execute();
            return OperationResult.success("Film berhasil dihapus (nonaktif).");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal menghapus film: " + e.getMessage());
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
            String sql = "{CALL sp_ToogleStatusFilm(?)}";
            connect.pstat = connect.conn.prepareCall(sql);
            connect.pstat.setInt(1, id);
            connect.pstat.execute();
            return OperationResult.success("Status film berhasil diubah.");
        } catch (SQLException e) {
            return OperationResult.failure("Gagal mengubah status film: " + e.getMessage());
        } finally {
            try {
                if (connect.pstat != null) connect.pstat.close();
            } catch (SQLException e) {
                System.out.println("Error closing toogleStatus: " + e.getMessage());
            }
        }
    }
}
