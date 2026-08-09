import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    
    public static boolean loadPlayer(String name) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("ERROR: driver not found!");
            return false;
        }

        String url = "jdbc:sqlite:game_data.db";
        String selectSql = "SELECT * FROM player_stats WHERE name = ? ORDER BY id DESC LIMIT 1";

        try (Connection conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(selectSql)) {

            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No saved character named '" + name + "' found.");
                    return false;
                }

                Player.name = rs.getString("name");
                Player.speciality = rs.getString("specialty");
                Player.setPlayerstats.level = rs.getInt("level");
                Player.xp = rs.getInt("xp");
                Player.setPlayerstats.hp = rs.getInt("hp");
                Player.setPlayerstats.attack = rs.getInt("attack");
                Player.setPlayerstats.defense = rs.getInt("defense");
                Player.setPlayerstats.magicAttack = rs.getInt("magic_attack");
                Player.setPlayerstats.magicDefense = rs.getInt("magic_defense");
                Player.setPlayerstats.speed = rs.getInt("speed");
                Player.ability = rs.getString("ability");

                Moves.selectedMoves = new Moves.Move[] {
                    new Moves.Move(rs.getString("move1")),
                    new Moves.Move(rs.getString("move2")),
                    new Moves.Move(rs.getString("move3")),
                    new Moves.Move(rs.getString("move4"))
                };

                System.out.println("Character '" + Player.name + "' loaded successfully!");
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Something went wrong loading the character!");
            e.printStackTrace();
            return false;
        }
    }
}