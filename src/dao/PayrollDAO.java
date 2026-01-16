package dao;

import db.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class PayrollDAO {

    public static double calculateSalary(int employeeId) {

        double salary = 1200.0; // βασικός μισθός (κανόνας άσκησης)

        String sql =
                "SELECT marital_status, number_of_children " +
                        "FROM employee " +
                        "WHERE employee_id = ? AND active = TRUE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                String maritalStatus = rs.getString("marital_status");
                int children = rs.getInt("number_of_children");

                if ("married".equalsIgnoreCase(maritalStatus)) {
                    salary += 100;
                }

                salary += children * 50;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return salary;
    }



    public static boolean insertPayment(int employeeId, double amount) {
        String sql =
                "INSERT INTO payroll_payment (employee_id, payment_date, amount) " +
                        "VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeId);
            ps.setDate(2, Date.valueOf(LocalDate.now()));
            ps.setDouble(3, amount);

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Vector<Vector<Object>> getPayrollHistory() {
        Vector<Vector<Object>> data = new Vector<>();

        String sql =
                "SELECT e.first_name, e.last_name, " +
                        "p.payment_date, p.amount " +
                        "FROM payroll_payment p " +
                        "JOIN employee e ON p.employee_id = e.employee_id " +
                        "ORDER BY p.created_at DESC";



        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("first_name") + " " + rs.getString("last_name"));
                row.add(rs.getDate("payment_date"));
                row.add(rs.getDouble("amount"));
                data.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return data;
    }

    public static Vector<String> getPayrollHistoryColumns() {
        Vector<String> columns = new Vector<>();
        columns.add("Υπάλληλος");
        columns.add("Ημερομηνία");
        columns.add("Ποσό (€)");
        return columns;
    }

    public static Vector<Vector<Object>> getPayrollByDepartment() {

        Vector<Vector<Object>> data = new Vector<>();

        String sql =
                "SELECT d.name AS department, " +
                        "COUNT(DISTINCT e.employee_id) AS employees, " +
                        "SUM(pp.amount) AS total_payroll " +
                        "FROM employee e " +
                        "JOIN department d ON e.department_id = d.department_id " +
                        "LEFT JOIN payroll_payment pp ON pp.payment_id = ( " +
                        "   SELECT p2.payment_id " +
                        "   FROM payroll_payment p2 " +
                        "   WHERE p2.employee_id = e.employee_id " +
                        "   ORDER BY p2.payment_date DESC " +
                        "   LIMIT 1 " +
                        ") " +
                        "WHERE e.active = TRUE " +
                        "GROUP BY d.department_id, d.name " +
                        "ORDER BY d.name";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("department"));
                row.add(rs.getInt("employees"));
                row.add(rs.getDouble("total_payroll"));
                data.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }


    public static Vector<String> getPayrollByDepartmentColumns() {
        Vector<String> cols = new Vector<>();
        cols.add("Τμήμα");
        cols.add("Αριθμός Υπαλλήλων");
        cols.add("Συνολικό Ποσό (€)");
        return cols;
    }

    public static Vector<Vector<Object>> getSalaryStatsByDepartmentJava() {
        Vector<Vector<Object>> data = new Vector<>();

        String sql =
                "SELECT d.department_id, d.name AS department, e.employee_id " +
                        "FROM employee e " +
                        "JOIN department d ON e.department_id = d.department_id " +
                        "WHERE e.active = TRUE";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // department -> salaries
            Map<String, Vector<Double>> map = new LinkedHashMap<>();

            while (rs.next()) {
                String dept = rs.getString("department");
                int empId = rs.getInt("employee_id");

                double salary = calculateSalary(empId);

                map.putIfAbsent(dept, new Vector<>());
                map.get(dept).add(salary);
            }

            for (String dept : map.keySet()) {
                Vector<Double> salaries = map.get(dept);

                double min = salaries.stream().min(Double::compare).orElse(0.0);
                double max = salaries.stream().max(Double::compare).orElse(0.0);
                double avg = salaries.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

                Vector<Object> row = new Vector<>();
                row.add(dept);
                row.add(min);
                row.add(max);
                row.add(avg);

                data.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }




    public static Vector<String> getSalaryStatsByDepartmentColumns() {
        Vector<String> cols = new Vector<>();
        cols.add("Τμήμα");
        cols.add("Ελάχιστος Μισθός");
        cols.add("Μέγιστος Μισθός");
        cols.add("Μέσος Μισθός");
        return cols;
    }



    public static Vector<String> getPayrollStatusByCategoryColumns() {
        Vector<String> cols = new Vector<>();
        cols.add("Κατηγορία");
        cols.add("Ενεργοί Υπάλληλοι");
        cols.add("Πληρωμένοι (μήνα)");
        cols.add("Μη Πληρωμένοι");
        cols.add("Σύνολο Ποσού (€)");
        cols.add("Τελευταία Πληρωμή (μήνα)");
        return cols;
    }

    public static Vector<Vector<Object>> getPayrollStatusByCategory(int year, int month) {
        Vector<Vector<Object>> data = new Vector<>();

        String sql =
                "SELECT " +
                        "  CASE " +
                        "    WHEN ae.employee_id IS NOT NULL THEN 'Administrative' " +
                        "    WHEN te.employee_id IS NOT NULL THEN 'Teaching' " +
                        "    WHEN ce.employee_id IS NOT NULL THEN 'Contract' " +
                        "    WHEN pe.employee_id IS NOT NULL THEN 'Permanent' " +
                        "    ELSE 'Uncategorized' " +
                        "  END AS category, " +
                        "  COUNT(DISTINCT e.employee_id) AS total_employees, " +
                        "  COUNT(DISTINCT CASE WHEN pp.payment_id IS NOT NULL THEN e.employee_id END) AS paid_employees, " +
                        "  (COUNT(DISTINCT e.employee_id) - COUNT(DISTINCT CASE WHEN pp.payment_id IS NOT NULL THEN e.employee_id END)) AS unpaid_employees, " +
                        "  COALESCE(SUM(pp.amount), 0) AS total_amount, " +
                        "  MAX(pp.payment_date) AS last_payment_date " +
                        "FROM employee e " +
                        "LEFT JOIN administrative_employee ae ON ae.employee_id = e.employee_id " +
                        "LEFT JOIN teaching_employee te ON te.employee_id = e.employee_id " +
                        "LEFT JOIN contract_employee ce ON ce.employee_id = e.employee_id " +
                        "LEFT JOIN permanent_employee pe ON pe.employee_id = e.employee_id " +
                        "LEFT JOIN payroll_payment pp ON pp.employee_id = e.employee_id " +
                        "  AND YEAR(pp.payment_date) = ? AND MONTH(pp.payment_date) = ? " +
                        "WHERE e.active = TRUE " +
                        "GROUP BY category " +
                        "ORDER BY category";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, year);
            ps.setInt(2, month);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getString("category"));
                    row.add(rs.getInt("total_employees"));
                    row.add(rs.getInt("paid_employees"));
                    row.add(rs.getInt("unpaid_employees"));
                    row.add(rs.getDouble("total_amount"));
                    row.add(rs.getDate("last_payment_date"));
                    data.add(row);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }
    public static Vector<String> getPayrollStatusByEmployeeCategoryColumns() {
        Vector<String> cols = new Vector<>();
        cols.add("Κατηγορία");
        cols.add("Ενεργοί Υπάλληλοι");
        cols.add("Πληρωμένοι (μήνα)");
        cols.add("Μη Πληρωμένοι");
        cols.add("Σύνολο Ποσού (€)");
        cols.add("Τελευταία Πληρωμή (μήνα)");
        return cols;
    }

    public static Vector<Vector<Object>> getPayrollStatusByEmployeeCategory(int year, int month) {
        Vector<Vector<Object>> data = new Vector<>();

        String sql =
                "SELECT " +
                        "  CASE " +
                        "    WHEN e.employment_type = 'PERMANENT' AND e.personnel_category = 'ADMINISTRATIVE' THEN 'Μόνιμος Διοικητικός' " +
                        "    WHEN e.employment_type = 'CONTRACT'   AND e.personnel_category = 'ADMINISTRATIVE' THEN 'Συμβασιούχος Διοικητικός' " +
                        "    WHEN e.employment_type = 'PERMANENT' AND e.personnel_category = 'TEACHING'       THEN 'Μόνιμο Διδακτικό' " +
                        "    WHEN e.employment_type = 'CONTRACT'   AND e.personnel_category = 'TEACHING'       THEN 'Συμβασιούχο Διδακτικό' " +
                        "    ELSE 'Άγνωστο' " +
                        "  END AS category, " +
                        "  COUNT(DISTINCT e.employee_id) AS total_employees, " +
                        "  COUNT(DISTINCT CASE WHEN pp.payment_id IS NOT NULL THEN e.employee_id END) AS paid_employees, " +
                        "  (COUNT(DISTINCT e.employee_id) - COUNT(DISTINCT CASE WHEN pp.payment_id IS NOT NULL THEN e.employee_id END)) AS unpaid_employees, " +
                        "  COALESCE(SUM(pp.amount), 0) AS total_amount, " +
                        "  MAX(pp.payment_date) AS last_payment_date " +
                        "FROM employee e " +
                        "LEFT JOIN payroll_payment pp ON pp.employee_id = e.employee_id " +
                        "  AND YEAR(pp.payment_date) = ? AND MONTH(pp.payment_date) = ? " +
                        "WHERE e.active = TRUE " +
                        "GROUP BY category " +
                        "ORDER BY category";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, year);
            ps.setInt(2, month);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getString("category"));
                    row.add(rs.getInt("total_employees"));
                    row.add(rs.getInt("paid_employees"));
                    row.add(rs.getInt("unpaid_employees"));
                    row.add(rs.getDouble("total_amount"));
                    row.add(rs.getDate("last_payment_date"));
                    data.add(row);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }



    public static Vector<Vector<Object>> getSalaryStatsByCategoryJava() {
        Vector<Vector<Object>> data = new Vector<>();

        // Θέλουμε ΠΑΝΤΑ 2 κατηγορίες
        Map<String, Vector<Double>> map = new LinkedHashMap<>();
        map.put("Διοικητικό", new Vector<>());
        map.put("Διδακτικό", new Vector<>());

        String sql =
                "SELECT employee_id, personnel_category " +
                        "FROM employee " +
                        "WHERE active = TRUE";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int empId = rs.getInt("employee_id");
                String cat = rs.getString("personnel_category"); // 'ADMINISTRATIVE' ή 'TEACHING'

                String label;
                if ("ADMINISTRATIVE".equals(cat)) label = "Διοικητικό";
                else if ("TEACHING".equals(cat)) label = "Διδακτικό";
                else label = null;

                if (label != null) {
                    double salary = calculateSalary(empId);
                    map.get(label).add(salary);
                }
            }

            for (String label : map.keySet()) {
                Vector<Double> salaries = map.get(label);

                double min = salaries.isEmpty() ? 0.0 : salaries.stream().min(Double::compare).orElse(0.0);
                double max = salaries.isEmpty() ? 0.0 : salaries.stream().max(Double::compare).orElse(0.0);
                double avg = salaries.isEmpty() ? 0.0 : salaries.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

                Vector<Object> row = new Vector<>();
                row.add(label);
                row.add(min);
                row.add(max);
                row.add(avg);
                data.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    public static Vector<String> getSalaryStatsByCategoryColumns() {
        Vector<String> cols = new Vector<>();
        cols.add("Κατηγορία Προσωπικού");
        cols.add("Ελάχιστος Μισθός");
        cols.add("Μέγιστος Μισθός");
        cols.add("Μέσος Μισθός");
        return cols;
    }




    public static Vector<String> getPayrollStatusByPersonnelCategoryColumns() {
        Vector<String> cols = new Vector<>();
        cols.add("Κατηγορία");
        cols.add("Ενεργοί Υπάλληλοι");
        cols.add("Πληρωμένοι (μήνα)");
        cols.add("Μη Πληρωμένοι");
        cols.add("Σύνολο Ποσού (€)");
        cols.add("Τελευταία Πληρωμή (μήνα)");
        return cols;
    }

    public static Vector<Vector<Object>> getPayrollStatusByPersonnelCategory(int year, int month) {
        Vector<Vector<Object>> data = new Vector<>();

        // Για να βγάζει ΠΑΝΤΑ 2 γραμμές
        Map<String, Object[]> map = new LinkedHashMap<>();
        map.put("Διοικητικό", new Object[]{0, 0, 0, 0.0, null});
        map.put("Διδακτικό", new Object[]{0, 0, 0, 0.0, null});

        String sql =
                "SELECT e.personnel_category AS cat, " +
                        "COUNT(DISTINCT e.employee_id) AS total_employees, " +
                        "COUNT(DISTINCT CASE WHEN pp.payment_id IS NOT NULL THEN e.employee_id END) AS paid_employees, " +
                        "(COUNT(DISTINCT e.employee_id) - COUNT(DISTINCT CASE WHEN pp.payment_id IS NOT NULL THEN e.employee_id END)) AS unpaid_employees, " +
                        "COALESCE(SUM(pp.amount), 0) AS total_amount, " +
                        "MAX(pp.payment_date) AS last_payment_date " +
                        "FROM employee e " +
                        "LEFT JOIN payroll_payment pp ON pp.employee_id = e.employee_id " +
                        " AND YEAR(pp.payment_date) = ? AND MONTH(pp.payment_date) = ? " +
                        "WHERE e.active = TRUE " +
                        "GROUP BY e.personnel_category";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, year);
            ps.setInt(2, month);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String cat = rs.getString("cat");
                    String label =
                            "ADMINISTRATIVE".equals(cat) ? "Διοικητικό" :
                                    "TEACHING".equals(cat) ? "Διδακτικό" :
                                            null;

                    if (label != null) {
                        map.put(label, new Object[]{
                                rs.getInt("total_employees"),
                                rs.getInt("paid_employees"),
                                rs.getInt("unpaid_employees"),
                                rs.getDouble("total_amount"),
                                rs.getDate("last_payment_date")
                        });
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String label : map.keySet()) {
            Object[] v = map.get(label);
            Vector<Object> row = new Vector<>();
            row.add(label);
            row.add(v[0]);
            row.add(v[1]);
            row.add(v[2]);
            row.add(v[3]);
            row.add(v[4]);
            data.add(row);
        }

        return data;
    }

}
