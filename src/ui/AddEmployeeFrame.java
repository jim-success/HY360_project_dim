package ui;

import dao.DepartmentDAO;
import dao.EmployeeDAO;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.Map;

public class AddEmployeeFrame extends JFrame {

    public AddEmployeeFrame() {
        setTitle("Προσθήκη Υπαλλήλου");
        setSize(420, 520);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JComboBox<String> maritalBox = new JComboBox<>(new String[]{"single", "married"});
        JTextField childrenField = new JTextField("0");
        JComboBox<String> departmentBox = new JComboBox<>();
        JTextField startDateField = new JTextField("2024-01-01");
        JTextField addressField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField bankAccountField = new JTextField();
        JTextField bankNameField = new JTextField();

        JComboBox<String> personnelCategoryBox = new JComboBox<>(new String[]{"ADMINISTRATIVE", "TEACHING"});
        JComboBox<String> employmentTypeBox = new JComboBox<>(new String[]{"PERMANENT", "CONTRACT"});

        JTextField salaryField = new JTextField();
        JTextField contractEndDateField = new JTextField("2026-12-31");
        contractEndDateField.setEnabled(false);

        employmentTypeBox.addActionListener(e -> {
            boolean isContract = "CONTRACT".equals(employmentTypeBox.getSelectedItem().toString());
            contractEndDateField.setEnabled(isContract);
            if (!isContract) contractEndDateField.setText("");
            else if (contractEndDateField.getText().trim().isEmpty()) contractEndDateField.setText("2026-12-31");
        });

        Map<Integer, String> departments = DepartmentDAO.getAllDepartments();
        for (String name : departments.values()) {
            departmentBox.addItem(name);
        }

        JButton saveBtn = new JButton("Αποθήκευση");

        panel.add(new JLabel("Όνομα"));
        panel.add(firstNameField);
        panel.add(new JLabel("Επώνυμο"));
        panel.add(lastNameField);
        panel.add(new JLabel("Οικ. Κατάσταση"));
        panel.add(maritalBox);
        panel.add(new JLabel("Παιδιά (πλήθος)"));
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

        panel.add(new JLabel("Μισθός (optional)"));
        panel.add(salaryField);

        panel.add(new JLabel("Λήξη Σύμβασης (YYYY-MM-DD)"));
        panel.add(contractEndDateField);

        add(panel, BorderLayout.CENTER);
        add(saveBtn, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> {
            try {
                int deptIndex = departmentBox.getSelectedIndex();
                int deptId = (int) departments.keySet().toArray()[deptIndex];

                LocalDate startDate = LocalDate.parse(startDateField.getText().trim());

                if (startDate.isBefore(LocalDate.now())) {
                    JOptionPane.showMessageDialog(this, "Η πρόσληψη δεν μπορεί να είναι αναδρομική.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (startDate.getDayOfMonth() != 1) {
                    JOptionPane.showMessageDialog(this, "Η πρόσληψη πρέπει να γίνεται από την 1η ημέρα του μήνα.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String empType = employmentTypeBox.getSelectedItem().toString();
                String pcat = personnelCategoryBox.getSelectedItem().toString();

                String prefix = "ADMINISTRATIVE".equals(pcat) ? "ADMIN" : "TEACH";
                String category = prefix + "_" + empType;

                String contractEnd = contractEndDateField.getText().trim();
                if ("CONTRACT".equals(empType) && contractEnd.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Για συμβασιούχο πρέπει να βάλεις ημερομηνία λήξης σύμβασης.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Double salaryInput = null;
                String salaryText = salaryField.getText().trim();
                if (!salaryText.isEmpty()) salaryInput = Double.parseDouble(salaryText);

                boolean success = EmployeeDAO.insertEmployee(
                        firstNameField.getText(),
                        lastNameField.getText(),
                        maritalBox.getSelectedItem().toString(),
                        Integer.parseInt(childrenField.getText()),
                        deptId,
                        startDateField.getText().trim(),
                        addressField.getText(),
                        phoneField.getText(),
                        bankAccountField.getText(),
                        bankNameField.getText(),
                        category,
                        contractEnd,
                        salaryInput
                );

                if (success) {
                    JOptionPane.showMessageDialog(this, "Ο υπάλληλος προστέθηκε");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Σφάλμα");
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Λάθος δεδομένα", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
