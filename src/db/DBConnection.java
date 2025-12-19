package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:mariadb://localhost:3306/payroll_uoc";
    private static final String USER = "root";      // άλλαξέ το αν χρειάζεται
    private static final String PASSWORD = "mypassword123";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
