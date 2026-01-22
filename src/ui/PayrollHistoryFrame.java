package ui;

import dao.EmployeeDAO;
import dao.PayrollDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import java.util.Vector;

public class PayrollHistoryFrame extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    public PayrollHistoryFrame() {
        setTitle("Ιστορικό Μισθοδοσίας Υπαλλήλου"); // Άλλαξε λίγο ο τίτλος
        setSize(600, 400);
        setLocationRelativeTo(null);
        // Χρησιμοποιούμε DISPOSE για να μην κλείνει όλη η εφαρμογή
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 1. Πάνελ Επιλογής Υπαλλήλου (Πάνω μέρος)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> employeeBox = new JComboBox<>();
        JButton showBtn = new JButton("Εμφάνιση Ιστορικού");

        // Φόρτωση ονομάτων από τη βάση
        Map<Integer, String> employees = EmployeeDAO.getEmployeeNames();
        Integer[] employeeIds = employees.keySet().toArray(new Integer[0]);

        for (String name : employees.values()) {
            employeeBox.addItem(name);
        }

        topPanel.add(new JLabel("Επιλογή Υπαλλήλου:"));
        topPanel.add(employeeBox);
        topPanel.add(showBtn);

        // 2. Πίνακας Αποτελεσμάτων (Κέντρο)
        // Φτιάχνουμε ένα κενό μοντέλο αρχικά
        model = new DefaultTableModel(PayrollDAO.getPayrollHistoryColumns(), 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        // 3. Λειτουργία Κουμπιού
        showBtn.addActionListener(e -> {
            int index = employeeBox.getSelectedIndex();
            if (index >= 0) {
                int empId = employeeIds[index];

                // Καλούμε τη ΝΕΑ μέθοδο που φτιάξαμε στο DAO
                Vector<Vector<Object>> data = PayrollDAO.getPayrollHistoryByEmployee(empId);

                // Ενημέρωση του πίνακα
                model.setDataVector(data, PayrollDAO.getPayrollHistoryColumns());
            }
        });

        // Στήσιμο παραθύρου
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
}