package ui;

import dao.PayrollListDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PayrollListByCategoryFrame extends JFrame {
    private final JTable table = new JTable();

    public PayrollListByCategoryFrame() {
        setTitle("Κατάσταση Μισθοδοσίας: Λίστα Υπαλλήλων ανά Κατηγορία");
        setSize(700, 420);
        setLocationRelativeTo(null);

        JComboBox<String> categoryBox = new JComboBox<>(new String[]{
                "ADMIN_PERMANENT",
                "TEACH_PERMANENT",
                "ADMIN_CONTRACT",
                "TEACH_CONTRACT"
        });

        JButton showBtn = new JButton("Εμφάνιση");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Κατηγορία:"));
        top.add(categoryBox);
        top.add(showBtn);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable load = () -> {
            String category = categoryBox.getSelectedItem().toString();

            table.setModel(new DefaultTableModel(
                    PayrollListDAO.getEmployeesSalaryBreakdownByExactCategory(category),
                    PayrollListDAO.getColumns()
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            });
        };




        showBtn.addActionListener(e -> load.run());

        load.run();
    }
}
