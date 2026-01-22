package dao;

import db.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class EmployeeDAO {
    public static Vector<Vector<Object>> getAllEmployees() {
        Vector<Vector<Object>> data = new Vector<>();
        String sql = "SELECT * FROM view_employee_details ORDER BY employee_id";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("employee_id"));
                row.add(rs.getString("first_name"));
                row.add(rs.getString("last_name"));
                row.add(rs.getString("department_name"));
                row.add(rs.getString("marital_status"));
                row.add(rs.getInt("number_of_children"));

                String rawCat = rs.getString("category");
                String type = (rawCat != null && rawCat.contains("PERMANENT")) ? "Μόνιμος" : "Συμβασιούχος";
                String role = (rawCat != null && rawCat.contains("ADMIN")) ? "Διοικητικό" : "Διδακτικό";

                row.add(type);
                row.add(role);
                row.add(rs.getBoolean("active") ? "Ενεργός" : "Ανενεργός");

                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static Vector<String> getColumnNames() {
        Vector<String> columns = new Vector<>();
        columns.add("ID");
        columns.add("Όνομα");
        columns.add("Επώνυμο");
        columns.add("Τμήμα");
        columns.add("Οικ. Κατάσταση");
        columns.add("Παιδιά");
        columns.add("Τύπος");
        columns.add("Κατηγορία");
        columns.add("Κατάσταση");
        return columns;
    }

    public static Map<Integer, String> getEmployeeNames() {
        Map<Integer, String> employees = new LinkedHashMap<>();
        String sql = "SELECT employee_id, first_name, last_name FROM employee WHERE active = TRUE";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                employees.put(rs.getInt("employee_id"), rs.getString("first_name") + " " + rs.getString("last_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    public static boolean insertEmployee(String firstName, String lastName, String maritalStatus,
                                         int childrenCount, int departmentId, String startDate,
                                         String address, String phone, String bankAccount, String bankName,
                                         String category, String contractEnd, Double salaryInput) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String sqlEmp = "INSERT INTO employee (first_name, last_name, department_id, marital_status, start_date, address, phone, bank_account, bank_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement psEmp = conn.prepareStatement(sqlEmp, Statement.RETURN_GENERATED_KEYS);
            psEmp.setString(1, firstName);
            psEmp.setString(2, lastName);
            psEmp.setInt(3, departmentId);
            psEmp.setString(4, maritalStatus);
            psEmp.setDate(5, Date.valueOf(startDate));
            psEmp.setString(6, address);
            psEmp.setString(7, phone);
            psEmp.setString(8, bankAccount);
            psEmp.setString(9, bankName);
            psEmp.executeUpdate();

            ResultSet rsKey = psEmp.getGeneratedKeys();
            if (!rsKey.next()) throw new SQLException("Failed to create employee");
            int empId = rsKey.getInt(1);

            if (childrenCount > 0) {
                String sqlChild = "INSERT INTO child (employee_id, birth_date) VALUES (?, ?)";
                PreparedStatement psChild = conn.prepareStatement(sqlChild);
                for (int i = 0; i < childrenCount; i++) {
                    psChild.setInt(1, empId);
                    psChild.setDate(2, Date.valueOf("2015-01-01"));
                    psChild.addBatch();
                }
                psChild.executeBatch();
            }

            if (category.contains("PERMANENT")) {
                double baseSalary = (salaryInput != null) ? salaryInput : 1000.0;
                String sqlPerm = "INSERT INTO permanent (employee_id, base_salary, years_of_service) VALUES (?, ?, 0)";
                PreparedStatement psPerm = conn.prepareStatement(sqlPerm);
                psPerm.setInt(1, empId);
                psPerm.setDouble(2, baseSalary);
                psPerm.executeUpdate();

                if (category.contains("TEACH")) {

                    double researchAllowance = 250.0;

                    try (Statement st = conn.createStatement();
                         ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(research_allowance), 0) AS val FROM teaching_permanent")) {
                        if (rs.next()) {
                            double v = rs.getDouble("val");
                            if (v > 0) researchAllowance = v;
                        }
                    }

                    String sqlTeach = "INSERT INTO teaching_permanent (employee_id, research_allowance) VALUES (?, ?)";
                    try (PreparedStatement psTeach = conn.prepareStatement(sqlTeach)) {
                        psTeach.setInt(1, empId);
                        psTeach.setDouble(2, researchAllowance);
                        psTeach.executeUpdate();
                    }
                }

            } else {
                double monthlySalary = (salaryInput != null) ? salaryInput : 800.0;
                String cEnd = (contractEnd != null && !contractEnd.isEmpty()) ? contractEnd : "2026-12-31";

                String sqlContr = "INSERT INTO contract (employee_id, monthly_salary, contract_start, contract_end) VALUES (?, ?, ?, ?)";
                PreparedStatement psContr = conn.prepareStatement(sqlContr);
                psContr.setInt(1, empId);
                psContr.setDouble(2, monthlySalary);
                psContr.setDate(3, Date.valueOf(startDate));
                psContr.setDate(4, Date.valueOf(cEnd));
                psContr.executeUpdate();

                if (category.contains("TEACH")) {

                    double libraryAllowance = 100.0;

                    try (Statement st = conn.createStatement();
                         ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(library_allowance), 0) AS val FROM teaching_contract")) {
                        if (rs.next()) {
                            double v = rs.getDouble("val");
                            if (v > 0) libraryAllowance = v;
                        }
                    }

                    String sqlTeach = "INSERT INTO teaching_contract (employee_id, library_allowance) VALUES (?, ?)";
                    try (PreparedStatement psTeach = conn.prepareStatement(sqlTeach)) {
                        psTeach.setInt(1, empId);
                        psTeach.setDouble(2, libraryAllowance);
                        psTeach.executeUpdate();
                    }
                }

            }
            conn.commit();
            return true;
        } catch (Exception e) {
            if (conn != null) try {
                conn.rollback();
            } catch (SQLException ex) {
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException ex) {
            }
        }
    }

    public static class EmployeeDetails {
        public String firstName, lastName, maritalStatus, address, phone, bankAccount, bankName, category;
        public int numberOfChildren, departmentId;
        public LocalDate startDate, terminationDate, contractEnd;
        public Double salary;
        public boolean active;
    }

    public static EmployeeDetails getEmployeeDetails(int empId) {
        String sql =
                "SELECT e.*, " +
                        "  (SELECT COUNT(*) FROM child c WHERE c.employee_id = e.employee_id) as children_count, " +
                        "  p.base_salary, c.monthly_salary, c.contract_end, " +
                        "  tp.research_allowance, tc.library_allowance " +
                        "FROM employee e " +
                        "LEFT JOIN permanent p ON e.employee_id = p.employee_id " +
                        "LEFT JOIN contract c ON e.employee_id = c.employee_id " +
                        "LEFT JOIN teaching_permanent tp ON e.employee_id = tp.employee_id " +
                        "LEFT JOIN teaching_contract tc ON e.employee_id = tc.employee_id " +
                        "WHERE e.employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;

            EmployeeDetails d = new EmployeeDetails();
            d.firstName = rs.getString("first_name");
            d.lastName = rs.getString("last_name");
            d.maritalStatus = rs.getString("marital_status");
            d.numberOfChildren = rs.getInt("children_count");
            d.departmentId = rs.getInt("department_id");
            d.address = rs.getString("address");
            d.phone = rs.getString("phone");
            d.bankAccount = rs.getString("bank_account");
            d.bankName = rs.getString("bank_name");
            d.active = rs.getBoolean("active");

            Date sd = rs.getDate("start_date");
            d.startDate = (sd != null) ? sd.toLocalDate() : null;
            Date td = rs.getDate("termination_date");
            d.terminationDate = (td != null) ? td.toLocalDate() : null;
            Date ced = rs.getDate("contract_end");
            d.contractEnd = (ced != null) ? ced.toLocalDate() : null;

            if (rs.getObject("base_salary") != null) {
                d.salary = rs.getDouble("base_salary");
                d.category = (rs.getObject("research_allowance") != null) ? "TEACH_PERMANENT" : "ADMIN_PERMANENT";
            } else {
                d.salary = rs.getDouble("monthly_salary");
                d.category = (rs.getObject("library_allowance") != null) ? "TEACH_CONTRACT" : "ADMIN_CONTRACT";
            }
            return d;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean updateEmployeeDetails(int empId, EmployeeDetails d) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String sql1 = "UPDATE employee SET first_name=?, last_name=?, marital_status=?, department_id=?, address=?, phone=?, bank_account=?, bank_name=?, active=?, termination_date=? WHERE employee_id=?";
            try (PreparedStatement ps = conn.prepareStatement(sql1)) {
                ps.setString(1, d.firstName);
                ps.setString(2, d.lastName);
                ps.setString(3, d.maritalStatus);
                ps.setInt(4, d.departmentId);
                ps.setString(5, d.address);
                ps.setString(6, d.phone);
                ps.setString(7, d.bankAccount);
                ps.setString(8, d.bankName);
                ps.setBoolean(9, d.active);
                ps.setDate(10, d.terminationDate != null ? Date.valueOf(d.terminationDate) : null);
                ps.setInt(11, empId);
                ps.executeUpdate();
            }

            if (d.category.contains("PERMANENT")) {
                String sql2 = "UPDATE permanent SET base_salary=? WHERE employee_id=?";
                try (PreparedStatement ps = conn.prepareStatement(sql2)) {
                    ps.setDouble(1, d.salary);
                    ps.setInt(2, empId);
                    ps.executeUpdate();
                }
            } else {
                String sql3 = "UPDATE contract SET monthly_salary=?, contract_end=? WHERE employee_id=?";
                try (PreparedStatement ps = conn.prepareStatement(sql3)) {
                    ps.setDouble(1, d.salary);
                    ps.setDate(2, d.contractEnd != null ? Date.valueOf(d.contractEnd) : null);
                    ps.setInt(3, empId);
                    ps.executeUpdate();
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            if (conn != null) try {
                conn.rollback();
            } catch (SQLException ex) {
            }
            return false;
        } finally {
            if (conn != null) try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException ex) {
            }
        }
    }

    public static boolean markForTermination(int employeeId) {
        String sql = "UPDATE employee SET termination_date = ?, active = FALSE WHERE employee_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(LocalDate.now()));
            ps.setInt(2, employeeId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static Vector<String> getChildColumns() {
        Vector<String> cols = new Vector<>();
        cols.add("child_id");
        cols.add("birth_date");
        return cols;
    }

    public static Vector<Vector<Object>> getChildrenRows(int employeeId) {
        Vector<Vector<Object>> data = new Vector<>();
        String sql = "SELECT child_id, birth_date FROM child WHERE employee_id = ? ORDER BY child_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("child_id"));
                    row.add(rs.getDate("birth_date"));
                    data.add(row);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    public static boolean addChild(int employeeId, LocalDate birthDate) {
        String sql = "INSERT INTO child (employee_id, birth_date) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeId);
            ps.setDate(2, Date.valueOf(birthDate));
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateChildBirthDate(int childId, LocalDate birthDate) {
        String sql = "UPDATE child SET birth_date = ? WHERE child_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(birthDate));
            ps.setInt(2, childId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteChild(int childId) {
        String sql = "DELETE FROM child WHERE child_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, childId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}