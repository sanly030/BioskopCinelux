package cinelux.bioskopcinelux.service;

import cinelux.bioskopcinelux.model.Karyawan;
import cinelux.bioskopcinelux.util.OperationResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface KaryawanSrvc {
    Karyawan mapResultSetToDetailKaryawan(ResultSet rs) throws SQLException;
    List<Karyawan> getAllData();
    List<Karyawan> getAllData(String search, String role, Integer status, String urutan, String sortBy);
    Karyawan getById(int id);
    int getLastId();
    OperationResult insertData(Karyawan karyawan);
    OperationResult deleteData(int id, String modifiedBy);
    OperationResult updateData(Karyawan karyawan);

}
