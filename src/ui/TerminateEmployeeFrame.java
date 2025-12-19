package ui;

import dao.EmployeeDAO;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class TerminateEmployeeFrame extends JFrame {

    public TerminateEmployeeFrame() {
        setTitle("Απόλυση / Συνταξιοδότηση");
        setSize(350, 200);
        setLocationRelativeTo(null);

        JComboBox<String> employeeBox = new JComboBox<>();
        Map<Integer, String> employees = EmployeeDAO.getEmployeeNames();

        for (String name : employees.values()) {
            employeeBox.addItem(name);
        }

        JButton terminateBtn = new JButton("Ολοκλήρωση");

        terminateBtn.addActionListener(e -> {

            int index = employeeBox.getSelectedIndex();
            int employeeId = (int) employees.keySet().toArray()[index];

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Η απόλυση / συνταξιοδότηση ισχύει από το τέλος του μήνα.\n"
                            + "Ο υπάλληλος θα πληρωθεί κανονικά.\n\nΣυνέχεια;",
                    "Επιβεβαίωση",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {

                if (EmployeeDAO.markForTermination(employeeId)) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Ο υπάλληλος σημειώθηκε προς απόλυση ✔\n"
                                    + "Θα απενεργοποιηθεί μετά την πληρωμή."
                    );

                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Σφάλμα ❌");
                }
            }
        });

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Υπάλληλος"));
        panel.add(employeeBox);
        panel.add(terminateBtn);

        add(panel);
    }
}
