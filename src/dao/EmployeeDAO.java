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

        String sql =
                "SELECT e.employee_id, " +
                        "e.first_name, " +
                        "e.last_name, " +
                        "d.name AS department, " +
                        "e.marital_status, " +
                        "e.number_of_children, " +
                        "e.employment_type, " +
                        "e.personnel_category, " +
                        "CASE " +
                        "   WHEN e.active = TRUE THEN 'Ενεργός' " +
                        "   ELSE 'Ανενεργός' " +
                        "END AS status " +
                        "FROM employee e " +
                        "JOIN department d ON e.department_id = d.department_id " +
                        "ORDER BY e.employee_id";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("employee_id"));
                row.add(rs.getString("first_name"));
                row.add(rs.getString("last_name"));
                row.add(rs.getString("department"));
                row.add(rs.getString("marital_status"));
                row.add(rs.getInt("number_of_children"));

                String empType = rs.getString("employment_type");
                String persCat = rs.getString("personnel_category");

                String empTypeLabel =
                        "PERMANENT".equals(empType) ? "Μόνιμος" :
                                "CONTRACT".equals(empType) ? "Συμβασιούχος" :
                                        empType;

                String persCatLabel =
                        "ADMINISTRATIVE".equals(persCat) ? "Διοικητικό" :
                                "TEACHING".equals(persCat) ? "Διδακτικό" :
                                        persCat;

                row.add(empTypeLabel);
                row.add(persCatLabel);

                row.add(rs.getString("status"));
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
        columns.add("Οικογενειακή Κατάσταση");
        columns.add("Παιδιά");
        columns.add("Τύπος Εργασίας");
        columns.add("Κατηγορία Προσωπικού");
        columns.add("Κατάσταση");
        return columns;
    }

    public static boolean insertEmployee(
            String firstName,
            String lastName,
            String maritalStatus,
            int children,
            int departmentId,
            String startDate,
            String address,
            String phone,
            String bankAccount,
            String bankName
    ) {

        LocalDate date = LocalDate.parse(startDate);

        if (date.getDayOfMonth() != 1 || date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Μη έγκυρη ημερομηνία πρόσληψης");
        }

        String sql = "INSERT INTO employee " +
                "(first_name, last_name, marital_status, number_of_children, " +
                "department_id, start_date, address, phone, bank_account, bank_name) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, maritalStatus);
            ps.setInt(4, children);
            ps.setInt(5, departmentId);
            ps.setDate(6, Date.valueOf(startDate));
            ps.setString(7, address);
            ps.setString(8, phone);
            ps.setString(9, bankAccount);
            ps.setString(10, bankName);

            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Map<Integer, String> getEmployeeNames() {
        Map<Integer, String> employees = new LinkedHashMap<>();

        String sql = "SELECT employee_id, first_name, last_name FROM employee";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                employees.put(
                        rs.getInt("employee_id"),
                        rs.getString("first_name") + " " + rs.getString("last_name")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return employees;
    }

    public static boolean updateEmployee(
            int employeeId,
            String address,
            String maritalStatus,
            int children
    ) {
        String sql =
                "UPDATE employee " +
                        "SET address = ?, marital_status = ?, number_of_children = ? " +
                        "WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, address);
            ps.setString(2, maritalStatus);
            ps.setInt(3, children);
            ps.setInt(4, employeeId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean markForTermination(int employeeId) {

        LocalDate today = LocalDate.now();
        LocalDate lastDay = today.withDayOfMonth(today.lengthOfMonth());

        if (!today.equals(lastDay)) {
            throw new IllegalStateException(
                    "Η απόλυση μπορεί να γίνει μόνο την τελευταία ημέρα του μήνα"
            );
        }

        String sql =
                "UPDATE employee " +
                        "SET termination_date = ?, active = TRUE " +
                        "WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(today));
            ps.setInt(2, employeeId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static class EmployeeDetails {
        public String firstName;
        public String lastName;
        public String maritalStatus;
        public int numberOfChildren;
        public int departmentId;
        public LocalDate startDate;
        public String address;
        public String phone;
        public String bankAccount;
        public String bankName;
        public boolean active;
        public LocalDate terminationDate;

        public String employmentType;
        public String personnelCategory;
    }

    public static EmployeeDetails getEmployeeDetails(int employeeId) {
        String sql =
                "SELECT first_name, last_name, marital_status, number_of_children, department_id, " +
                        "start_date, address, phone, bank_account, bank_name, active, termination_date, " +
                        "employment_type, personnel_category " +
                        "FROM employee WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                EmployeeDetails d = new EmployeeDetails();
                d.firstName = rs.getString("first_name");
                d.lastName = rs.getString("last_name");
                d.maritalStatus = rs.getString("marital_status");
                d.numberOfChildren = rs.getInt("number_of_children");
                d.departmentId = rs.getInt("department_id");

                Date sd = rs.getDate("start_date");
                d.startDate = (sd == null) ? null : sd.toLocalDate();

                d.address = rs.getString("address");
                d.phone = rs.getString("phone");
                d.bankAccount = rs.getString("bank_account");
                d.bankName = rs.getString("bank_name");

                d.active = rs.getBoolean("active");

                Date td = rs.getDate("termination_date");
                d.terminationDate = (td == null) ? null : td.toLocalDate();

                d.employmentType = rs.getString("employment_type");
                d.personnelCategory = rs.getString("personnel_category");

                return d;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean updateEmployeeDetails(int employeeId, EmployeeDetails d) {

        if (d.startDate != null && d.startDate.getDayOfMonth() != 1) {
            throw new IllegalArgumentException("Η ημερομηνία πρέπει να είναι 1η του μήνα.");
        }

        String sql =
                "UPDATE employee SET " +
                        "first_name = ?, last_name = ?, marital_status = ?, number_of_children = ?, " +
                        "department_id = ?, start_date = ?, address = ?, phone = ?, " +
                        "bank_account = ?, bank_name = ?, active = ?, termination_date = ?, " +
                        "employment_type = ?, personnel_category = ? " +
                        "WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, d.firstName);
            ps.setString(2, d.lastName);
            ps.setString(3, d.maritalStatus);
            ps.setInt(4, d.numberOfChildren);
            ps.setInt(5, d.departmentId);

            if (d.startDate == null) ps.setNull(6, Types.DATE);
            else ps.setDate(6, Date.valueOf(d.startDate));

            ps.setString(7, d.address);
            ps.setString(8, d.phone);
            ps.setString(9, d.bankAccount);
            ps.setString(10, d.bankName);

            ps.setBoolean(11, d.active);

            if (d.terminationDate == null) ps.setNull(12, Types.DATE);
            else ps.setDate(12, Date.valueOf(d.terminationDate));

            ps.setString(13, d.employmentType);
            ps.setString(14, d.personnelCategory);

            ps.setInt(15, employeeId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean insertEmployee(
            String firstName,
            String lastName,
            String maritalStatus,
            int children,
            int departmentId,
            String startDate,
            String address,
            String phone,
            String bankAccount,
            String bankName,
            String employmentType,
            String personnelCategory
    ) {

        LocalDate date = LocalDate.parse(startDate);

        if (date.getDayOfMonth() != 1 || date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Μη έγκυρη ημερομηνία πρόσληψης");
        }

        String sql = "INSERT INTO employee " +
                "(first_name, last_name, marital_status, number_of_children, " +
                "department_id, start_date, address, phone, bank_account, bank_name, " +
                "employment_type, personnel_category) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, maritalStatus);
            ps.setInt(4, children);
            ps.setInt(5, departmentId);
            ps.setDate(6, Date.valueOf(startDate));
            ps.setString(7, address);
            ps.setString(8, phone);
            ps.setString(9, bankAccount);
            ps.setString(10, bankName);
            ps.setString(11, employmentType);
            ps.setString(12, personnelCategory);

            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
