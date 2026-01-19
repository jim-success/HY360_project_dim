package ui;

import dao.PayrollDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class SalaryPolicyFrame extends JFrame {

    private JTable table;
    private DefaultTableModel model;

    // policy_id -> old values (base, per_child, spouse, research, library)
    private final Map<Integer, BigDecimal[]> original = new HashMap<>();

    public SalaryPolicyFrame() {
        setTitle("Μεταβολή Βασικών Μισθών και Επιδομάτων");
        setSize(900, 320);
        setLocationRelativeTo(null);

        Vector<String> cols = PayrollDAO.getSalaryPolicyColumns();
        Vector<Vector<Object>> rows = PayrollDAO.getSalaryPolicyRows();

        model = new DefaultTableModel(rows, cols) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 0: policy_id, 1: category (μη editable)
                return column >= 2;
            }
        };

        table = new JTable(model);
        loadOriginalFromModel();

        JButton saveBtn = new JButton("Αποθήκευση");
        JButton refreshBtn = new JButton("Ανανέωση");

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(refreshBtn);
        bottom.add(saveBtn);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> refresh());

        saveBtn.addActionListener(e -> {
            try {
                Map<Integer, PayrollDAO.SalaryPolicyUpdate> updates = new HashMap<>();

                for (int r = 0; r < model.getRowCount(); r++) {
                    int policyId = Integer.parseInt(model.getValueAt(r, 0).toString());
                    String category = model.getValueAt(r, 1).toString();

                    BigDecimal base = parseBD(model.getValueAt(r, 2));
                    BigDecimal perChild = parseBD(model.getValueAt(r, 3));
                    BigDecimal spouse = parseBD(model.getValueAt(r, 4));
                    BigDecimal research = parseBD(model.getValueAt(r, 5));
                    BigDecimal library = parseBD(model.getValueAt(r, 6));

                    // client-side no-decrease check
                    BigDecimal[] old = original.get(policyId);
                    if (old != null) {
                        if (base.compareTo(old[0]) < 0) throw new IllegalArgumentException("Μείωση base_salary για " + category);
                        if (perChild.compareTo(old[1]) < 0) throw new IllegalArgumentException("Μείωση allowance_per_child για " + category);
                        if (spouse.compareTo(old[2]) < 0) throw new IllegalArgumentException("Μείωση allowance_spouse για " + category);
                        if (research.compareTo(old[3]) < 0) throw new IllegalArgumentException("Μείωση research_allowance για " + category);
                        if (library.compareTo(old[4]) < 0) throw new IllegalArgumentException("Μείωση library_allowance για " + category);
                    }

                    PayrollDAO.SalaryPolicyUpdate u = new PayrollDAO.SalaryPolicyUpdate();
                    u.policyId = policyId;
                    u.category = category;
                    u.baseSalary = base;
                    u.allowancePerChild = perChild;
                    u.allowanceSpouse = spouse;
                    u.researchAllowance = research;
                    u.libraryAllowance = library;

                    updates.put(policyId, u);
                }

                PayrollDAO.updateSalaryPoliciesNoDecrease(updates);

                JOptionPane.showMessageDialog(this, "Οι αλλαγές αποθηκεύτηκαν ✔");
                refresh();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void refresh() {
        Vector<Vector<Object>> rows = PayrollDAO.getSalaryPolicyRows();
        model.setRowCount(0);
        for (Vector<Object> r : rows) model.addRow(r);
        original.clear();
        loadOriginalFromModel();
    }

    private void loadOriginalFromModel() {
        for (int r = 0; r < model.getRowCount(); r++) {
            int policyId = Integer.parseInt(model.getValueAt(r, 0).toString());

            BigDecimal base = parseBD(model.getValueAt(r, 2));
            BigDecimal perChild = parseBD(model.getValueAt(r, 3));
            BigDecimal spouse = parseBD(model.getValueAt(r, 4));
            BigDecimal research = parseBD(model.getValueAt(r, 5));
            BigDecimal library = parseBD(model.getValueAt(r, 6));

            original.put(policyId, new BigDecimal[]{base, perChild, spouse, research, library});
        }
    }

    private BigDecimal parseBD(Object o) {
        if (o == null) return BigDecimal.ZERO;
        String s = o.toString().trim();
        if (s.isEmpty()) return BigDecimal.ZERO;
        return new BigDecimal(s);
    }
}
