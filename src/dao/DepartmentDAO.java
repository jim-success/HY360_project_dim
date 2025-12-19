package dao;

import db.DBConnection;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class DepartmentDAO {

    public static Map<Integer, String> getAllDepartments() {
        Map<Integer, String> departments = new LinkedHashMap<>();

        String sql = "SELECT department_id, name FROM department ORDER BY name";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                departments.put(
                        rs.getInt("department_id"),
                        rs.getString("name")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return departments;
    }
}
