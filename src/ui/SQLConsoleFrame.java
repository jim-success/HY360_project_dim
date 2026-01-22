package ui;

import db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class SQLConsoleFrame extends JFrame {
    private final JTextArea queryArea = new JTextArea(6, 80);
    private final JTable table = new JTable();
    private final JLabel statusLabel = new JLabel(" ");

    public SQLConsoleFrame() {
        setTitle("SQL Console");
        setSize(900, 550);
        setLocationRelativeTo(null);

        queryArea.setText("SELECT * FROM employee LIMIT 50;");
        queryArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JButton execBtn = new JButton("Execute (SELECT only)");
        JButton clearBtn = new JButton("Clear");
        JButton sampleBtn = new JButton("Sample");

        JPanel topBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBtns.add(execBtn);
        topBtns.add(clearBtn);
        topBtns.add(sampleBtn);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        top.add(new JScrollPane(queryArea), BorderLayout.CENTER);
        top.add(topBtns, BorderLayout.SOUTH);

        JScrollPane tablePane = new JScrollPane(table);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        bottom.add(statusLabel, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(tablePane, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        clearBtn.addActionListener(e -> queryArea.setText(""));

        sampleBtn.addActionListener(e -> queryArea.setText(
                "SELECT e.employee_id, e.first_name, e.last_name, e.category, d.name AS department\n" +
                        "FROM employee e\n" +
                        "JOIN department d ON d.department_id = e.department_id\n" +
                        "ORDER BY e.employee_id\n" +
                        "LIMIT 50;"
        ));

        execBtn.addActionListener(e -> executeQuery());
    }

    private void executeQuery() {
        String sql = queryArea.getText().trim();

        if (sql.isEmpty()) {
            statusLabel.setText("Γράψε ένα query.");
            return;
        }

        String normalized = sql.replaceAll("\\s+", " ").trim().toLowerCase();
        if (!normalized.startsWith("select")) {
            statusLabel.setText("Επιτρέπονται μόνο SELECT queries.");
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            DefaultTableModel model = buildTableModel(rs);
            table.setModel(model);

            statusLabel.setText("OK - " + model.getRowCount() + " rows");

        } catch (Exception ex) {
            statusLabel.setText("Σφάλμα: " + ex.getMessage());
        }
    }

    private DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();

        Vector<String> colNames = new Vector<>();
        for (int i = 1; i <= cols; i++) {
            colNames.add(md.getColumnLabel(i));
        }

        Vector<Vector<Object>> data = new Vector<>();
        int rowCount = 0;
        int hardLimit = 5000;

        while (rs.next()) {
            Vector<Object> row = new Vector<>();
            for (int i = 1; i <= cols; i++) {
                row.add(rs.getObject(i));
            }
            data.add(row);

            rowCount++;
            if (rowCount >= hardLimit) break;
        }

        return new DefaultTableModel(data, colNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
}
