import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;

public class Database {
    
    public static void savePlayer(String name, String specialty, int level, int xp, int hp, int attack, int defense, int magicAttack,
         int magicDefense, int speed, String ability, String move1, String move2, String move3, String move4) {

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

            // 2. CREATE THE TABLE
            Statement translator = conn.createStatement();

            String createTableCommand = 
                "CREATE TABLE IF NOT EXISTS player_stats (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "specialty TEXT, " +
                "level INTEGER, " +
                "xp INTEGER, " +
                "hp INTEGER, " +
                "attack INTEGER, " +
                "defense INTEGER, " +
                "magic_attack INTEGER, " +
                "magic_defense INTEGER, " +
                "speed INTEGER, " +
                "ability TEXT, " +
                "move1 TEXT, " +
                "move2 TEXT, " +
                "move3 TEXT, " +
                "move4 TEXT" +
                ");";

            translator.execute(createTableCommand);
            System.out.println("2. Character table is ready!");

            // 4. INSERT THE DATA SAFELY
            String insertSql = "INSERT INTO player_stats (name, specialty, level, xp, hp, attack, defense, magic_attack, magic_defense, speed, ability, move1, move2, move3, move4) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                // Filling in the 15 blanks
                pstmt.setString(1, name);
                pstmt.setString(2, specialty);
                pstmt.setInt(3, level);
                pstmt.setInt(4, xp);
                pstmt.setInt(5, hp);
                pstmt.setInt(6, attack);
                pstmt.setInt(7, defense);
                pstmt.setInt(8, magicAttack);
                pstmt.setInt(9, magicDefense);
                pstmt.setInt(10, speed);
                pstmt.setString(11, ability);
                pstmt.setString(12, move1);
                pstmt.setString(13, move2);
                pstmt.setString(14, move3);
                pstmt.setString(15, move4);

                // Execute the save
                pstmt.executeUpdate();
                System.out.println("3. Character '" + name + "' saved successfully with all stats and moves!");
            }

        } catch (SQLException e) {
            System.out.println("Something went wrong with the database!");
            e.printStackTrace();
        }
    }
}