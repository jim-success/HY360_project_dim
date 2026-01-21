package ui;

import dao.DepartmentDAO;
import dao.EmployeeDAO;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.Map;

public class AddEmployeeFrame extends JFrame {
    private JTextField contractEndField, contractSalaryField;
    private JComboBox<String> employmentTypeBox;

    public AddEmployeeFrame() {
        setTitle("Προσθήκη Υπαλλήλου");
        setSize(450, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JComboBox<String> maritalBox = new JComboBox<>(new String[]{"single", "married"});
        JTextField childrenField = new JTextField("0");

        JComboBox<String> departmentBox = new JComboBox<>();
        Map<Integer, String> departments = DepartmentDAO.getAllDepartments();
        for (String name : departments.values()) departmentBox.addItem(name);

        JTextField startDateField = new JTextField(LocalDate.now().toString());
        JTextField addressField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField bankAccountField = new JTextField();
        JTextField bankNameField = new JTextField();

        JComboBox<String> personnelCategoryBox = new JComboBox<>(new String[]{"ADMINISTRATIVE", "TEACHING"});
        employmentTypeBox = new JComboBox<>(new String[]{"PERMANENT", "CONTRACT"});

        contractEndField = new JTextField(); contractSalaryField = new JTextField();
        contractEndField.setEnabled(false); contractSalaryField.setEnabled(false);
        contractEndField.setBackground(Color.LIGHT_GRAY); contractSalaryField.setBackground(Color.LIGHT_GRAY);

        employmentTypeBox.addActionListener(e -> {
            boolean isContract = "CONTRACT".equals(employmentTypeBox.getSelectedItem());
            contractEndField.setEnabled(isContract); contractSalaryField.setEnabled(isContract);
            Color bg = isContract ? Color.WHITE : Color.LIGHT_GRAY;
            contractEndField.setBackground(bg); contractSalaryField.setBackground(bg);
        });

        JButton saveBtn = new JButton("Αποθήκευση");

        panel.add(new JLabel("Όνομα:")); panel.add(firstNameField);
        panel.add(new JLabel("Επώνυμο:")); panel.add(lastNameField);
        panel.add(new JLabel("Οικ. Κατάσταση:")); panel.add(maritalBox);
        panel.add(new JLabel("Παιδιά:")); panel.add(childrenField);
        panel.add(new JLabel("Τμήμα:")); panel.add(departmentBox);
        panel.add(new JLabel("Ημ. Πρόσληψης:")); panel.add(startDateField);
        panel.add(new JLabel("Διεύθυνση:")); panel.add(addressField);
        panel.add(new JLabel("Τηλέφωνο:")); panel.add(phoneField);
        panel.add(new JLabel("IBAN:")); panel.add(bankAccountField);
        panel.add(new JLabel("Τράπεζα:")); panel.add(bankNameField);
        panel.add(new JLabel("Κατηγορία:")); panel.add(personnelCategoryBox);
        panel.add(new JLabel("Τύπος:")); panel.add(employmentTypeBox);
        panel.add(new JLabel("Λήξη Σύμβασης:")); panel.add(contractEndField);
        panel.add(new JLabel("Μισθός (Βασικός/Σύμβασης):")); panel.add(contractSalaryField);

        add(panel, BorderLayout.CENTER);
        add(saveBtn, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> {
            try {
                int deptId = (int) departments.keySet().toArray()[departmentBox.getSelectedIndex()];
                String prefix = "ADMINISTRATIVE".equals(personnelCategoryBox.getSelectedItem()) ? "ADMIN" : "TEACH";
                String category = prefix + "_" + employmentTypeBox.getSelectedItem();

                String cEnd = null; Double salary = null;
                if (!contractSalaryField.getText().trim().isEmpty()) salary = Double.parseDouble(contractSalaryField.getText().trim());
                if ("CONTRACT".equals(employmentTypeBox.getSelectedItem())) {
                    cEnd = contractEndField.getText().trim();
                    if (cEnd.isEmpty() || salary == null) throw new Exception("Required fields missing");
                }

                boolean success = EmployeeDAO.insertEmployee(
                        firstNameField.getText(), lastNameField.getText(), maritalBox.getSelectedItem().toString(),
                        Integer.parseInt(childrenField.getText()), deptId, startDateField.getText(),
                        addressField.getText(), phoneField.getText(), bankAccountField.getText(), bankNameField.getText(),
                        category, cEnd, salary
                );
                if (success) { JOptionPane.showMessageDialog(this, "Επιτυχία! ✔"); dispose(); }
                else JOptionPane.showMessageDialog(this, "Σφάλμα.");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Λάθος δεδομένα."); }
        });
    }
}