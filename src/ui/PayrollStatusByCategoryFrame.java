package ui;

import dao.StatisticsDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class PayrollStatusByCategoryFrame extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    public PayrollStatusByCategoryFrame() {
        setTitle("Συνολικό ύψος μισθοδοσίας ανά κατηγορία προσωπικού");
        setSize(820, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        LocalDate now = LocalDate.now();

        JComboBox<Integer> yearBox = new JComboBox<>();
        int currentYear = now.getYear();
        for (int y = currentYear - 5; y <= currentYear + 1; y++) yearBox.addItem(y);
        yearBox.setSelectedItem(currentYear);

        JComboBox<Integer> monthBox = new JComboBox<>();
        for (int m = 1; m <= 12; m++) monthBox.addItem(m);
        monthBox.setSelectedItem(now.getMonthValue());

        JButton loadBtn = new JButton("Εμφάνιση");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Έτος:"));
        top.add(yearBox);
        top.add(new JLabel("Μήνας:"));
        top.add(monthBox);
        top.add(loadBtn);

        add(top, BorderLayout.NORTH);
        String[] columns = {"Κατηγορία", "Αριθμός Υπαλλήλων", "Συνολικό Κόστος"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);

        add(new JScrollPane(table), BorderLayout.CENTER);

        loadBtn.addActionListener(e -> {
            int year = (int) yearBox.getSelectedItem();
            int month = (int) monthBox.getSelectedItem();
            updateTable(year, month);
        });

        updateTable(currentYear, now.getMonthValue());
    }

    private void updateTable(int year, int month) {
        model.setRowCount(0);

        List<StatisticsDAO.CategoryTotalStats> stats = StatisticsDAO.getPayrollStatusByCategory(year, month);

        if (stats.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Δεν βρέθηκαν πληρωμές για " + month + "/" + year,
                    "Πληροφορία", JOptionPane.INFORMATION_MESSAGE);
        }

        for (StatisticsDAO.CategoryTotalStats s : stats) {
            model.addRow(new Object[]{
                    s.category,
                    s.employeeCount,
                    String.format("%.2f ευρώ", s.totalCost)
            });
        }
    }
}