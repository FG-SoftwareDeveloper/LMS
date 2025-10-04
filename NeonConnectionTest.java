import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class NeonConnectionTest {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://ep-cool-sound-adc8nufr-pooler.c-2.us-east-1.aws.neon.tech:5432/neondb?sslmode=require";
        String username = "neondb_owner";
        String password = "npg_7wcg3zuZkWXx";
        
        System.out.println("🔄 Testing Neon DB Connection...");
        System.out.println("URL: " + url);
        System.out.println("Username: " + username);
        System.out.println("Password: " + password.replaceAll(".", "*"));
        System.out.println();
        
        try {
            // Load PostgreSQL driver
            Class.forName("org.postgresql.Driver");
            
            // Connect to database
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("✅ CONNECTION SUCCESSFUL!");
            
            // Test query
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT version()");
            
            if (rs.next()) {
                System.out.println("📊 Database Version: " + rs.getString(1));
            }
            
            // Test another query
            rs = stmt.executeQuery("SELECT current_database(), current_user");
            if (rs.next()) {
                System.out.println("🗄️  Database: " + rs.getString(1));
                System.out.println("👤 User: " + rs.getString(2));
            }
            
            // Close connections
            rs.close();
            stmt.close();
            conn.close();
            
            System.out.println();
            System.out.println("🎉 NEON DB IS PROPERLY CONNECTED!");
            
        } catch (Exception e) {
            System.out.println("❌ CONNECTION FAILED!");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}