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

}
