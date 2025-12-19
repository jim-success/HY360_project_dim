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
        setSize(400, 450);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));

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
        panel.add(new JLabel("Παιδιά"));
        panel.add(childrenField);
        panel.add(new JLabel("Τμήμα"));
        panel.add(departmentBox);
        panel.add(new JLabel("Ημ. Έναρξης"));
        panel.add(startDateField);
        panel.add(new JLabel("Διεύθυνση"));
        panel.add(addressField);
        panel.add(new JLabel("Τηλέφωνο"));
        panel.add(phoneField);
        panel.add(new JLabel("IBAN"));
        panel.add(bankAccountField);
        panel.add(new JLabel("Τράπεζα"));
        panel.add(bankNameField);

        add(panel, BorderLayout.CENTER);
        add(saveBtn, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> {
            int deptIndex = departmentBox.getSelectedIndex();
            int deptId = (int) departments.keySet().toArray()[deptIndex];

            LocalDate startDate = LocalDate.parse(startDateField.getText());


            // ❌ όχι αναδρομικά
            if (startDate.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(
                        this,
                        "Η πρόσληψη δεν μπορεί να είναι αναδρομική.",
                        "Σφάλμα",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }


            // ❌ όχι αν δεν είναι 1η του μήνα
            if (startDate.getDayOfMonth() != 1) {
                JOptionPane.showMessageDialog(
                        this,
                        "Η πρόσληψη πρέπει να γίνεται από την 1η ημέρα του μήνα.",
                        "Σφάλμα",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }


            boolean success = EmployeeDAO.insertEmployee(
                    firstNameField.getText(),
                    lastNameField.getText(),
                    maritalBox.getSelectedItem().toString(),
                    Integer.parseInt(childrenField.getText()),
                    deptId,
                    startDateField.getText(),
                    addressField.getText(),
                    phoneField.getText(),
                    bankAccountField.getText(),
                    bankNameField.getText()
            );

            if (success) {
                JOptionPane.showMessageDialog(this, "Ο υπάλληλος προστέθηκε ✔");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Σφάλμα ❌");
            }
        });
    }
}
