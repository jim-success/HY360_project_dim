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
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Κλείνει μόνο το παράθυρο, όχι την εφαρμογή

        // Ορισμός Στηλών
        String[] columns = {"Κατηγορία", "Ελάχιστος Μισθός", "Μέγιστος Μισθός", "Μέσος Όρος"};

        // Δημιουργία Μοντέλου Πίνακα
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);

        // Φόρτωση Δεδομένων
        loadData(model);

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void loadData(DefaultTableModel model) {
        // Καλούμε τη μέθοδο από το StatisticsDAO
        List<StatisticsDAO.CategorySalaryStats> stats = StatisticsDAO.getSalaryStatistics();

        for (StatisticsDAO.CategorySalaryStats s : stats) {
            model.addRow(new Object[]{
                    s.category,
                    String.format("%.2f €", s.minSalary), // Μορφοποίηση με 2 δεκαδικά και €
                    String.format("%.2f €", s.maxSalary),
                    String.format("%.2f €", s.avgSalary)
            });
        }
    }
}