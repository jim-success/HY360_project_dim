package dao;

import db.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Vector;

public class PayrollListDAO {
    public static Vector<String> getColumns() {
        Vector<String> cols = new Vector<>();
        cols.add("ID");
        cols.add("Υπάλληλος");
        cols.add("Υποκατηγορία");
        cols.add("Μισθός (ευρώ)");
        return cols;
    }

    public static Vector<Vector<Object>> getEmployeesWithSalaryByExactCategory(String category) {
        Vector<Vector<Object>> data = new Vector<>();

        String sql =
                "SELECT v.employee_id, v.first_name, v.last_name, v.category, " +
                        "CASE " +
                        "  WHEN v.category = 'TEACH_PERMANENT' THEN COALESCE(p.base_salary,0) + COALESCE(tp.research_allowance,0) " +
                        "  WHEN v.category = 'ADMIN_PERMANENT' THEN COALESCE(p.base_salary,0) " +
                        "  WHEN v.category = 'TEACH_CONTRACT' THEN COALESCE(c.monthly_salary,0) + COALESCE(tc.library_allowance,0) " +
                        "  WHEN v.category = 'ADMIN_CONTRACT' THEN COALESCE(c.monthly_salary,0) " +
                        "  ELSE 0 " +
                        "END AS salary " +
                        "FROM view_employee_details v " +
                        "LEFT JOIN permanent p ON p.employee_id = v.employee_id " +
                        "LEFT JOIN `contract` c ON c.employee_id = v.employee_id " +
                        "LEFT JOIN teaching_permanent tp ON tp.employee_id = v.employee_id " +
                        "LEFT JOIN teaching_contract tc ON tc.employee_id = v.employee_id " +
                        "WHERE v.active = TRUE AND v.category = ? " +
                        "ORDER BY v.last_name, v.first_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("employee_id");
                    String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
                    String cat = rs.getString("category");
                    BigDecimal salary = rs.getBigDecimal("salary");

                    Vector<Object> row = new Vector<>();
                    row.add(id);
                    row.add(fullName);
                    row.add(cat);
                    row.add(salary);
                    data.add(row);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

}
