import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** All persistence goes through here (SQLite via JDBC). */
public class Database {

    /** Override with -Drpg.db=path/to/file.db (the self-test uses a throwaway file). */
    private static final String URL = "jdbc:sqlite:" + System.getProperty("rpg.db", "game_data.db");

    private static final String PLAYER_COLUMNS =
        "name, specialty, level, xp, hp, attack, defense, magic_attack, magic_defense, speed, " +
        "ability, move1, move2, move3, move4, wins, losses, rating, gold, potions, is_bot";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // ------------------------------------------------------------------ setup

    /** Creates the schema, migrates saves from the old player_stats table, and seeds arena bots. */
    public static void init() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS players (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE COLLATE NOCASE, " +
                "specialty TEXT NOT NULL, " +
                "level INTEGER NOT NULL DEFAULT 1, " +
                "xp INTEGER NOT NULL DEFAULT 0, " +
                "hp INTEGER NOT NULL, attack INTEGER NOT NULL, defense INTEGER NOT NULL, " +
                "magic_attack INTEGER NOT NULL, magic_defense INTEGER NOT NULL, speed INTEGER NOT NULL, " +
                "ability TEXT NOT NULL, " +
                "move1 TEXT NOT NULL, move2 TEXT NOT NULL, move3 TEXT NOT NULL, move4 TEXT NOT NULL, " +
                "wins INTEGER NOT NULL DEFAULT 0, losses INTEGER NOT NULL DEFAULT 0, " +
                "rating INTEGER NOT NULL DEFAULT " + Player.STARTING_RATING + ", " +
                "gold INTEGER NOT NULL DEFAULT " + Player.STARTING_GOLD + ", " +
                "potions INTEGER NOT NULL DEFAULT " + Player.STARTING_POTIONS + ", " +
                "is_bot INTEGER NOT NULL DEFAULT 0, " +
                "created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')), " +
                "last_played TEXT NOT NULL DEFAULT (datetime('now','localtime')))");
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS battles (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "mode TEXT NOT NULL, " +
                "player_name TEXT NOT NULL, " +
                "opponent_name TEXT NOT NULL, " +
                "winner_name TEXT, " +
                "turns INTEGER NOT NULL, " +
                "xp_gained INTEGER NOT NULL DEFAULT 0, " +
                "rating_change INTEGER NOT NULL DEFAULT 0, " +
                "fought_at TEXT NOT NULL DEFAULT (datetime('now','localtime')))");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_battles_player ON battles(player_name)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_battles_opponent ON battles(opponent_name)");

