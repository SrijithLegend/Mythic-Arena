import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class Database {

    private static void ensureTableExists(Connection conn) throws SQLException {
        String createTableCommand =
            "CREATE TABLE IF NOT EXISTS player_stats (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT NOT NULL, " +
            "specialty TEXT, level INTEGER, xp INTEGER, hp INTEGER, attack INTEGER, " +
            "defense INTEGER, magic_attack INTEGER, magic_defense INTEGER, speed INTEGER, " +
            "ability TEXT, move1 TEXT, move2 TEXT, move3 TEXT, move4 TEXT);";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableCommand);
        }
    }
    
    public static void savePlayer(String name, String specialty, int level, int xp, int hp, int attack, int defense, int magicAttack,
         int magicDefense, int speed, String ability, String move1, String move2, String move3, String move4) {

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
                
                Player.name = rs.getString("name");
                Player.speciality = rs.getString("specialty");
                Player.ability = rs.getString("ability");
                Player.xp = rs.getInt("xp");
                
                System.out.println("Welcome back, " + Player.name + " the " + Player.speciality + "!");

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

    public static boolean loadLatestPlayer() {
    try { Class.forName("org.sqlite.JDBC"); }
    catch (ClassNotFoundException e) { return false; }

    String url = "jdbc:sqlite:game_data.db";
    try (Connection conn = DriverManager.getConnection(url)) {
        ensureTableExists(conn);
        String selectSql = "SELECT * FROM player_stats ORDER BY id DESC LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(selectSql);
             ResultSet rs = pstmt.executeQuery()) {

            if (!rs.next()) return false; // no save exists yet — normal on first run

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
                new Moves.Move(rs.getString("move1")), new Moves.Move(rs.getString("move2")),
                new Moves.Move(rs.getString("move3")), new Moves.Move(rs.getString("move4"))
            };
            return true;
        }
    } catch (SQLException e) {
        System.out.println("Something went wrong loading saved data!");
        e.printStackTrace();
        return false;
    }
}

public static void deletePlayer(String name) {
    try { Class.forName("org.sqlite.JDBC"); }
    catch (ClassNotFoundException e) { return; }

    String url = "jdbc:sqlite:game_data.db";
    try (Connection conn = DriverManager.getConnection(url)) {
        ensureTableExists(conn);
        String deleteSql = "DELETE FROM player_stats WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
            pstmt.setString(1, name);
            int rows = pstmt.executeUpdate();
            System.out.println(rows > 0
                ? "Deleted " + rows + " save record(s) for '" + name + "'."
                : "No saved character named '" + name + "' found.");
        }
    } catch (SQLException e) {
        System.out.println("Something went wrong deleting the character!");
        e.printStackTrace();
    }
}

    public static void showLeaderboard() {
        String url = "jdbc:sqlite:game_data.db";

        String query = "SELECT name, specialty, MAX(level) as level FROM player_stats GROUP BY name ORDER BY level DESC";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("\n=================================");
            System.out.println("       🏆 LEADERBOARD 🏆         ");
            System.out.println("=================================");
            System.out.printf("%-5s %-15s %-15s %-5s\n", "RANK", "NAME", "SPECIALTY", "LEVEL");
            System.out.println("-------------------------------------------------");

            int rank = 1;
            while (rs.next()) {
                String name = rs.getString("name");
                String specialty = rs.getString("specialty");
                int level = rs.getInt("level");
                
                // Print each row beautifully formatted
                System.out.printf("%-5d %-15s %-15s Lvl %d\n", rank, name, specialty, level);
                rank++;
            }
            System.out.println("=================================\n");

        } catch (SQLException e) {
            System.out.println("Error retrieving the leaderboard!");
            e.printStackTrace();
        }
    }
    
}