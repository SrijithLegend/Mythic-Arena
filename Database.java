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

            String insertSql = "INSERT INTO player_stats (name, specialty, level, xp, hp, attack, defense, magic_attack, magic_defense, speed, ability, move1, move2, move3, move4) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
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


                pstmt.executeUpdate();
                System.out.println("3. Character '" + name + "' saved successfully with all stats and moves!");
            }

        } catch (SQLException e) {
            System.out.println("Something went wrong with the database!");
            e.printStackTrace();
        }
    }

    public static boolean loadPlayer(String searchName) {
        String query = "SELECT * FROM player_stats WHERE name = ?";
        String url = "jdbc:sqlite:game_data.db";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, searchName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("\n--- CHARACTER LOADED SUCCESSFULLY ---");
                
                String loadedName = rs.getString("name");
                String loadedSpecialty = rs.getString("specialty");
                
                System.out.println("Welcome back, " + loadedName + " the " + loadedSpecialty + "!");

                Player.setPlayerstats.level = rs.getInt("level");
                Player.setPlayerstats.hp = rs.getInt("hp");
                Player.setPlayerstats.attack = rs.getInt("attack");
                Player.setPlayerstats.defense = rs.getInt("defense");
                Player.setPlayerstats.magicAttack = rs.getInt("magic_attack");
                Player.setPlayerstats.magicDefense = rs.getInt("magic_defense");
                Player.setPlayerstats.speed = rs.getInt("speed");
                
                Moves.selectedMoves = new Moves.Move[4];
                Moves.selectedMoves[0] = new Moves.Move(rs.getString("move1"));
                Moves.selectedMoves[1] = new Moves.Move(rs.getString("move2"));
                Moves.selectedMoves[2] = new Moves.Move(rs.getString("move3"));
                Moves.selectedMoves[3] = new Moves.Move(rs.getString("move4"));

                return true;

            } else {
                System.out.println("No character found with the name: " + searchName);
                return false; 
            }

        } catch (SQLException e) {
            System.out.println("Error trying to load the character!");
            e.printStackTrace();
            return false;
        }
    }

    
}