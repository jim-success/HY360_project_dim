package ui;

import dao.StatisticsDAO;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class SalaryIncreaseFrame extends JFrame {

    public SalaryIncreaseFrame() {
        setTitle("Μέση Αύξηση Μισθών ανά Περίοδο");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));




           int currentYear = LocalDate.now().getYear();
        Integer[] years = new Integer[10];
        for (int i = 0; i < 10; i++) {
            years[i] = currentYear - 5 + i;
        }

        JComboBox<Integer> fromYearBox = new JComboBox<>(years);
        JComboBox<Integer> toYearBox = new JComboBox<>(years);

        fromYearBox.setSelectedItem(currentYear - 1);
        toYearBox.setSelectedItem(currentYear);

        topPanel.add(new JLabel("Από Έτος:"));
        topPanel.add(fromYearBox);
        topPanel.add(new JLabel("Προς Έτος:"));
        topPanel.add(toYearBox);
        JButton calculateBtn = new JButton("Υπολογισμός Διαφοράς");
        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        resultArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));





        calculateBtn.addActionListener(e -> {
            int year1 = (int) fromYearBox.getSelectedItem();
            int year2 = (int) toYearBox.getSelectedItem();

            if (year1 == year2) {
                resultArea.setText("Παρακαλώ επιλέξτε διαφορετικά έτη.");
                return;
            }

            double avg1 = StatisticsDAO.getAverageSalaryForYear(year1);
            double avg2 = StatisticsDAO.getAverageSalaryForYear(year2);

            if (avg1 == 0 && avg2 == 0) {
                resultArea.setText("Δεν βρέθηκαν δεδομένα μισθοδοσίας\nγια κανένα από τα δύο έτη.");
                return;
            }

            double diff = avg2 - avg1;
            double percent = (avg1 > 0) ? (diff / avg1) * 100 : 0.0;

            String message = String.format(
                    "Μέσος Μισθός %d: %.2f €\n" +
                            "Μέσος Μισθός %d: %.2f €\n\n" +
                            "Διαφορά: %s%.2f €\n" +
                            "Ποσοστό: %s%.2f%%",
                    year1, avg1,
                    year2, avg2,
                    (diff > 0 ? "+" : ""), diff,
                    (percent > 0 ? "+" : ""), percent
            );

            resultArea.setText(message);
        });

        add(topPanel, BorderLayout.NORTH);
          add(new JScrollPane(resultArea), BorderLayout.CENTER);
        add(calculateBtn, BorderLayout.SOUTH);
    }
}