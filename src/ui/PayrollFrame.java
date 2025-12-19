package ui;

import dao.EmployeeDAO;
import dao.PayrollDAO;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class PayrollFrame extends JFrame {

    private double calculatedAmount = -1; // -1 = δεν έχει υπολογιστεί

    public PayrollFrame() {
        setTitle("Καταβολή Μισθοδοσίας");
        setSize(350, 220);
        setLocationRelativeTo(null);

        JComboBox<String> employeeBox = new JComboBox<>();
        JLabel amountLabel = new JLabel("Ποσό: -");

        Map<Integer, String> employees = EmployeeDAO.getEmployeeNames();
        Integer[] employeeIds = employees.keySet().toArray(new Integer[0]);

        for (String name : employees.values()) {
            employeeBox.addItem(name);
        }

        JButton calculateBtn = new JButton("Υπολογισμός");
        JButton payBtn = new JButton("Καταβολή");

        // Αν αλλάξει υπάλληλος → reset
        employeeBox.addActionListener(e -> {
            calculatedAmount = -1;
            amountLabel.setText("Ποσό: -");
        });

        // ΥΠΟΛΟΓΙΣΜΟΣ ΜΙΣΘΟΥ
        calculateBtn.addActionListener(e -> {
            int index = employeeBox.getSelectedIndex();
            int empId = employeeIds[index];

            calculatedAmount = PayrollDAO.calculateSalary(empId);
            amountLabel.setText("Ποσό: " + calculatedAmount + " €");
        });

        // ΚΑΤΑΒΟΛΗ ΜΙΣΘΟΔΟΣΙΑΣ
        payBtn.addActionListener(e -> {
            if (calculatedAmount < 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Πρέπει πρώτα να γίνει υπολογισμός μισθού ❗"
                );
                return;
            }

            int index = employeeBox.getSelectedIndex();
            int empId = employeeIds[index];

            boolean success = PayrollDAO.insertPayment(empId, calculatedAmount);

            if (success) {
                JOptionPane.showMessageDialog(
                        this,
                        "Η μισθοδοσία καταχωρήθηκε επιτυχώς ✔"
                );
                dispose();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Σφάλμα κατά την καταβολή ❌"
                );
            }
        });

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Υπάλληλος"));
        panel.add(employeeBox);
        panel.add(amountLabel);
        panel.add(calculateBtn);
        panel.add(payBtn);

        add(panel);
    }
}
