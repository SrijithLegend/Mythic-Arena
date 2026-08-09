import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Connection;

public class Database {
    
    public static void main(String[] args) {

        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("0. Driver found successfully!");
        } catch (ClassNotFoundException e) {
            System.out.println("ERROR: Java still cannot see the .jar file!");
            return; 
        }

        String url = "jdbc:sqlite:game_data.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            System.out.println("1. Connected to the database!");

            Statement translator = conn.createStatement();

            String createTableCommand = 
                "CREATE TABLE IF NOT EXISTS characters (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "level INTEGER" +
                ");";

            translator.execute(createTableCommand);
            System.out.println("2. Character table is ready!");

            String insertDataCommand = "INSERT INTO characters (name, level) VALUES ('Arthur', 5);";
            translator.execute(insertDataCommand);
            System.out.println("3. Character 'Arthur' saved successfully!");

        } catch (SQLException e) {
            System.out.println("Something went wrong with the database!");
            e.printStackTrace();
        }
    }
}