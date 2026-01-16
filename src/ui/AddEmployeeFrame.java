package ui;

import dao.DepartmentDAO;
import dao.EmployeeDAO;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

public class AddEmployeeFrame extends JFrame {

    // Δηλώνουμε τα πεδία ως πεδία της κλάσης για να έχουμε πρόσβαση παντού
    private JTextField contractEndField;
    private JTextField contractSalaryField;
    private JComboBox<String> employmentTypeBox;

    public AddEmployeeFrame() {
        setTitle("Προσθήκη Υπαλλήλου");
        setSize(450, 550); // Λίγο μεγαλύτερο ύψος για τα νέα πεδία
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Πεδία ---
        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JComboBox<String> maritalBox = new JComboBox<>(new String[]{"single", "married"});
        JTextField childrenField = new JTextField("0");

        JComboBox<String> departmentBox = new JComboBox<>();
        // Γέμισμα Τμημάτων
        Map<Integer, String> departments = DepartmentDAO.getAllDepartments();
        for (String name : departments.values()) {
            departmentBox.addItem(name);
        }

        JTextField startDateField = new JTextField(LocalDate.now().toString()); // Default σημερινή ημερομηνία
        JTextField addressField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField bankAccountField = new JTextField();
        JTextField bankNameField = new JTextField();

        // Κατηγορίες (UI Labels) -> Θα τα μετατρέψουμε σε DB values (ADMIN, TEACH)
        JComboBox<String> personnelCategoryBox = new JComboBox<>(new String[]{"ADMINISTRATIVE", "TEACHING"});

        // Τύπος (UI Labels) -> PERMANENT, CONTRACT
        employmentTypeBox = new JComboBox<>(new String[]{"PERMANENT", "CONTRACT"});

        // ΝΕΑ ΠΕΔΙΑ ΓΙΑ ΣΥΜΒΑΣΙΟΥΧΟΥΣ
        contractEndField = new JTextField();
        contractSalaryField = new JTextField();

        // Αρχικά απενεργοποιημένα (γιατί default είναι PERMANENT)
        contractEndField.setEnabled(false);
        contractSalaryField.setEnabled(false);
        contractEndField.setBackground(Color.LIGHT_GRAY);
        contractSalaryField.setBackground(Color.LIGHT_GRAY);

        // --- ΛΟΓΙΚΗ UI: Ενεργοποίηση πεδίων αν είναι CONTRACT ---
        employmentTypeBox.addActionListener(e -> {
            String selected = (String) employmentTypeBox.getSelectedItem();
            if ("CONTRACT".equals(selected)) {
                contractEndField.setEnabled(true);
                contractSalaryField.setEnabled(true);
                contractEndField.setBackground(Color.WHITE);
                contractSalaryField.setBackground(Color.WHITE);
            } else {
                contractEndField.setEnabled(false);
                contractSalaryField.setEnabled(false);
                contractEndField.setText(""); // Καθαρισμός
                contractSalaryField.setText("");
                contractEndField.setBackground(Color.LIGHT_GRAY);
                contractSalaryField.setBackground(Color.LIGHT_GRAY);
            }
        });

        JButton saveBtn = new JButton("Αποθήκευση");

        // --- Προσθήκη στο Panel ---
        panel.add(new JLabel("Όνομα:"));
        panel.add(firstNameField);
        panel.add(new JLabel("Επώνυμο:"));
        panel.add(lastNameField);
        panel.add(new JLabel("Οικ. Κατάσταση:"));
        panel.add(maritalBox);
        panel.add(new JLabel("Παιδιά:"));
        panel.add(childrenField);
        panel.add(new JLabel("Τμήμα:"));
        panel.add(departmentBox);

        panel.add(new JLabel("Ημ. Πρόσληψης (YYYY-MM-DD):"));
        panel.add(startDateField);

        panel.add(new JLabel("Διεύθυνση:"));
        panel.add(addressField);
        panel.add(new JLabel("Τηλέφωνο:"));
        panel.add(phoneField);
        panel.add(new JLabel("IBAN:"));
        panel.add(bankAccountField);
        panel.add(new JLabel("Τράπεζα:"));
        panel.add(bankNameField);

        panel.add(new JLabel("Κατηγορία Προσωπικού:"));
        panel.add(personnelCategoryBox);
        panel.add(new JLabel("Τύπος Εργασίας:"));
        panel.add(employmentTypeBox);

        // Τα νέα πεδία στο τέλος
        panel.add(new JLabel("Λήξη Σύμβασης (YYYY-MM-DD):"));
        panel.add(contractEndField);
        panel.add(new JLabel("Μισθός Σύμβασης (€):"));
        panel.add(contractSalaryField);

        add(panel, BorderLayout.CENTER);
        add(saveBtn, BorderLayout.SOUTH);

        // --- Logic Αποθήκευσης ---
        saveBtn.addActionListener(e -> {
            try {
                // 1. Έλεγχος Τμήματος
                int deptIndex = departmentBox.getSelectedIndex();
                if (deptIndex == -1) return;
                int deptId = (int) departments.keySet().toArray()[deptIndex];

                // 2. Έλεγχος Ημερομηνίας Πρόσληψης
                LocalDate startDate = LocalDate.parse(startDateField.getText());

                // Έλεγχοι (σύμφωνα με τις απαιτήσεις)
                if (startDate.isBefore(LocalDate.now())) {
                    JOptionPane.showMessageDialog(this, "Η πρόσληψη δεν μπορεί να είναι αναδρομική.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (startDate.getDayOfMonth() != 1) {
                    JOptionPane.showMessageDialog(this, "Η πρόσληψη πρέπει να γίνεται από την 1η ημέρα του μήνα.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 3. Δημιουργία του CATEGORY String για τη βάση
                // UI: ADMINISTRATIVE, TEACHING -> DB: ADMIN, TEACH
                String catSelection = (String) personnelCategoryBox.getSelectedItem();
                String typeSelection = (String) employmentTypeBox.getSelectedItem();

                String dbPrefix = "";
                if ("ADMINISTRATIVE".equals(catSelection)) dbPrefix = "ADMIN";
                else if ("TEACHING".equals(catSelection)) dbPrefix = "TEACH";

                // Τελικό string: π.χ. ADMIN_PERMANENT
                String finalCategory = dbPrefix + "_" + typeSelection;

                // 4. Χειρισμός Στοιχείων Σύμβασης
                String contractEnd = null;
                Double contractSalary = null;

                if ("CONTRACT".equals(typeSelection)) {
                    // Αν είναι συμβασιούχος, πρέπει να έχει ημερομηνία λήξης και μισθό
                    if (contractEndField.getText().trim().isEmpty() || contractSalaryField.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Για συμβασιούχους απαιτείται Ημ. Λήξης και Μισθός.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    contractEnd = contractEndField.getText().trim();
                    try {
                        LocalDate.parse(contractEnd); // Έλεγχος αν είναι σωστή ημερομηνία
                        contractSalary = Double.parseDouble(contractSalaryField.getText().trim());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Λάθος μορφή ημερομηνίας λήξης ή μισθού.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                // 5. Κλήση DAO
                boolean success = EmployeeDAO.insertEmployee(
                        firstNameField.getText(),
                        lastNameField.getText(),
                        maritalBox.getSelectedItem().toString(),
                        Integer.parseInt(childrenField.getText()),
                        deptId,
                        startDateField.getText(),
                        addressField.getText(),
                        phoneField.getText(),
                        bankAccountField.getText(),
                        bankNameField.getText(),
                        finalCategory,    // Το νέο πεδίο category
                        contractEnd,      // null αν Permanent
                        contractSalary    // null αν Permanent
                );

                if (success) {
                    JOptionPane.showMessageDialog(this, "Ο υπάλληλος προστέθηκε επιτυχώς! ✔");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Σφάλμα κατά την αποθήκευση στη βάση.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                }

            } catch (DateTimeParseException dtpe) {
                JOptionPane.showMessageDialog(this, "Λάθος μορφή ημερομηνίας (YYYY-MM-DD).", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Ελέγξτε τα αριθμητικά πεδία (Παιδιά, Μισθός).", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Άγνωστο Σφάλμα: " + ex.getMessage(), "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}