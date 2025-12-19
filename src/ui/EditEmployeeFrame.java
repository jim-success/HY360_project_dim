package ui;

import dao.EmployeeDAO;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class EditEmployeeFrame extends JFrame {

    public EditEmployeeFrame() {
        setTitle("Επεξεργασία Υπαλλήλου");
        setSize(400, 300);
        setLocationRelativeTo(null);

        JComboBox<String> employeeBox = new JComboBox<>();
        JTextField addressField = new JTextField();
        JComboBox<String> maritalBox = new JComboBox<>(new String[]{"single", "married"});
        JTextField childrenField = new JTextField();

        Map<Integer, String> employees = EmployeeDAO.getEmployeeNames();
        for (String name : employees.values()) {
            employeeBox.addItem(name);
        }

        JButton saveBtn = new JButton("Αποθήκευση");

        saveBtn.addActionListener(e -> {
            try {
                int index = employeeBox.getSelectedIndex();
                int empId = (int) employees.keySet().toArray()[index];

                String address = addressField.getText();
                String maritalStatus = maritalBox.getSelectedItem().toString();
                int children = Integer.parseInt(childrenField.getText());

                boolean ok = EmployeeDAO.updateEmployee(
                        empId, address, maritalStatus, children
                );

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

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Υπάλληλος"));
        panel.add(employeeBox);
        panel.add(new JLabel("Διεύθυνση"));
        panel.add(addressField);
        panel.add(new JLabel("Οικογενειακή Κατάσταση"));
        panel.add(maritalBox);
        panel.add(new JLabel("Αριθμός Παιδιών"));
        panel.add(childrenField);
        panel.add(saveBtn);

        add(panel);
    }
}
