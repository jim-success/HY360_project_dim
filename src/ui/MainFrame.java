package ui;

import db.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Payroll Application (3NF Version)");
        setSize(420, 430);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel statusLabel = new JLabel("Έλεγχος σύνδεσης...", SwingConstants.CENTER);

        JButton viewEmployeesBtn = new JButton("Προβολή Υπαλλήλων");
        JButton addEmployeeBtn = new JButton("Προσθήκη Υπαλλήλου");
        JButton payrollBtn = new JButton("Καταβολή Μισθοδοσίας");
        JButton payrollHistoryBtn = new JButton("Ιστορικό Μισθοδοσίας");
        JButton reportDeptBtn = new JButton("Μισθοδοσία ανά Τμήμα");
        JButton editEmployeeBtn = new JButton("Επεξεργασία Υπαλλήλου");
        JButton terminateEmployeeBtn = new JButton("Απόλυση / Συνταξιοδότηση");
        JButton salaryStatsBtn = new JButton("Στατιστικά Μισθών");
        JButton payrollStatusByCategoryBtn = new JButton("Κατάσταση Μισθοδοσίας");
        JButton sqlConsoleBtn = new JButton("SQL Console");
        JButton salaryPolicyBtn = new JButton("Μεταβολή Βασικών Μισθών & Επιδομάτων");
        salaryPolicyBtn.addActionListener(e -> new SalaryPolicyFrame().setVisible(true));

        viewEmployeesBtn.addActionListener(e -> new EmployeesFrame().setVisible(true));
        addEmployeeBtn.addActionListener(e -> new AddEmployeeFrame().setVisible(true));
        payrollBtn.addActionListener(e -> new PayrollFrame().setVisible(true));
        payrollHistoryBtn.addActionListener(e -> new PayrollHistoryFrame().setVisible(true));
        reportDeptBtn.addActionListener(e -> new PayrollByDepartmentFrame().setVisible(true));
        editEmployeeBtn.addActionListener(e -> new EditEmployeeFrame().setVisible(true));
        terminateEmployeeBtn.addActionListener(e -> new TerminateEmployeeFrame().setVisible(true));
        salaryStatsBtn.addActionListener(e -> new SalaryStatsByCategoryFrame().setVisible(true));
        payrollStatusByCategoryBtn.addActionListener(e -> new PayrollStatusByCategoryFrame().setVisible(true));
        sqlConsoleBtn.addActionListener(e -> new SQLConsoleFrame().setVisible(true));

        JPanel buttonsPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        buttonsPanel.add(viewEmployeesBtn);
        buttonsPanel.add(addEmployeeBtn);
        buttonsPanel.add(editEmployeeBtn);
        buttonsPanel.add(payrollBtn);
        buttonsPanel.add(payrollHistoryBtn);
        buttonsPanel.add(reportDeptBtn);
        buttonsPanel.add(salaryStatsBtn);
        buttonsPanel.add(payrollStatusByCategoryBtn);
        // Το SalaryPolicy αφαιρέθηκε λόγω 3NF
        buttonsPanel.add(sqlConsoleBtn);
        buttonsPanel.add(salaryPolicyBtn);

        buttonsPanel.add(terminateEmployeeBtn);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        add(buttonsPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        try (Connection conn = DBConnection.getConnection()) {
            statusLabel.setText("Σύνδεση με βάση 'testdatabase': ΕΠΙΤΥΧΗΣ ✅");
        } catch (Exception e) {
            statusLabel.setText("Αποτυχία σύνδεσης ❌");
        }
    }
}