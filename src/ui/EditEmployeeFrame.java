package ui;

import dao.DepartmentDAO;
import dao.EmployeeDAO;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.Map;

public class EditEmployeeFrame extends JFrame {

    // Δηλώνουμε τα πεδία που χρειάζονται πρόσβαση από παντού
    private JTextField contractEndField;
    private JTextField contractSalaryField;
    private JComboBox<String> employmentTypeBox;

    public EditEmployeeFrame() {
        setTitle("Επεξεργασία Υπαλλήλου");
        setSize(550, 650); // Λίγο μεγαλύτερο ύψος
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Dropdown Επιλογής Υπαλλήλου ---
        JComboBox<String> employeeBox = new JComboBox<>();

        // --- Πεδία ---
        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JComboBox<String> maritalBox = new JComboBox<>(new String[]{"single", "married"});
        JTextField childrenField = new JTextField();

        JComboBox<String> departmentBox = new JComboBox<>();
        JTextField startDateField = new JTextField();

        JTextField addressField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField bankAccountField = new JTextField();
        JTextField bankNameField = new JTextField();

        // Κατηγορίες UI
        JComboBox<String> personnelCategoryBox = new JComboBox<>(new String[]{"ADMINISTRATIVE", "TEACHING"});
        employmentTypeBox = new JComboBox<>(new String[]{"PERMANENT", "CONTRACT"});

        // Νέα πεδία Σύμβασης
        contractEndField = new JTextField();
        contractSalaryField = new JTextField();
        contractEndField.setEnabled(false);
        contractSalaryField.setEnabled(false);
        contractEndField.setBackground(Color.LIGHT_GRAY);
        contractSalaryField.setBackground(Color.LIGHT_GRAY);

        JCheckBox activeBox = new JCheckBox("Ενεργός");
        JTextField terminationDateField = new JTextField();

        // --- Φόρτωση Λιστών ---
        Map<Integer, String> employees = EmployeeDAO.getEmployeeNames();
        for (String name : employees.values()) {
            employeeBox.addItem(name);
        }

        Map<Integer, String> departments = DepartmentDAO.getAllDepartments();
        for (String name : departments.values()) {
            departmentBox.addItem(name);
        }

        // --- UI Logic για Σύμβαση ---
        employmentTypeBox.addActionListener(e -> {
            String selected = (String) employmentTypeBox.getSelectedItem();
            if ("CONTRACT".equals(selected)) {
                contractEndField.setEnabled(true);
                contractSalaryField.setEnabled(true);
                contractEndField.setBackground(Color.WHITE);
                contractSalaryField.setBackground(Color.WHITE);
            } else {
                contractEndField.setEnabled(false);
                contractSalaryField.setEnabled(false);
                contractEndField.setText("");
                contractSalaryField.setText("");
                contractEndField.setBackground(Color.LIGHT_GRAY);
                contractSalaryField.setBackground(Color.LIGHT_GRAY);
            }
        });

        // --- Logic Φόρτωσης Δεδομένων (Load) ---
        Runnable loadSelectedEmployee = () -> {
            int index = employeeBox.getSelectedIndex();
            if (index < 0) return;

            int empId = (int) employees.keySet().toArray()[index];
            EmployeeDAO.EmployeeDetails d = EmployeeDAO.getEmployeeDetails(empId);
            if (d == null) return;

            // Βασικά πεδία
            firstNameField.setText(d.firstName);
            lastNameField.setText(d.lastName);
            maritalBox.setSelectedItem(d.maritalStatus);
            childrenField.setText(String.valueOf(d.numberOfChildren));

            // Τμήμα
            int deptIndex = 0;
            int i = 0;
            for (Integer deptId : departments.keySet()) {
                if (deptId == d.departmentId) {
                    deptIndex = i;
                    break;
                }
                i++;
            }
            departmentBox.setSelectedIndex(deptIndex);

            startDateField.setText(d.startDate == null ? "" : d.startDate.toString());
            addressField.setText(d.address == null ? "" : d.address);
            phoneField.setText(d.phone == null ? "" : d.phone);
            bankAccountField.setText(d.bankAccount == null ? "" : d.bankAccount);
            bankNameField.setText(d.bankName == null ? "" : d.bankName);

            // --- Αποκωδικοποίηση του Category (π.χ. ADMIN_PERMANENT) ---
            if (d.category != null) {
                if (d.category.contains("ADMIN")) {
                    personnelCategoryBox.setSelectedItem("ADMINISTRATIVE");
                } else {
                    personnelCategoryBox.setSelectedItem("TEACHING");
                }

                if (d.category.contains("CONTRACT")) {
                    employmentTypeBox.setSelectedItem("CONTRACT");
                } else {
                    employmentTypeBox.setSelectedItem("PERMANENT");
                }
            }

            // --- Φόρτωση πεδίων Σύμβασης ---
            contractEndField.setText(d.contractEnd == null ? "" : d.contractEnd.toString());
            contractSalaryField.setText(d.contractSalary == null ? "" : String.valueOf(d.contractSalary));

            // Force update UI colors
            if ("CONTRACT".equals(employmentTypeBox.getSelectedItem())) {
                contractEndField.setEnabled(true);
                contractSalaryField.setEnabled(true);
                contractEndField.setBackground(Color.WHITE);
                contractSalaryField.setBackground(Color.WHITE);
            } else {
                contractEndField.setEnabled(false);
                contractSalaryField.setEnabled(false);
                contractEndField.setBackground(Color.LIGHT_GRAY);
                contractSalaryField.setBackground(Color.LIGHT_GRAY);
            }

            activeBox.setSelected(d.active);
            terminationDateField.setText(d.terminationDate == null ? "" : d.terminationDate.toString());
        };

        employeeBox.addActionListener(e -> loadSelectedEmployee.run());

        // --- Logic Αποθήκευσης (Save) ---
        JButton saveBtn = new JButton("Αποθήκευση Αλλαγών");
        saveBtn.addActionListener(e -> {
            try {
                int empIndex = employeeBox.getSelectedIndex();
                if (empIndex < 0) return;
                int empId = (int) employees.keySet().toArray()[empIndex];

                int deptIndex = departmentBox.getSelectedIndex();
                int deptId = (int) departments.keySet().toArray()[deptIndex];

                EmployeeDAO.EmployeeDetails d = new EmployeeDAO.EmployeeDetails();

                // Βασικά
                d.firstName = firstNameField.getText();
                d.lastName = lastNameField.getText();
                d.maritalStatus = maritalBox.getSelectedItem().toString();
                d.numberOfChildren = Integer.parseInt(childrenField.getText());
                d.departmentId = deptId;

                String sdText = startDateField.getText().trim();
                d.startDate = sdText.isEmpty() ? null : LocalDate.parse(sdText);

                d.address = addressField.getText();
                d.phone = phoneField.getText();
                d.bankAccount = bankAccountField.getText();
                d.bankName = bankNameField.getText();

                // --- Κατασκευή Category String ---
                String catSelection = (String) personnelCategoryBox.getSelectedItem();
                String typeSelection = (String) employmentTypeBox.getSelectedItem();

                String dbPrefix = "ADMINISTRATIVE".equals(catSelection) ? "ADMIN" : "TEACH";
                d.category = dbPrefix + "_" + typeSelection;

                // --- Στοιχεία Σύμβασης ---
                if ("CONTRACT".equals(typeSelection)) {
                    String ceText = contractEndField.getText().trim();
                    d.contractEnd = ceText.isEmpty() ? null : LocalDate.parse(ceText);

                    String csText = contractSalaryField.getText().trim();
                    d.contractSalary = csText.isEmpty() ? null : Double.parseDouble(csText);
                } else {
                    d.contractEnd = null;
                    d.contractSalary = null;
                }

                d.active = activeBox.isSelected();
                String tdText = terminationDateField.getText().trim();
                d.terminationDate = tdText.isEmpty() ? null : LocalDate.parse(tdText);

                // Update
                boolean ok = EmployeeDAO.updateEmployeeDetails(empId, d);

                if (ok) {
                    JOptionPane.showMessageDialog(this, "Τα στοιχεία ενημερώθηκαν ✔");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Αποτυχία ενημέρωσης ❌");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Λάθος δεδομένα (ελέγξτε ημερομηνίες και αριθμούς) ❌");
            }
        });

        // --- Προσθήκη στο Panel ---
        panel.add(new JLabel("Επιλογή Υπαλλήλου:"));
        panel.add(employeeBox);
        panel.add(new JSeparator()); panel.add(new JSeparator());

        panel.add(new JLabel("Όνομα:"));
        panel.add(firstNameField);
        panel.add(new JLabel("Επώνυμο:"));
        panel.add(lastNameField);
        panel.add(new JLabel("Οικ. Κατάσταση:"));
        panel.add(maritalBox);
        panel.add(new JLabel("Παιδιά:"));
        panel.add(childrenField);
        panel.add(new JLabel("Τμήμα:"));
        panel.add(departmentBox);
        panel.add(new JLabel("Ημ. Έναρξης:"));
        panel.add(startDateField);
        panel.add(new JLabel("Διεύθυνση:"));
        panel.add(addressField);
        panel.add(new JLabel("Τηλέφωνο:"));
        panel.add(phoneField);
        panel.add(new JLabel("IBAN:"));
        panel.add(bankAccountField);
        panel.add(new JLabel("Τράπεζα:"));
        panel.add(bankNameField);

        panel.add(new JSeparator()); panel.add(new JSeparator());

        panel.add(new JLabel("Κατηγορία:"));
        panel.add(personnelCategoryBox);
        panel.add(new JLabel("Τύπος:"));
        panel.add(employmentTypeBox);

        // Νέα Πεδία
        panel.add(new JLabel("Λήξη Σύμβασης:"));
        panel.add(contractEndField);
        panel.add(new JLabel("Μισθός Σύμβασης:"));
        panel.add(contractSalaryField);

        panel.add(new JSeparator()); panel.add(new JSeparator());

        panel.add(new JLabel("Κατάσταση (Active):"));
        panel.add(activeBox);
        panel.add(new JLabel("Ημ. Απόλυσης:"));
        panel.add(terminationDateField);

        add(panel, BorderLayout.CENTER);
        add(saveBtn, BorderLayout.SOUTH);

        // Φόρτωση του πρώτου (αν υπάρχει)
        if (employeeBox.getItemCount() > 0) {
            employeeBox.setSelectedIndex(0);
            loadSelectedEmployee.run();
        }
    }
}