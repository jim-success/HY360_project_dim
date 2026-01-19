package dao;

import db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StatisticsDAO {

    // --- 1. Βοηθητική Κλάση για τα Σύνολα (Κατάσταση Μισθοδοσίας) ---
    public static class CategoryTotalStats {
        public String category;
        public int employeeCount;
        public double totalCost;

        public CategoryTotalStats(String category, int employeeCount, double totalCost) {
            this.category = category;
            this.employeeCount = employeeCount;
            this.totalCost = totalCost;
        }
    }

    // --- 2. Βοηθητική Κλάση για τα Min/Max/Avg (Στατιστικά Μισθών) ---
    public static class CategorySalaryStats {
        public String category;
        public double minSalary;
        public double maxSalary;
        public double avgSalary;

        public CategorySalaryStats(String category, double minSalary, double maxSalary, double avgSalary) {
            this.category = category;
            this.minSalary = minSalary;
            this.maxSalary = maxSalary;
            this.avgSalary = avgSalary;
        }
    }

    // --- ΜΕΘΟΔΟΣ 1: Παίρνει το συνολικό κόστος ανά κατηγορία ---
    public static List<CategoryTotalStats> getPayrollStatusByCategory() {
        List<CategoryTotalStats> list = new ArrayList<>();

        // Query: Ενώνει Υπαλλήλους & Πληρωμές και ομαδοποιεί ανά Κατηγορία
        String sql =
                "SELECT e.category, COUNT(DISTINCT e.employee_id) as emp_count, SUM(p.amount) as total_cost " +
                        "FROM employee e " +
                        "JOIN payroll_payment p ON e.employee_id = p.employee_id " +
                        "GROUP BY e.category";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new CategoryTotalStats(
                        rs.getString("category"),
                        rs.getInt("emp_count"),
                        rs.getDouble("total_cost")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- ΜΕΘΟΔΟΣ 2: Παίρνει Min, Max, Avg ανά κατηγορία ---
    public static List<CategorySalaryStats> getSalaryStatistics() {
        List<CategorySalaryStats> list = new ArrayList<>();

        // Query: Υπολογίζει ελάχιστο, μέγιστο και μέσο όρο πληρωμών
        String sql =
                "SELECT e.category, MIN(p.amount) as min_sal, MAX(p.amount) as max_sal, AVG(p.amount) as avg_sal " +
                        "FROM payroll_payment p " +
                        "JOIN employee e ON p.employee_id = e.employee_id " +
                        "GROUP BY e.category";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new CategorySalaryStats(
                        rs.getString("category"),
                        rs.getDouble("min_sal"),
                        rs.getDouble("max_sal"),
                        rs.getDouble("avg_sal")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    // --- ΜΕΘΟΔΟΣ 3: Κατάσταση Μισθοδοσίας με Φίλτρα Έτους/Μήνα ---
    public static List<CategoryTotalStats> getPayrollStatusByCategory(int year, int month) {
        List<CategoryTotalStats> list = new ArrayList<>();

        String sql =
                "SELECT e.category, COUNT(DISTINCT e.employee_id) as emp_count, SUM(p.amount) as total_cost " +
                        "FROM payroll_payment p " +
                        "JOIN employee e ON p.employee_id = e.employee_id " +
                        "WHERE YEAR(p.payment_date) = ? AND MONTH(p.payment_date) = ? " +
                        "GROUP BY e.category";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, year);
            ps.setInt(2, month);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new CategoryTotalStats(
                        rs.getString("category"),
                        rs.getInt("emp_count"),
                        rs.getDouble("total_cost")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}