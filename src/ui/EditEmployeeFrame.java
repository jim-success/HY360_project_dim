package ui;

import dao.DepartmentDAO;
import dao.EmployeeDAO;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.Map;

public class EditEmployeeFrame extends JFrame {

    public EditEmployeeFrame() {
        setTitle("Επεξεργασία Υπαλλήλου");
        setSize(520, 580);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));

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
        JComboBox<String> employmentTypeBox = new JComboBox<>(new String[]{"PERMANENT", "CONTRACT"});

        JCheckBox activeBox = new JCheckBox("Ενεργός");
        JTextField terminationDateField = new JTextField();

        Map<Integer, String> employees = EmployeeDAO.getEmployeeNames();
        for (String name : employees.values()) {
            employeeBox.addItem(name);
        }

        Map<Integer, String> departments = DepartmentDAO.getAllDepartments();
        for (String name : departments.values()) {
            departmentBox.addItem(name);
        }

        JButton saveBtn = new JButton("Αποθήκευση");

        Runnable loadSelectedEmployee = () -> {
            int index = employeeBox.getSelectedIndex();
            if (index < 0) return;

            int empId = (int) employees.keySet().toArray()[index];
            EmployeeDAO.EmployeeDetails d = EmployeeDAO.getEmployeeDetails(empId);
            if (d == null) return;

            firstNameField.setText(d.firstName);
            lastNameField.setText(d.lastName);
            maritalBox.setSelectedItem(d.maritalStatus);
            childrenField.setText(String.valueOf(d.numberOfChildren));

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

            if (d.personnelCategory != null) personnelCategoryBox.setSelectedItem(d.personnelCategory);
            else personnelCategoryBox.setSelectedIndex(0);

            if (d.employmentType != null) employmentTypeBox.setSelectedItem(d.employmentType);
            else employmentTypeBox.setSelectedIndex(0);

            activeBox.setSelected(d.active);
            terminationDateField.setText(d.terminationDate == null ? "" : d.terminationDate.toString());
        };

        employeeBox.addActionListener(e -> loadSelectedEmployee.run());

        saveBtn.addActionListener(e -> {
            try {
                int empIndex = employeeBox.getSelectedIndex();
                if (empIndex < 0) return;

                int empId = (int) employees.keySet().toArray()[empIndex];

                int deptIndex = departmentBox.getSelectedIndex();
                int deptId = (int) departments.keySet().toArray()[deptIndex];

                EmployeeDAO.EmployeeDetails d = new EmployeeDAO.EmployeeDetails();
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

                d.personnelCategory = personnelCategoryBox.getSelectedItem().toString();
                d.employmentType = employmentTypeBox.getSelectedItem().toString();

                d.active = activeBox.isSelected();

                String tdText = terminationDateField.getText().trim();
                d.terminationDate = tdText.isEmpty() ? null : LocalDate.parse(tdText);

                boolean ok = EmployeeDAO.updateEmployeeDetails(empId, d);

                if (ok) {
                    JOptionPane.showMessageDialog(this, "Τα στοιχεία ενημερώθηκαν ✔");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Αποτυχία ενημέρωσης ❌");
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Λάθος δεδομένα ❌");
            }
        });

        panel.add(new JLabel("Υπάλληλος"));
        panel.add(employeeBox);

        panel.add(new JLabel("Όνομα"));
        panel.add(firstNameField);

        panel.add(new JLabel("Επώνυμο"));
        panel.add(lastNameField);

        panel.add(new JLabel("Οικογενειακή Κατάσταση"));
        panel.add(maritalBox);

        panel.add(new JLabel("Αριθμός Παιδιών"));
        panel.add(childrenField);

        panel.add(new JLabel("Τμήμα"));
        panel.add(departmentBox);

        panel.add(new JLabel("Ημ. Έναρξης (YYYY-MM-DD)"));
        panel.add(startDateField);

        panel.add(new JLabel("Διεύθυνση"));
        panel.add(addressField);

        panel.add(new JLabel("Τηλέφωνο"));
        panel.add(phoneField);

        panel.add(new JLabel("IBAN"));
        panel.add(bankAccountField);

        panel.add(new JLabel("Τράπεζα"));
        panel.add(bankNameField);

        panel.add(new JLabel("Κατηγορία Προσωπικού"));
        panel.add(personnelCategoryBox);

        panel.add(new JLabel("Τύπος Εργασίας"));
        panel.add(employmentTypeBox);

        panel.add(new JLabel("Κατάσταση"));
        panel.add(activeBox);

        panel.add(new JLabel("Ημ. Απόλυσης (YYYY-MM-DD)"));
        panel.add(terminationDateField);

        add(panel, BorderLayout.CENTER);
        add(saveBtn, BorderLayout.SOUTH);

        if (employeeBox.getItemCount() > 0) {
            employeeBox.setSelectedIndex(0);
            loadSelectedEmployee.run();
        }
    }
}
