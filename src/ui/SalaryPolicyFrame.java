package ui;

import dao.SalaryPolicyDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SalaryPolicyFrame extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    public SalaryPolicyFrame() {
        setTitle("Μεταβολή Βασικών Μισθών και Επιδομάτων");
        setSize(900, 320);
        setLocationRelativeTo(null);

        model = new DefaultTableModel(new Object[]{"Κατηγορία", "Βασικός Μισθός", "Research Allowance", "Library Allowance"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                if (col == 0) return false;

                String category = getValueAt(row, 0).toString();

                if (col == 2) return "TEACH_PERMANENT".equals(category);
                if (col == 3) return "TEACH_CONTRACT".equals(category);

                return col == 1;
            }
        };

        table = new JTable(model);

        JButton refreshBtn = new JButton("Ανανέωση");
        JButton saveBtn = new JButton("Αποθήκευση");

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(refreshBtn);
        bottom.add(saveBtn);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> load());
        saveBtn.addActionListener(e -> save());

        load();
    }

    private void load() {
        model.setRowCount(0);

        for (SalaryPolicyDAO.PolicyRow r : SalaryPolicyDAO.getCurrentPolicyRows()) {
            model.addRow(new Object[]{
                    r.category,
                    r.baseSalary,
                    r.researchAllowance,
                    r.libraryAllowance
            });
        }
    }

    private void save() {
        try {
            if (table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }

            List<SalaryPolicyDAO.PolicyRow> rows = new ArrayList<>();

            for (int i = 0; i < model.getRowCount(); i++) {
                SalaryPolicyDAO.PolicyRow r = new SalaryPolicyDAO.PolicyRow();
                r.category = model.getValueAt(i, 0).toString();

                r.baseSalary = parseBD(model.getValueAt(i, 1));
                r.researchAllowance = parseBD(model.getValueAt(i, 2));
                r.libraryAllowance = parseBD(model.getValueAt(i, 3));

                rows.add(r);
            }
            SalaryPolicyDAO.updatePoliciesNoDecrease(rows);

            JOptionPane.showMessageDialog(this, "Οι αλλαγές αποθηκεύτηκαν");
            load();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Σφάλμα", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BigDecimal parseBD(Object o) {
        if (o == null) return BigDecimal.ZERO;
        String s = o.toString().trim();
        if (s.isEmpty()) return BigDecimal.ZERO;
        return new BigDecimal(s);
    }
}
