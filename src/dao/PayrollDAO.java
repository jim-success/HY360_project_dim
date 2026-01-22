package dao;

import db.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class PayrollDAO {
    public static double calculateSalary(int employeeId) {
        String sql =
                "SELECT e.marital_status, " +
                        "  (SELECT COUNT(*) FROM child c WHERE c.employee_id = e.employee_id AND TIMESTAMPDIFF(YEAR, c.birth_date, CURDATE()) < 18) as children_count, " +
                        "  p.base_salary, p.years_of_service, c.monthly_salary, " +
                        "  tp.research_allowance, tc.library_allowance " +
                        "FROM employee e " +
                        "LEFT JOIN permanent p ON e.employee_id = p.employee_id " +
                        "LEFT JOIN contract c ON e.employee_id = c.employee_id " +
                        "LEFT JOIN teaching_permanent tp ON e.employee_id = tp.employee_id " +
                        "LEFT JOIN teaching_contract tc ON e.employee_id = tc.employee_id " +
                        "WHERE e.employee_id = ? AND e.active = 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return 0.0;

                String marital = rs.getString("marital_status");
                int children = rs.getInt("children_count");
                double salary = 0.0;

                double basePerm = rs.getDouble("base_salary");
                if (basePerm > 0) {
                    int years = rs.getInt("years_of_service");
                    salary = basePerm + (years > 0 ? basePerm * 0.15 * years : 0);
                } else {
                    salary = rs.getDouble("monthly_salary");
                }

                double familyRate = ("married".equalsIgnoreCase(marital) ? 0.05 : 0) + (children * 0.05);
                salary += (salary * familyRate);

                salary += rs.getDouble("research_allowance") + rs.getDouble("library_allowance");
                return salary;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public static boolean insertPayment(int employeeId, double amount) {
        String sql = "INSERT INTO payroll_payment (employee_id, payment_date, amount) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, Date.valueOf(LocalDate.now()));
            ps.setDouble(3, amount);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static Vector<Vector<Object>> getPayrollHistory() {
        Vector<Vector<Object>> data = new Vector<>();
        String sql = "SELECT e.first_name, e.last_name, p.payment_date, p.amount FROM payroll_payment p JOIN employee e ON p.employee_id = e.employee_id ORDER BY p.created_at DESC";
        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("first_name") + " " + rs.getString("last_name"));
                row.add(rs.getDate("payment_date"));
                row.add(rs.getDouble("amount"));
                data.add(row);
            }
        } catch (SQLException e) {
        }
        return data;
    }

    public static Vector<String> getPayrollHistoryColumns() {
        Vector<String> c = new Vector<>();
        c.add("Υπάλληλος");
        c.add("Ημερομηνία");
        c.add("Ποσό (€)");
        return c;
    }

    public static Vector<Vector<Object>> getPayrollByDepartment() {
        Vector<Vector<Object>> data = new Vector<>();
        String sql = "SELECT d.name, COUNT(DISTINCT e.employee_id) as emps, SUM(pp.amount) as total " +
                "FROM employee e JOIN department d ON e.department_id = d.department_id " +
                "LEFT JOIN payroll_payment pp ON pp.payment_id = (SELECT p2.payment_id FROM payroll_payment p2 WHERE p2.employee_id = e.employee_id ORDER BY p2.payment_date DESC LIMIT 1) " +
                "WHERE e.active = TRUE GROUP BY d.department_id, d.name ORDER BY d.name";
        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("name"));
                row.add(rs.getInt("emps"));
                row.add(rs.getDouble("total"));
                data.add(row);
            }
        } catch (Exception e) {
        }
        return data;
    }

    public static Vector<String> getPayrollByDepartmentColumns() {
        Vector<String> c = new Vector<>();
        c.add("Τμήμα");
        c.add("Υπάλληλοι");
        c.add("Σύνολο (€)");
        return c;
    }

    public static Vector<Vector<Object>> getSalaryStatsByDepartmentJava() {
        Vector<Vector<Object>> data = new Vector<>();
        String sql = "SELECT d.name, e.employee_id FROM employee e JOIN department d ON e.department_id = d.department_id WHERE e.active = 1";
        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            Map<String, Vector<Double>> map = new LinkedHashMap<>();
            while (rs.next()) {
                String dept = rs.getString("name");
                map.putIfAbsent(dept, new Vector<>());
                map.get(dept).add(calculateSalary(rs.getInt("employee_id")));
            }
            for (String dept : map.keySet()) {
                Vector<Double> vals = map.get(dept);
                double min = vals.stream().min(Double::compare).orElse(0.0);
                double max = vals.stream().max(Double::compare).orElse(0.0);
                double avg = vals.stream().mapToDouble(d -> d).average().orElse(0.0);
                Vector<Object> r = new Vector<>();
                r.add(dept);
                r.add(min);
                r.add(max);
                r.add(avg);
                data.add(r);
            }
        } catch (Exception e) {
        }
        return data;
    }

    public static Vector<String> getSalaryStatsByDepartmentColumns() {
        Vector<String> c = new Vector<>();
        c.add("Τμήμα");
        c.add("Min");
        c.add("Max");
        c.add("Avg");
        return c;
    }
}