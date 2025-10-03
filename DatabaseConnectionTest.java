import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionTest {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://ep-rapid-king-adipnv5l-pooler.c-2.us-east-1.aws.neon.tech:5432/neondb?sslmode=require";
        String username = "neondb_owner";
        String password = "npg_o9JBCIQg0eHP";
        
        System.out.println("Testing connection to Neon DB...");
        System.out.println("URL: " + url);
        System.out.println("Username: " + username);
        System.out.println("Password: " + password.replaceAll(".", "*")); // Hide password
        
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, username, password);
            System.out.println("✅ CONNECTION SUCCESS!");
            System.out.println("Database: " + connection.getCatalog());
            connection.close();
        } catch (ClassNotFoundException e) {
            System.out.println("❌ PostgreSQL driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("❌ CONNECTION FAILED!");
            System.out.println("Error Code: " + e.getErrorCode());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Message: " + e.getMessage());
        }
    }
}