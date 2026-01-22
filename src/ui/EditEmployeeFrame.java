package ui;

import dao.DepartmentDAO;
import dao.EmployeeDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.Map;

public class EditEmployeeFrame extends JFrame {
    private JTextField contractEndField, salaryField;
    private JComboBox<String> employmentTypeBox;

    public EditEmployeeFrame() {
        setTitle("Επεξεργασία Υπαλλήλου");
        setSize(700, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JComboBox<String> employeeBox = new JComboBox<>();
        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JComboBox<String> maritalBox = new JComboBox<>(new String[]{"single", "married"});

        JTextField childrenCountField = new JTextField();
        childrenCountField.setEditable(false);

        JComboBox<String> departmentBox = new JComboBox<>();
        JTextField addressField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField bankAccountField = new JTextField();
        JTextField bankNameField = new JTextField();

        JComboBox<String> personnelCategoryBox = new JComboBox<>(new String[]{"ADMINISTRATIVE", "TEACHING"});
        employmentTypeBox = new JComboBox<>(new String[]{"PERMANENT", "CONTRACT"});
        employmentTypeBox.setEnabled(false);

        contractEndField = new JTextField();
        salaryField = new JTextField();

        JCheckBox activeBox = new JCheckBox("Ενεργός");
        JTextField terminationDateField = new JTextField();

        Map<Integer, String> employees = EmployeeDAO.getEmployeeNames();
        for (String name : employees.values()) employeeBox.addItem(name);

        Map<Integer, String> departments = DepartmentDAO.getAllDepartments();
        for (String name : departments.values()) departmentBox.addItem(name);

        JTable childrenTable = new JTable();
        JScrollPane childrenScroll = new JScrollPane(childrenTable);
        childrenScroll.setPreferredSize(new Dimension(650, 220));

        JTextField childBirthDateField = new JTextField("2015-01-01");
        JButton addChildBtn = new JButton("Προσθήκη");
        JButton updateChildBtn = new JButton("Αλλαγή Ημ/νίας");
        JButton deleteChildBtn = new JButton("Διαγραφή");

        Runnable refreshChildren = () -> {
            int idx = employeeBox.getSelectedIndex();
            if (idx < 0) return;

            int empId = (int) employees.keySet().toArray()[idx];

            DefaultTableModel model = new DefaultTableModel(
                    EmployeeDAO.getChildrenRows(empId),
                    EmployeeDAO.getChildColumns()
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            childrenTable.setModel(model);
            childrenCountField.setText(String.valueOf(model.getRowCount()));
        };

        childrenTable.getSelectionModel().addListSelectionListener(ev -> {
            int row = childrenTable.getSelectedRow();
            if (row < 0) return;
            Object bd = childrenTable.getValueAt(row, 1);
            if (bd != null) childBirthDateField.setText(bd.toString());
        });

        addChildBtn.addActionListener(ev -> {
            try {
                int idx = employeeBox.getSelectedIndex();
                if (idx < 0) return;

                int empId = (int) employees.keySet().toArray()[idx];
                LocalDate bd = LocalDate.parse(childBirthDateField.getText().trim());

                if (EmployeeDAO.addChild(empId, bd)) {
                    refreshChildren.run();
                } else {
                    JOptionPane.showMessageDialog(this, "Αποτυχία προσθήκης");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Λάθος ημερομηνία (YYYY-MM-DD)");
            }
        });

        updateChildBtn.addActionListener(ev -> {
            try {
                int row = childrenTable.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(this, "Διάλεξε παιδί από τον πίνακα");
                    return;
                }

                int childId = Integer.parseInt(childrenTable.getValueAt(row, 0).toString());
                LocalDate bd = LocalDate.parse(childBirthDateField.getText().trim());

                if (EmployeeDAO.updateChildBirthDate(childId, bd)) {
                    refreshChildren.run();
                } else {
                    JOptionPane.showMessageDialog(this, "Αποτυχία ενημέρωσης");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Λάθος ημερομηνία (YYYY-MM-DD)");
            }
        });

        deleteChildBtn.addActionListener(ev -> {
            int row = childrenTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Διάλεξε παιδί από τον πίνακα");
                return;
            }

            int childId = Integer.parseInt(childrenTable.getValueAt(row, 0).toString());

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Σίγουρα θέλεις διαγραφή αυτού του παιδιού;",
                    "Επιβεβαίωση",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.YES_OPTION) {
                if (EmployeeDAO.deleteChild(childId)) {
                    refreshChildren.run();
                } else {
                    JOptionPane.showMessageDialog(this, "Αποτυχία διαγραφής");
                }
            }
        });

        employeeBox.addActionListener(e -> {
            int index = employeeBox.getSelectedIndex();
            if (index < 0) return;

            int empId = (int) employees.keySet().toArray()[index];
            EmployeeDAO.EmployeeDetails d = EmployeeDAO.getEmployeeDetails(empId);
            if (d == null) return;

            firstNameField.setText(d.firstName);
            lastNameField.setText(d.lastName);
            maritalBox.setSelectedItem(d.maritalStatus);

            refreshChildren.run();

            int i = 0;
            for (Integer deptId : departments.keySet()) {
                if (deptId == d.departmentId) {
                    departmentBox.setSelectedIndex(i);
                    break;
                }
                i++;
            }

            addressField.setText(d.address != null ? d.address : "");
            phoneField.setText(d.phone != null ? d.phone : "");
            bankAccountField.setText(d.bankAccount != null ? d.bankAccount : "");
            bankNameField.setText(d.bankName != null ? d.bankName : "");

            if (d.category != null && d.category.contains("ADMIN"))
                personnelCategoryBox.setSelectedItem("ADMINISTRATIVE");
            else personnelCategoryBox.setSelectedItem("TEACHING");

            if (d.category != null && d.category.contains("CONTRACT")) {
                employmentTypeBox.setSelectedItem("CONTRACT");
                contractEndField.setEnabled(true);
                contractEndField.setText(d.contractEnd != null ? d.contractEnd.toString() : "");
            } else {
                employmentTypeBox.setSelectedItem("PERMANENT");
                contractEndField.setEnabled(false);
                contractEndField.setText("-");
            }

            salaryField.setText(d.salary != null ? d.salary.toString() : "0.0");
            activeBox.setSelected(d.active);
            terminationDateField.setText(d.terminationDate != null ? d.terminationDate.toString() : "");
        });

        JButton saveBtn = new JButton("Αποθήκευση");
        saveBtn.addActionListener(e -> {
            try {
                int empId = (int) employees.keySet().toArray()[employeeBox.getSelectedIndex()];
                int deptId = (int) departments.keySet().toArray()[departmentBox.getSelectedIndex()];

                EmployeeDAO.EmployeeDetails d = new EmployeeDAO.EmployeeDetails();

                d.firstName = firstNameField.getText();
                d.lastName = lastNameField.getText();
                d.maritalStatus = maritalBox.getSelectedItem().toString();
                d.departmentId = deptId;
                d.address = addressField.getText();
                d.phone = phoneField.getText();
                d.bankAccount = bankAccountField.getText();
                d.bankName = bankNameField.getText();
                d.active = activeBox.isSelected();

                String td = terminationDateField.getText().trim();
                d.terminationDate = td.isEmpty() ? null : LocalDate.parse(td);

                String prefix = "ADMINISTRATIVE".equals(personnelCategoryBox.getSelectedItem()) ? "ADMIN" : "TEACH";
                d.category = prefix + "_" + employmentTypeBox.getSelectedItem();
                d.salary = Double.parseDouble(salaryField.getText());

                if ("CONTRACT".equals(employmentTypeBox.getSelectedItem())) {
                    String ce = contractEndField.getText().trim();
                    d.contractEnd = ce.isEmpty() ? null : LocalDate.parse(ce);
                }

                if (EmployeeDAO.updateEmployeeDetails(empId, d)) {
                    JOptionPane.showMessageDialog(this, "Ενημερώθηκε!");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Σφάλμα.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Λάθος δεδομένα.");
            }
        });

        panel.add(new JLabel("Υπάλληλος:"));
        panel.add(employeeBox);

        panel.add(new JLabel("Όνομα:"));
        panel.add(firstNameField);

        panel.add(new JLabel("Επώνυμο:"));
        panel.add(lastNameField);

        panel.add(new JLabel("Οικ. Κατάσταση:"));
        panel.add(maritalBox);

        panel.add(new JLabel("Παιδιά (πλήθος):"));
        panel.add(childrenCountField);

        panel.add(new JLabel("Τμήμα:"));
        panel.add(departmentBox);

        panel.add(new JLabel("Διεύθυνση:"));
        panel.add(addressField);

        panel.add(new JLabel("Τηλέφωνο:"));
        panel.add(phoneField);

        panel.add(new JLabel("IBAN:"));
        panel.add(bankAccountField);

        panel.add(new JLabel("Τράπεζα:"));
        panel.add(bankNameField);

        panel.add(new JSeparator());
        panel.add(new JSeparator());

        panel.add(new JLabel("Κατηγορία:"));
        panel.add(personnelCategoryBox);

        panel.add(new JLabel("Τύπος:"));
        panel.add(employmentTypeBox);

        panel.add(new JLabel("Μισθός:"));
        panel.add(salaryField);

        panel.add(new JLabel("Λήξη Σύμβασης:"));
        panel.add(contractEndField);

        panel.add(new JLabel("Ενεργός:"));
        panel.add(activeBox);

        panel.add(new JLabel("Ημ. Απόλυσης:"));
        panel.add(terminationDateField);

        JPanel childrenPanel = new JPanel(new BorderLayout(5, 5));
        childrenPanel.setBorder(BorderFactory.createTitledBorder("Διαχείριση Παιδιών"));

        JPanel childrenControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        childrenControls.add(new JLabel("Ημ/νία Γέννησης (YYYY-MM-DD):"));
        childBirthDateField.setColumns(10);
        childrenControls.add(childBirthDateField);
        childrenControls.add(addChildBtn);
        childrenControls.add(updateChildBtn);
        childrenControls.add(deleteChildBtn);

        childrenPanel.add(childrenControls, BorderLayout.NORTH);
        childrenPanel.add(childrenScroll, BorderLayout.CENTER);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(panel);
        content.add(Box.createVerticalStrut(10));
        content.add(childrenPanel);

        add(new JScrollPane(content), BorderLayout.CENTER);
        add(saveBtn, BorderLayout.SOUTH);

        if (employeeBox.getItemCount() > 0) {
            employeeBox.setSelectedIndex(0);
        }
    }
}
