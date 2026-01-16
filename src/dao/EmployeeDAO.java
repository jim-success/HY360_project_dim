package dao;

import db.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class EmployeeDAO {

    // --- 1. ΛΙΣΤΑ ΟΛΩΝ ΤΩΝ ΥΠΑΛΛΗΛΩΝ (Για τον πίνακα στο EmployeesFrame) ---
    public static Vector<Vector<Object>> getAllEmployees() {
        Vector<Vector<Object>> data = new Vector<>();

        // Προσοχή: Εδώ επιλέγουμε το 'category' αντί για τα παλιά πεδία
        String sql =
                "SELECT e.employee_id, " +
                        "e.first_name, " +
                        "e.last_name, " +
                        "d.name AS department, " +
                        "e.marital_status, " +
                        "e.number_of_children, " +
                        "e.category, " +
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

                // --- ΛΟΓΙΚΗ ΜΕΤΑΤΡΟΠΗΣ CATEGORY ΣΕ ΕΛΛΗΝΙΚΑ ---
                String dbCategory = rs.getString("category"); // π.χ. ADMIN_PERMANENT

                String typeLabel = "-";
                String catLabel = "-";

                if (dbCategory != null) {
                    // Τύπος Εργασίας
                    if (dbCategory.contains("PERMANENT")) {
                        typeLabel = "Μόνιμος";
                    } else if (dbCategory.contains("CONTRACT")) {
                        typeLabel = "Συμβασιούχος";
                    }

                    // Κατηγορία Προσωπικού
                    if (dbCategory.contains("ADMIN")) {
                        catLabel = "Διοικητικό";
                    } else if (dbCategory.contains("TEACH")) {
                        catLabel = "Διδακτικό";
                    }
                }

                row.add(typeLabel); // Στήλη: Τύπος
                row.add(catLabel);  // Στήλη: Κατηγορία
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
        columns.add("Τύπος Εργασίας");       // Μόνιμος/Συμβασιούχος
        columns.add("Κατηγορία Προσωπικού"); // Διοικητικό/Διδακτικό
        columns.add("Κατάσταση");
        return columns;
    }

    // --- 2. ΛΙΣΤΑ ΟΝΟΜΑΤΩΝ (Για Dropdowns) ---
    public static Map<Integer, String> getEmployeeNames() {
        Map<Integer, String> employees = new LinkedHashMap<>();
        String sql = "SELECT employee_id, first_name, last_name FROM employee WHERE active = TRUE";

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

    // --- 3. ΕΙΣΑΓΩΓΗ ΥΠΑΛΛΗΛΟΥ (INSERT) ---
    // Ενοποιημένη μέθοδος insert που υποστηρίζει όλα τα πεδία της νέας βάσης
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
            String category,          // ΝΕΟ: π.χ. ADMIN_CONTRACT
            String contractEnd,       // ΝΕΟ: null αν είναι μόνιμος
            Double contractSalary     // ΝΕΟ: null αν είναι μόνιμος
    ) {
        LocalDate date = LocalDate.parse(startDate);
        if (date.getDayOfMonth() != 1) {
            // Μπορείς να βάλεις και έλεγχο date.isBefore(LocalDate.now()) αν θες αυστηρότητα
            // Αλλά για testing ας το αφήσουμε πιο χαλαρό ή μόνο για 1η του μήνα
        }

        String sql = "INSERT INTO employee " +
                "(first_name, last_name, marital_status, number_of_children, " +
                "department_id, start_date, address, phone, bank_account, bank_name, " +
                "category, contract_end, contract_salary) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            ps.setString(11, category);

            // Χειρισμός NULL για contract_end
            if (contractEnd == null || contractEnd.trim().isEmpty()) {
                ps.setNull(12, Types.DATE);
            } else {
                ps.setDate(12, Date.valueOf(contractEnd));
            }

            // Χειρισμός NULL για contract_salary
            if (contractSalary == null || contractSalary == 0.0) {
                ps.setNull(13, Types.DECIMAL);
            } else {
                ps.setDouble(13, contractSalary);
            }

            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- 4. ΒΟΗΘΗΤΙΚΗ ΚΛΑΣΗ EmployeeDetails (Προσαρμοσμένη στη νέα βάση) ---
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

        // ΝΕΑ ΠΕΔΙΑ
        public String category;        // π.χ. TEACH_PERMANENT
        public LocalDate contractEnd;  // Λήξη σύμβασης
        public Double contractSalary;  // Μισθός σύμβασης
    }

    // --- 5. ΛΗΨΗ ΛΕΠΤΟΜΕΡΕΙΩΝ (SELECT ONE) ---
    public static EmployeeDetails getEmployeeDetails(int employeeId) {
        String sql =
                "SELECT * FROM employee WHERE employee_id = ?";

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

                // ΝΕΑ ΠΕΔΙΑ
                d.category = rs.getString("category");

                Date ced = rs.getDate("contract_end");
                d.contractEnd = (ced == null) ? null : ced.toLocalDate();

                d.contractSalary = rs.getObject("contract_salary") != null ? rs.getDouble("contract_salary") : null;

                return d;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- 6. ΕΝΗΜΕΡΩΣΗ ΣΤΟΙΧΕΙΩΝ (UPDATE FULL) ---
    public static boolean updateEmployeeDetails(int employeeId, EmployeeDetails d) {

        String sql =
                "UPDATE employee SET " +
                        "first_name = ?, last_name = ?, marital_status = ?, number_of_children = ?, " +
                        "department_id = ?, start_date = ?, address = ?, phone = ?, " +
                        "bank_account = ?, bank_name = ?, active = ?, termination_date = ?, " +
                        "category = ?, contract_end = ?, contract_salary = ? " +
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

            // ΝΕΑ ΠΕΔΙΑ
            ps.setString(13, d.category);

            if (d.contractEnd == null) ps.setNull(14, Types.DATE);
            else ps.setDate(14, Date.valueOf(d.contractEnd));

            if (d.contractSalary == null) ps.setNull(15, Types.DECIMAL);
            else ps.setDouble(15, d.contractSalary);

            ps.setInt(16, employeeId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- 7. ΑΠΟΛΥΣΗ (TERMINATION) ---
    public static boolean markForTermination(int employeeId) {
        LocalDate today = LocalDate.now();
        // Έλεγχος αν είναι τελευταία μέρα (προαιρετικά, ανάλογα τις απαιτήσεις)
        // LocalDate lastDay = today.withDayOfMonth(today.lengthOfMonth());
        // if (!today.equals(lastDay)) { ... }

        String sql = "UPDATE employee SET termination_date = ?, active = FALSE WHERE employee_id = ?";

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
}