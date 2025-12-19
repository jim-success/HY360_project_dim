package ui;

import dao.PayrollDAO;

import javax.swing.*;
import java.awt.*;

public class SalaryStatsByDepartmentFrame extends JFrame {

    public SalaryStatsByDepartmentFrame() {
        setTitle("Στατιστικά Μισθών ανά Τμήμα");
        setSize(600, 300);
        setLocationRelativeTo(null);

        JTable table = new JTable(
                PayrollDAO.getSalaryStatsByDepartmentJava(),
                PayrollDAO.getSalaryStatsByDepartmentColumns()
        );

        add(new JScrollPane(table), BorderLayout.CENTER);
    }
}
