package cinelux.bioskopcinelux.service;


import cinelux.bioskopcinelux.model.Setting;
import cinelux.bioskopcinelux.util.OperationResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface SettingSrvc {
    Setting mapResultSetToDetailSetting(ResultSet rs) throws SQLException;
    List<Setting> getAllData();
    List<Setting> getAllData(String search,String kategori, Integer status, String urutan, String sortBy);
    Setting getById(int id);
    int getLastId();
    OperationResult insertData(Setting Setting);
    OperationResult deleteData(int id);
    OperationResult updateData(Setting Setting);
    OperationResult toogleStatus(int id);

}
