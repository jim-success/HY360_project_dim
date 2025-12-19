package ui;

import db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Payroll Application");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel statusLabel = new JLabel("Έλεγχος σύνδεσης...", SwingConstants.CENTER);

        JButton viewEmployeesBtn = new JButton("Προβολή Υπαλλήλων");
        JButton addEmployeeBtn = new JButton("Προσθήκη Υπαλλήλου");
        JButton payrollBtn = new JButton("Καταβολή Μισθοδοσίας");
        JButton payrollHistoryBtn = new JButton("Ιστορικό Μισθοδοσίας");
        JButton reportDeptBtn = new JButton("Μισθοδοσία ανά Τμήμα");
        JButton editEmployeeBtn = new JButton("Επεξεργασία Υπαλλήλου");
        JButton terminateEmployeeBtn = new JButton("Απόλυση / Συνταξιοδότηση");
        JButton salaryStatsBtn = new JButton("Στατιστικά Μισθών (Min / Max / Avg)");


        salaryStatsBtn.addActionListener(e -> {
            new SalaryStatsByDepartmentFrame().setVisible(true);
        });


        terminateEmployeeBtn.addActionListener(e -> {
            new TerminateEmployeeFrame().setVisible(true);
        });


        editEmployeeBtn.addActionListener(e -> {
            new EditEmployeeFrame().setVisible(true);
        });



        reportDeptBtn.addActionListener(e -> {
            new PayrollByDepartmentFrame().setVisible(true);
        });


        payrollHistoryBtn.addActionListener(e -> {
            new PayrollHistoryFrame().setVisible(true);
        });

        viewEmployeesBtn.addActionListener(e -> {
            new EmployeesFrame().setVisible(true);
        });

        addEmployeeBtn.addActionListener(e -> {
            new AddEmployeeFrame().setVisible(true);
        });

        payrollBtn.addActionListener(e -> {
            new PayrollFrame().setVisible(true);
        });

        JPanel panel = new JPanel(new GridLayout(9, 1, 10, 10));

        panel.add(viewEmployeesBtn);
        panel.add(addEmployeeBtn);
        panel.add(editEmployeeBtn);
        panel.add(payrollBtn);
        panel.add(payrollHistoryBtn);
        panel.add(reportDeptBtn);
        panel.add(salaryStatsBtn);        // ⬅️ ΝΕΟ ΚΟΥΜΠΙ
        panel.add(terminateEmployeeBtn);
        panel.add(statusLabel);





        add(panel);

        try (Connection conn = DBConnection.getConnection()) {
            statusLabel.setText("Σύνδεση με τη βάση: ΕΠΙΤΥΧΗΣ ✅");
        } catch (Exception e) {
            statusLabel.setText("Αποτυχία σύνδεσης ❌");
            e.printStackTrace();
        }
    }


}
