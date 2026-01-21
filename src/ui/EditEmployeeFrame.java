package ui;

import dao.DepartmentDAO;
import dao.EmployeeDAO;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.Map;

public class EditEmployeeFrame extends JFrame {
    private JTextField contractEndField, salaryField;
    private JComboBox<String> employmentTypeBox;

    public EditEmployeeFrame() {
        setTitle("Επεξεργασία Υπαλλήλου");
        setSize(550, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JComboBox<String> employeeBox = new JComboBox<>();
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

        JComboBox<String> personnelCategoryBox = new JComboBox<>(new String[]{"ADMINISTRATIVE", "TEACHING"});
        employmentTypeBox = new JComboBox<>(new String[]{"PERMANENT", "CONTRACT"});
        employmentTypeBox.setEnabled(false);

        contractEndField = new JTextField();
        salaryField = new JTextField();
        JCheckBox activeBox = new JCheckBox("Ενεργός");
        JTextField terminationDateField = new JTextField();

        Map<Integer, String> employees = EmployeeDAO.getEmployeeNames();
        for (String name : employees.values()) employeeBox.addItem(name);
        Map<Integer, String> departments = DepartmentDAO.getAllDepartments();
        for (String name : departments.values()) departmentBox.addItem(name);

        employeeBox.addActionListener(e -> {
            int index = employeeBox.getSelectedIndex();
            if (index < 0) return;
            int empId = (int) employees.keySet().toArray()[index];
            EmployeeDAO.EmployeeDetails d = EmployeeDAO.getEmployeeDetails(empId);
            if (d == null) return;

            firstNameField.setText(d.firstName); lastNameField.setText(d.lastName);
            maritalBox.setSelectedItem(d.maritalStatus); childrenField.setText(String.valueOf(d.numberOfChildren));

            int i = 0;
            for (Integer deptId : departments.keySet()) {
                if (deptId == d.departmentId) { departmentBox.setSelectedIndex(i); break; }
                i++;
            }
            startDateField.setText(d.startDate != null ? d.startDate.toString() : "");
            addressField.setText(d.address); phoneField.setText(d.phone);
            bankAccountField.setText(d.bankAccount); bankNameField.setText(d.bankName);

            if (d.category.contains("ADMIN")) personnelCategoryBox.setSelectedItem("ADMINISTRATIVE");
            else personnelCategoryBox.setSelectedItem("TEACHING");

            if (d.category.contains("CONTRACT")) {
                employmentTypeBox.setSelectedItem("CONTRACT");
                contractEndField.setEnabled(true); contractEndField.setText(d.contractEnd != null ? d.contractEnd.toString() : "");
            } else {
                employmentTypeBox.setSelectedItem("PERMANENT");
                contractEndField.setEnabled(false); contractEndField.setText("-");
            }
            salaryField.setText(d.salary != null ? d.salary.toString() : "0.0");
            activeBox.setSelected(d.active);
            terminationDateField.setText(d.terminationDate != null ? d.terminationDate.toString() : "");
        });

        JButton saveBtn = new JButton("Αποθήκευση");
        saveBtn.addActionListener(e -> {
            try {
                int empId = (int) employees.keySet().toArray()[employeeBox.getSelectedIndex()];
                int deptId = (int) departments.keySet().toArray()[departmentBox.getSelectedIndex()];
                EmployeeDAO.EmployeeDetails d = new EmployeeDAO.EmployeeDetails();

                d.firstName = firstNameField.getText(); d.lastName = lastNameField.getText();
                d.maritalStatus = maritalBox.getSelectedItem().toString(); d.departmentId = deptId;
                d.address = addressField.getText(); d.phone = phoneField.getText();
                d.bankAccount = bankAccountField.getText(); d.bankName = bankNameField.getText();
                d.active = activeBox.isSelected();
                String td = terminationDateField.getText().trim();
                d.terminationDate = td.isEmpty() ? null : LocalDate.parse(td);

                String prefix = "ADMINISTRATIVE".equals(personnelCategoryBox.getSelectedItem()) ? "ADMIN" : "TEACH";
                d.category = prefix + "_" + employmentTypeBox.getSelectedItem();
                d.salary = Double.parseDouble(salaryField.getText());

                if ("CONTRACT".equals(employmentTypeBox.getSelectedItem())) {
                    String ce = contractEndField.getText().trim();
                    d.contractEnd = ce.isEmpty() ? null : LocalDate.parse(ce);
                }

                if (EmployeeDAO.updateEmployeeDetails(empId, d)) { JOptionPane.showMessageDialog(this, "Ενημερώθηκε!"); dispose(); }
                else JOptionPane.showMessageDialog(this, "Σφάλμα.");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Λάθος δεδομένα."); }
        });

        panel.add(new JLabel("Υπάλληλος:")); panel.add(employeeBox);
        panel.add(new JLabel("Όνομα:")); panel.add(firstNameField);
        panel.add(new JLabel("Επώνυμο:")); panel.add(lastNameField);
        panel.add(new JLabel("Οικ. Κατάσταση:")); panel.add(maritalBox);
        panel.add(new JLabel("Παιδιά (Read-only):")); panel.add(childrenField);
        panel.add(new JLabel("Τμήμα:")); panel.add(departmentBox);
        panel.add(new JLabel("Διεύθυνση:")); panel.add(addressField);
        panel.add(new JLabel("Τηλέφωνο:")); panel.add(phoneField);
        panel.add(new JLabel("IBAN:")); panel.add(bankAccountField);
        panel.add(new JLabel("Τράπεζα:")); panel.add(bankNameField);
        panel.add(new JSeparator()); panel.add(new JSeparator());
        panel.add(new JLabel("Κατηγορία:")); panel.add(personnelCategoryBox);
        panel.add(new JLabel("Τύπος:")); panel.add(employmentTypeBox);
        panel.add(new JLabel("Μισθός:")); panel.add(salaryField);
        panel.add(new JLabel("Λήξη Σύμβασης:")); panel.add(contractEndField);
        panel.add(new JLabel("Ενεργός:")); panel.add(activeBox);
        panel.add(new JLabel("Ημ. Απόλυσης:")); panel.add(terminationDateField);

        add(panel, BorderLayout.CENTER);
        add(saveBtn, BorderLayout.SOUTH);
    }
}