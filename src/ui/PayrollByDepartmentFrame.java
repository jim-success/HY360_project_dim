package ui;

import dao.PayrollDAO;

import javax.swing.*;
import java.awt.*;

public class PayrollByDepartmentFrame extends JFrame {

    public PayrollByDepartmentFrame() {
        setTitle("Μισθοδοσία ανά Τμήμα");
        setSize(500, 300);
        setLocationRelativeTo(null);

        JTable table = new JTable(
                PayrollDAO.getPayrollByDepartment(),
                PayrollDAO.getPayrollByDepartmentColumns()
        );

        add(new JScrollPane(table), BorderLayout.CENTER);
    }
}
