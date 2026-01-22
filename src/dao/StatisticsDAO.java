package dao;

import db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StatisticsDAO {
    public static class CategoryTotalStats {
        public String category;
        public int employeeCount;
        public double totalCost;

        public CategoryTotalStats(String c, int e, double t) {
            category = c;
            employeeCount = e;
            totalCost = t;
        }
    }

    public static class CategorySalaryStats {
        public String category;
        public double minSalary, maxSalary, avgSalary;

        public CategorySalaryStats(String c, double min, double max, double avg) {
            category = c;
            minSalary = min;
            maxSalary = max;
            avgSalary = avg;
        }
    }

    public static List<CategoryTotalStats> getPayrollStatusByCategory(int year, int month) {
        List<CategoryTotalStats> list = new ArrayList<>();
        String sql =
                "SELECT v.category, COUNT(DISTINCT v.employee_id) as count, SUM(p.amount) as cost " +
                        "FROM view_employee_details v " +
                        "JOIN payroll_payment p ON v.employee_id = p.employee_id " +
                        "WHERE YEAR(p.payment_date) = ? AND MONTH(p.payment_date) = ? " +
                        "GROUP BY v.category";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new CategoryTotalStats(rs.getString("category"), rs.getInt("count"), rs.getDouble("cost")));
            }
        } catch (Exception e) {
        }
        return list;
    }

    public static List<CategorySalaryStats> getSalaryStatistics() {
        List<CategorySalaryStats> list = new ArrayList<>();
        String sql =
                "SELECT v.category, MIN(p.amount), MAX(p.amount), AVG(p.amount) " +
                        "FROM view_employee_details v " +
                        "JOIN payroll_payment p ON v.employee_id = p.employee_id " +
                        "GROUP BY v.category";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new CategorySalaryStats(rs.getString(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4)));
            }
        } catch (Exception e) {
        }
        return list;
    }
}