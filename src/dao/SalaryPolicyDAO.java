package dao;

import db.DBConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalaryPolicyDAO {
    public static class PolicyRow {
        public String category;
        public BigDecimal baseSalary;
        public BigDecimal researchAllowance;
        public BigDecimal libraryAllowance;
    }

    private static BigDecimal nz(BigDecimal b) {
        return b == null ? BigDecimal.ZERO : b;
    }

    public static List<PolicyRow> getCurrentPolicyRows() {
        List<PolicyRow> rows = new ArrayList<>();

        rows.add(fetchAdminPermanent());
        rows.add(fetchTeachPermanent());
        rows.add(fetchAdminContract());
        rows.add(fetchTeachContract());

        return rows;
    }

    private static PolicyRow fetchAdminPermanent() {
        PolicyRow r = new PolicyRow();
        r.category = "ADMIN_PERMANENT";

        String sql =
                "SELECT MAX(p.base_salary) AS max_base " +
                        "FROM permanent p " +
                        "LEFT JOIN teaching_permanent tp ON tp.employee_id = p.employee_id " +
                        "WHERE tp.employee_id IS NULL";

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) r.baseSalary = nz(rs.getBigDecimal("max_base"));
        } catch (Exception e) {
            r.baseSalary = BigDecimal.ZERO;
        }

        r.researchAllowance = BigDecimal.ZERO;
        r.libraryAllowance = BigDecimal.ZERO;
        return r;
    }

    private static PolicyRow fetchTeachPermanent() {
        PolicyRow r = new PolicyRow();
        r.category = "TEACH_PERMANENT";

        String sqlBase =
                "SELECT MAX(p.base_salary) AS max_base " +
                        "FROM permanent p " +
                        "JOIN teaching_permanent tp ON tp.employee_id = p.employee_id";

        String sqlRes =
                "SELECT MAX(research_allowance) AS max_res " +
                        "FROM teaching_permanent";

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {

            try (ResultSet rs = st.executeQuery(sqlBase)) {
                if (rs.next()) r.baseSalary = nz(rs.getBigDecimal("max_base"));
            }
            try (ResultSet rs = st.executeQuery(sqlRes)) {
                if (rs.next()) r.researchAllowance = nz(rs.getBigDecimal("max_res"));
            }

        } catch (Exception e) {
            r.baseSalary = BigDecimal.ZERO;
            r.researchAllowance = BigDecimal.ZERO;
        }

        r.libraryAllowance = BigDecimal.ZERO;
        return r;
    }

    private static PolicyRow fetchAdminContract() {
        PolicyRow r = new PolicyRow();
        r.category = "ADMIN_CONTRACT";

        String sql =
                "SELECT MAX(c.monthly_salary) AS max_base " +
                        "FROM contract c " +
                        "LEFT JOIN teaching_contract tc ON tc.employee_id = c.employee_id " +
                        "WHERE tc.employee_id IS NULL";

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) r.baseSalary = nz(rs.getBigDecimal("max_base"));
        } catch (Exception e) {
            r.baseSalary = BigDecimal.ZERO;
        }

        r.researchAllowance = BigDecimal.ZERO;
        r.libraryAllowance = BigDecimal.ZERO;
        return r;
    }

    private static PolicyRow fetchTeachContract() {
        PolicyRow r = new PolicyRow();
        r.category = "TEACH_CONTRACT";

        String sqlBase =
                "SELECT MAX(c.monthly_salary) AS max_base " +
                        "FROM contract c " +
                        "JOIN teaching_contract tc ON tc.employee_id = c.employee_id";

        String sqlLib =
                "SELECT MAX(library_allowance) AS max_lib " +
                        "FROM teaching_contract";

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {

            try (ResultSet rs = st.executeQuery(sqlBase)) {
                if (rs.next()) r.baseSalary = nz(rs.getBigDecimal("max_base"));
            }
            try (ResultSet rs = st.executeQuery(sqlLib)) {
                if (rs.next()) r.libraryAllowance = nz(rs.getBigDecimal("max_lib"));
            }

        } catch (Exception e) {
            r.baseSalary = BigDecimal.ZERO;
            r.libraryAllowance = BigDecimal.ZERO;
        }

        r.researchAllowance = BigDecimal.ZERO;
        return r;
    }

    public static void updatePoliciesNoDecrease(List<PolicyRow> newRows) {
        List<PolicyRow> oldRows = getCurrentPolicyRows();

        for (PolicyRow n : newRows) {
            PolicyRow o = oldRows.stream().filter(x -> x.category.equals(n.category)).findFirst().orElse(null);
            if (o == null) continue;

            if (nz(n.baseSalary).compareTo(nz(o.baseSalary)) < 0)
                throw new IllegalArgumentException("Δεν επιτρέπεται μείωση βασικού μισθού για " + n.category);

            if (nz(n.researchAllowance).compareTo(nz(o.researchAllowance)) < 0)
                throw new IllegalArgumentException("Δεν επιτρέπεται μείωση research_allowance για " + n.category);

            if (nz(n.libraryAllowance).compareTo(nz(o.libraryAllowance)) < 0)
                throw new IllegalArgumentException("Δεν επιτρέπεται μείωση library_allowance για " + n.category);
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                for (PolicyRow n : newRows) {
                    applyUpdate(conn, n);
                }
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void applyUpdate(Connection conn, PolicyRow n) throws SQLException {
        if ("ADMIN_PERMANENT".equals(n.category)) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE permanent p " +
                            "LEFT JOIN teaching_permanent tp ON tp.employee_id = p.employee_id " +
                            "SET p.base_salary = ? " +
                            "WHERE tp.employee_id IS NULL"
            )) {
                ps.setBigDecimal(1, nz(n.baseSalary));
                ps.executeUpdate();
            }
        }

        if ("TEACH_PERMANENT".equals(n.category)) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE permanent p " +
                            "JOIN teaching_permanent tp ON tp.employee_id = p.employee_id " +
                            "SET p.base_salary = ?"
            )) {
                ps.setBigDecimal(1, nz(n.baseSalary));
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE teaching_permanent SET research_allowance = ?"
            )) {
                ps.setBigDecimal(1, nz(n.researchAllowance));
                ps.executeUpdate();
            }
        }

        if ("ADMIN_CONTRACT".equals(n.category)) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE contract c " +
                            "LEFT JOIN teaching_contract tc ON tc.employee_id = c.employee_id " +
                            "SET c.monthly_salary = ? " +
                            "WHERE tc.employee_id IS NULL"
            )) {
                ps.setBigDecimal(1, nz(n.baseSalary));
                ps.executeUpdate();
            }
        }

        if ("TEACH_CONTRACT".equals(n.category)) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE contract c " +
                            "JOIN teaching_contract tc ON tc.employee_id = c.employee_id " +
                            "SET c.monthly_salary = ?"
            )) {
                ps.setBigDecimal(1, nz(n.baseSalary));
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE teaching_contract SET library_allowance = ?"
            )) {
                ps.setBigDecimal(1, nz(n.libraryAllowance));
                ps.executeUpdate();
            }
        }
    }
}
