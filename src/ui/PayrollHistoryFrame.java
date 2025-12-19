package ui;

import dao.PayrollDAO;

import javax.swing.*;
import java.awt.*;

public class PayrollHistoryFrame extends JFrame {

    public PayrollHistoryFrame() {
        setTitle("Ιστορικό Μισθοδοσίας");
        setSize(500, 300);
        setLocationRelativeTo(null);

        JTable table = new JTable(
                PayrollDAO.getPayrollHistory(),
                PayrollDAO.getPayrollHistoryColumns()
        );

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }
}
