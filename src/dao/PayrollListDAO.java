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
        cols.add("Κατηγορία");
        cols.add("Βασικός (€)");
        cols.add("Προϋπηρεσία (€)"); // NEW: Προσθήκη στήλης για να φαίνεται η αύξηση
        cols.add("Οικογενειακό (€)");
        cols.add("Ειδικό (€)");
        cols.add("Σύνολο (€)");
        return cols;
    }

    public static Vector<Vector<Object>> getEmployeesSalaryBreakdownByExactCategory(String category) {
        Vector<Vector<Object>> data = new Vector<>();

        String sql =
                "select * from (SELECT v.employee_id, v.first_name, v.last_name, v.category, v.marital_status, v.number_of_children, " +

                        "CASE " +
                        "  WHEN v.category IN ('ADMIN_PERMANENT','TEACH_PERMANENT') THEN COALESCE(p.base_salary,0) " +
                        "  WHEN v.category IN ('ADMIN_CONTRACT','TEACH_CONTRACT') THEN COALESCE(c.monthly_salary,0) " +
                        "  ELSE 0 " +
                        "END AS base_salary, " +

                        "CASE " +
                        "  WHEN v.category IN ('ADMIN_PERMANENT','TEACH_PERMANENT') THEN " +
                        "     (COALESCE(p.base_salary, 0) * 0.15 * COALESCE(p.years_of_service, 0)) " +
                        "  ELSE 0 " +
                        "END AS service_allowance, " +

                        "CASE " +
                        "  WHEN v.category = 'TEACH_PERMANENT' THEN COALESCE(tp.research_allowance,0) " +
                        "  WHEN v.category = 'TEACH_CONTRACT' THEN COALESCE(tc.library_allowance,0) " +
                        "  ELSE 0 " +
                        "END AS special_allowance, " +


                        "CASE " +
                        "  WHEN v.marital_status = 'married' THEN 100 ELSE 0 " +
                        "END + (v.number_of_children * 50) AS family_allowance, " +

                        "(" +
                        "  CASE " +
                        "    WHEN v.category IN ('ADMIN_PERMANENT','TEACH_PERMANENT') THEN COALESCE(p.base_salary,0) " +
                        "    WHEN v.category IN ('ADMIN_CONTRACT','TEACH_CONTRACT') THEN COALESCE(c.monthly_salary,0) " +
                        "    ELSE 0 " +
                        "  END " +
                        "  + " +
                        "  CASE " +
                        "    WHEN v.category IN ('ADMIN_PERMANENT','TEACH_PERMANENT') THEN (COALESCE(p.base_salary, 0) * 0.15 * COALESCE(p.years_of_service, 0)) " +
                        "    ELSE 0 " +
                        "  END " +
                        "  + " +
                        "  (CASE WHEN v.marital_status = 'married' THEN 100 ELSE 0 END + (v.number_of_children * 50)) " +
                        "  + " +
                        "  CASE " +
                        "    WHEN v.category = 'TEACH_PERMANENT' THEN COALESCE(tp.research_allowance,0) " +
                        "    WHEN v.category = 'TEACH_CONTRACT' THEN COALESCE(tc.library_allowance,0) " +
                        "    ELSE 0 " +
                        "  END" +
                        ") AS total " +

                        "FROM view_employee_details v " +
                        "LEFT JOIN permanent p ON p.employee_id = v.employee_id " +
                        "LEFT JOIN `contract` c ON c.employee_id = v.employee_id " +
                        "LEFT JOIN teaching_permanent tp ON tp.employee_id = v.employee_id " +
                        "LEFT JOIN teaching_contract tc ON tc.employee_id = v.employee_id " +
                        "WHERE v.active = TRUE AND v.category = ? " +
                        "ORDER BY v.last_name, v.first_name) vpctt";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("employee_id");
                    String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
                    String cat = rs.getString("category");

                    Vector<Object> row = new Vector<>();
                    row.add(id);
                    row.add(fullName);
                    row.add(cat);
                    row.add(rs.getBigDecimal("base_salary"));
                    row.add(rs.getBigDecimal("service_allowance")); // NEW: Προσθήκη στα data
                    row.add(rs.getBigDecimal("family_allowance"));
                    row.add(rs.getBigDecimal("special_allowance"));
                    row.add(rs.getBigDecimal("total"));

                    data.add(row);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }
}