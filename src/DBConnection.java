import java.sql.*;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/inventory_new?serverTimezone=UTC";
    private static final String USER = "root"; // change if needed
    private static final String PASS = "Vikash@2005";     // change if needed

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("MySQL driver not found", ex);
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }
}