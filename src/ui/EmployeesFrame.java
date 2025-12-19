package ui;

import dao.EmployeeDAO;

import javax.swing.*;
import java.awt.*;

public class EmployeesFrame extends JFrame {

    public EmployeesFrame() {
        setTitle("Λίστα Υπαλλήλων");
        setSize(700, 300);
        setLocationRelativeTo(null);

        JTable table = new JTable(
                EmployeeDAO.getAllEmployees(),
                EmployeeDAO.getColumnNames()
        );


        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }
}