            migrateLegacy(conn);
            seedBots(conn);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not initialise the database at " + URL, e);
        }
    }

    /**
     * Older versions inserted a new player_stats row on every save. Keep the newest row per
     * name, convert it, then rename the old table so this only ever runs once.
     */
    private static void migrateLegacy(Connection conn) throws SQLException {
        if (!tableExists(conn, "player_stats")) return;

        int migrated = 0;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT * FROM player_stats WHERE id IN (SELECT MAX(id) FROM player_stats GROUP BY name COLLATE NOCASE)")) {
            while (rs.next()) {
                Player p = new Player();
                p.name = rs.getString("name");
                p.speciality = rs.getString("specialty");
                if (p.name == null || p.speciality == null || existsIn(conn, p.name)) continue;
                try {
                    Moves.catalogFor(p.speciality);
                } catch (IllegalArgumentException e) {
                    continue;
                }
                p.level = Math.max(1, rs.getInt("level"));
                p.xp = Math.max(0, rs.getInt("xp"));
                p.setStats(new int[] {rs.getInt("hp"), rs.getInt("attack"), rs.getInt("defense"),
                    rs.getInt("magic_attack"), rs.getInt("magic_defense"), rs.getInt("speed")});

                Moves.Move abilityMove = Moves.find(p.speciality, rs.getString("ability"));
                p.ability = abilityMove != null ? abilityMove.name() : Character.abilityOptions(p.speciality)[0].name();

                Set<Moves.Move> moves = new LinkedHashSet<>();
                for (int i = 1; i <= 4; i++) {
                    Moves.Move m = Moves.find(p.speciality, rs.getString("move" + i));
                    if (m != null && m.unlockLevel() <= p.level) moves.add(m);
                }
                for (Moves.Move m : Moves.defaultMoves(p.speciality)) {
                    if (moves.size() < 4) moves.add(m);
                }
                p.moves = moves.toArray(new Moves.Move[0]);

                // Top up / trim stats so the hero is valid under the current rules.
                int diff = Player.statPointsForLevel(p.level) - p.totalStats();
                if (diff > 0) {
                    p.hp += diff;
                } else if (diff < 0) {
                    p.setStats(Player.buildGains(Character.recommendedBuild(p.speciality), Player.statPointsForLevel(p.level), 1));
                }
                insert(conn, p);
                migrated++;
            }
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE player_stats RENAME TO legacy_player_stats");
        }
        if (migrated > 0) {
            System.out.println("[DB] Migrated " + migrated + " hero(es) from the old save format.");
        }
    }

    private static boolean tableExists(Connection conn, String table) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM sqlite_master WHERE type='table' AND name=?")) {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void seedBots(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM players WHERE is_bot = 1")) {
            if (rs.next() && rs.getInt(1) > 0) return;
        }
        Object[][] bots = {
            {"Bram Ironhide", "Warrior", 2}, {"Lyra Emberveil", "Mage", 4},
            {"Vex Shadowstep", "Rogue", 6}, {"Sir Aldric", "Paladin", 8},
            {"Grimgar the Bold", "Warrior", 12}, {"Morwen Frost", "Mage", 15},
            {"Nyx", "Rogue", 20}, {"Seraphine Dawn", "Paladin", 25}
        };
        for (Object[] b : bots) {
            if (existsIn(conn, (String) b[0])) continue;
            insert(conn, makeBot((String) b[0], (String) b[1], (Integer) b[2]));
        }
    }

    /** Builds an AI hero with the recommended stats and its strongest unlocked moves. */
    public static Player makeBot(String name, String specialty, int level) {
        Player p = new Player();
        p.name = name;
        p.speciality = specialty;
        p.level = level;
        p.bot = true;
        p.rating = 900 + level * 25;
        p.ability = Character.abilityOptions(specialty)[0].name();
        p.setStats(Player.buildGains(Character.recommendedBuild(specialty), Player.statPointsForLevel(level), 1));

        Moves.Move[] all = Moves.catalogFor(specialty);
        List<Moves.Move> picks = new ArrayList<>();
        if (level >= Moves.MASTER_LEVEL) picks.add(all[9]);
        if (level >= Moves.ULTIMATE_LEVEL) picks.add(all[8]);
        for (Moves.Move m : Moves.defaultMoves(specialty)) {
            if (picks.size() < 4) picks.add(m);
        }
        p.moves = picks.toArray(new Moves.Move[0]);
        return p;
    }

    // ------------------------------------------------------------------ players

    public static boolean nameExists(String name) {
        try (Connection conn = connect()) {
            return existsIn(conn, name);
        } catch (SQLException e) {
            System.out.println("Database error checking name: " + e.getMessage());
            return true;
        }
    }

    private static boolean existsIn(Connection conn, String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM players WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void insert(Connection conn, Player p) throws SQLException {
        String sql = "INSERT INTO players (" + PLAYER_COLUMNS + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, p);
            ps.executeUpdate();
        }
    }

    /** Inserts a new hero or updates the existing row with the same name. */
    public static boolean savePlayer(Player p) {
        String sql = "INSERT INTO players (" + PLAYER_COLUMNS + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) " +
            "ON CONFLICT(name) DO UPDATE SET specialty=excluded.specialty, level=excluded.level, xp=excluded.xp, " +
            "hp=excluded.hp, attack=excluded.attack, defense=excluded.defense, magic_attack=excluded.magic_attack, " +
            "magic_defense=excluded.magic_defense, speed=excluded.speed, ability=excluded.ability, " +
            "move1=excluded.move1, move2=excluded.move2, move3=excluded.move3, move4=excluded.move4, " +
            "wins=excluded.wins, losses=excluded.losses, rating=excluded.rating, gold=excluded.gold, " +
            "potions=excluded.potions, is_bot=excluded.is_bot, last_played=datetime('now','localtime')";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, p);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("[!] Could not save " + p.name + ": " + e.getMessage());
            return false;
        }
    }

    /** Updates only the arena record, so a defender's other progress and last_played are untouched. */
    public static void updateRecord(Player p) {
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement("UPDATE players SET wins = ?, losses = ?, rating = ? WHERE name = ?")) {
            ps.setInt(1, p.wins);
            ps.setInt(2, p.losses);
            ps.setInt(3, p.rating);
            ps.setString(4, p.name);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[!] Could not update " + p.name + "'s record: " + e.getMessage());
        }
    }

    private static void bind(PreparedStatement ps, Player p) throws SQLException {
        ps.setString(1, p.name);
        ps.setString(2, p.speciality);
        ps.setInt(3, p.level);
        ps.setInt(4, p.xp);
        int[] s = p.stats();
        for (int i = 0; i < s.length; i++) ps.setInt(5 + i, s[i]);
        ps.setString(11, p.ability);
        for (int i = 0; i < 4; i++) ps.setString(12 + i, p.moves[i].name());
        ps.setInt(16, p.wins);
        ps.setInt(17, p.losses);
        ps.setInt(18, p.rating);
        ps.setInt(19, p.gold);
        ps.setInt(20, p.potions);
        ps.setInt(21, p.bot ? 1 : 0);
    }

    private static Player fromRow(ResultSet rs) throws SQLException {
        Player p = new Player();
        p.name = rs.getString("name");
        p.speciality = rs.getString("specialty");
        p.level = rs.getInt("level");
        p.xp = rs.getInt("xp");
        p.setStats(new int[] {rs.getInt("hp"), rs.getInt("attack"), rs.getInt("defense"),
            rs.getInt("magic_attack"), rs.getInt("magic_defense"), rs.getInt("speed")});
        p.ability = rs.getString("ability");
        p.moves = new Moves.Move[4];
        Moves.Move[] defaults = Moves.defaultMoves(p.speciality);
        for (int i = 0; i < 4; i++) {
            Moves.Move m = Moves.find(p.speciality, rs.getString("move" + (i + 1)));
            p.moves[i] = m != null ? m : defaults[i];
        }
        p.wins = rs.getInt("wins");
        p.losses = rs.getInt("losses");
        p.rating = rs.getInt("rating");
        p.gold = rs.getInt("gold");
        p.potions = rs.getInt("potions");
        p.bot = rs.getInt("is_bot") == 1;
        return p;
    }

    public static Player loadPlayer(String name) {
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM players WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? fromRow(rs) : null;
            }
        } catch (SQLException e) {
            System.out.println("[!] Error loading '" + name + "': " + e.getMessage());
            return null;
        }
    }

    /** The human hero who played most recently, or null if there is none. */
    public static Player loadLastPlayed() {
        List<Player> heroes = query("SELECT * FROM players WHERE is_bot = 0 ORDER BY last_played DESC, id DESC LIMIT 1");
        return heroes.isEmpty() ? null : heroes.get(0);
    }

    public static List<Player> listHumanHeroes() {
        return query("SELECT * FROM players WHERE is_bot = 0 ORDER BY last_played DESC, id DESC");
    }

    /** Everyone except the given hero, closest rating first. */
    public static List<Player> listOpponents(Player self) {
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM players WHERE name <> ? ORDER BY ABS(rating - ?), level")) {
            ps.setString(1, self.name);
            ps.setInt(2, self.rating);
            return readAll(ps);
        } catch (SQLException e) {
            System.out.println("[!] Error listing opponents: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static List<Player> query(String sql) {
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            return readAll(ps);
        } catch (SQLException e) {
            System.out.println("[!] Database error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static List<Player> readAll(PreparedStatement ps) throws SQLException {
        List<Player> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(fromRow(rs));
        }
        return list;
    }

    public static boolean deletePlayer(String name) {
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM players WHERE name = ? AND is_bot = 0")) {
            ps.setString(1, name);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[!] Error deleting '" + name + "': " + e.getMessage());
            return false;
        }
    }

    // ------------------------------------------------------------------ battles & rankings

    public static void recordBattle(String mode, String player, String opponent, String winner,
                                    int turns, int xpGained, int ratingChange) {
        String sql = "INSERT INTO battles (mode, player_name, opponent_name, winner_name, turns, xp_gained, rating_change) " +
                     "VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mode);
            ps.setString(2, player);
            ps.setString(3, opponent);
            ps.setString(4, winner);
            ps.setInt(5, turns);
            ps.setInt(6, xpGained);
            ps.setInt(7, ratingChange);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[!] Could not record battle: " + e.getMessage());
        }
    }

    public static void showLeaderboard() {
        String sql = "SELECT name, specialty, level, rating, wins, losses, is_bot FROM players " +
                     "ORDER BY rating DESC, level DESC, wins DESC LIMIT 20";
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n=================================================================");
            System.out.println("                          LEADERBOARD");
            System.out.println("=================================================================");
            System.out.printf("%-5s %-24s %-9s %-6s %-7s %-9s%n", "RANK", "NAME", "CLASS", "LEVEL", "RATING", "W-L");
            System.out.println("-----------------------------------------------------------------");
            int rank = 1;
            while (rs.next()) {
                String name = rs.getString("name") + (rs.getInt("is_bot") == 1 ? " (bot)" : "");
                System.out.printf("%-5d %-24s %-9s %-6d %-7d %d-%d%n", rank++, name, rs.getString("specialty"),
                    rs.getInt("level"), rs.getInt("rating"), rs.getInt("wins"), rs.getInt("losses"));
            }
            System.out.println("=================================================================");
        } catch (SQLException e) {
            System.out.println("[!] Error retrieving the leaderboard: " + e.getMessage());
        }
    }

    /** Battles the hero fought, plus arena challenges other players made against them. */
    public static void showHistory(String name, int limit) {
        String sql = "SELECT * FROM battles WHERE player_name = ? OR opponent_name = ? ORDER BY id DESC LIMIT ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, name);
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\n=================== BATTLE HISTORY: " + name + " ===================");
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    boolean wasChallenger = rs.getString("player_name").equalsIgnoreCase(name);
                    String opponent = wasChallenger ? rs.getString("opponent_name") : rs.getString("player_name");
                    String winner = rs.getString("winner_name");
                    String result = winner == null ? "DRAW" : winner.equalsIgnoreCase(name) ? "WIN " : "LOSS";
                    String extra = wasChallenger
                        ? (rs.getInt("xp_gained") > 0 ? "+" + rs.getInt("xp_gained") + " XP" : "") + ratingText(rs.getInt("rating_change"))
                        : "(defended)" + ratingText(-rs.getInt("rating_change"));
                    System.out.printf("%s  %-8s %s vs %-18s %2d turns  %s%n", rs.getString("fought_at"),
                        rs.getString("mode"), result, opponent, rs.getInt("turns"), extra);
                }
                if (!any) System.out.println("No battles yet. Go fight something!");
            }
        } catch (SQLException e) {
            System.out.println("[!] Error loading battle history: " + e.getMessage());
        }
    }

    private static String ratingText(int change) {
        if (change == 0) return "";
        return " rating " + (change > 0 ? "+" : "") + change;
    }
}
