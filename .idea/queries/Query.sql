USE `testdatabase`;

-- 1. Σβήσιμο των "ΛΑΘΟΣ" παλιών πινάκων (αν υπάρχουν)
DROP TABLE IF EXISTS `contract_employee`;
DROP TABLE IF EXISTS `permanent_employee`;
-- Σβήνουμε και τους σωστούς για να τους ξαναφτιάξουμε καθαρούς
DROP TABLE IF EXISTS `payroll_payment`;
DROP TABLE IF EXISTS `employee`;
DROP TABLE IF EXISTS `salary_policy`;
DROP TABLE IF EXISTS `department`;

-- 2. Δημιουργία των ΣΩΣΤΩΝ πινάκων (που θέλει η Java σου)

-- Πίνακας Τμημάτων
CREATE TABLE department (
                            department_id INT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(100) NOT NULL UNIQUE
);

-- Πίνακας Υπαλλήλων (Όλοι σε έναν πίνακα, όπως το έχεις στο EmployeeDAO)
CREATE TABLE employee (
                          employee_id INT AUTO_INCREMENT PRIMARY KEY,
                          first_name VARCHAR(50) NOT NULL,
                          last_name VARCHAR(50) NOT NULL,
                          department_id INT NOT NULL,
                          marital_status VARCHAR(20) DEFAULT 'single',
                          number_of_children INT DEFAULT 0,
                          category VARCHAR(50) DEFAULT 'ADMIN_PERMANENT',
                          address VARCHAR(150),
                          phone VARCHAR(20),
                          bank_account VARCHAR(50),
                          bank_name VARCHAR(50),
                          start_date DATE NOT NULL,
                          termination_date DATE DEFAULT NULL,
                          active BOOLEAN DEFAULT TRUE,
                          FOREIGN KEY (department_id) REFERENCES department(department_id)
);

-- Πίνακας Πληρωμών
CREATE TABLE payroll_payment (
                                 payment_id INT AUTO_INCREMENT PRIMARY KEY,
                                 employee_id INT NOT NULL,
                                 payment_date DATE NOT NULL,
                                 amount DECIMAL(10, 2) NOT NULL,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

-- Πίνακας Πολιτικής Μισθών
CREATE TABLE salary_policy (
                               policy_id INT AUTO_INCREMENT PRIMARY KEY,
                               category VARCHAR(50) NOT NULL,
                               base_salary DECIMAL(10, 2) NOT NULL,
                               allowance_per_child DECIMAL(10, 2) DEFAULT 0,
                               allowance_spouse DECIMAL(10, 2) DEFAULT 0
);

-- 3. Εισαγωγή Δεδομένων (Dummy Data)
INSERT INTO department (name) VALUES
                                  ('Computer Science'), ('Management'), ('Physics');

INSERT INTO employee (first_name, last_name, department_id, marital_status, number_of_children, category, start_date, active) VALUES
                                                                                                                                  ('Giorgos', 'Papadopoulos', 1, 'married', 2, 'TEACH_PERMANENT', '2020-01-01', TRUE),
                                                                                                                                  ('Maria', 'Nikolaou', 2, 'single', 0, 'ADMIN_CONTRACT', '2022-03-01', TRUE);

INSERT INTO salary_policy (category, base_salary, allowance_per_child, allowance_spouse) VALUES
                                                                                             ('ADMIN_PERMANENT', 1200.00, 50.00, 100.00),
                                                                                             ('ADMIN_CONTRACT', 1000.00, 50.00, 100.00),
                                                                                             ('TEACH_PERMANENT', 1500.00, 60.00, 120.00),
                                                                                             ('TEACH_CONTRACT', 1300.00, 60.00, 120.00);