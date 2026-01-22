package ui;

import dao.StatisticsDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SalaryStatsByCategoryFrame extends JFrame {
    public SalaryStatsByCategoryFrame() {
        setTitle("Στατιστικά Μισθών ανά Κατηγορία Προσωπικού");
        setSize(700, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] columns = {"Κατηγορία", "Ελάχιστος Μισθός", "Μέγιστος Μισθός", "Μέσος Όρος"};

        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);

        loadData(model);

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void loadData(DefaultTableModel model) {
        List<StatisticsDAO.CategorySalaryStats> stats = StatisticsDAO.getSalaryStatistics();

        for (StatisticsDAO.CategorySalaryStats s : stats) {
            model.addRow(new Object[]{
                    s.category,
                    String.format("%.2f ευρώ", s.minSalary),
                    String.format("%.2f ευρώ", s.maxSalary),
                    String.format("%.2f ευρώ", s.avgSalary)
            });
        }
    }
}