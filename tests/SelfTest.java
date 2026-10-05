import java.io.File;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;

/**
 * Self-test for the game. Run via run-tests.bat / run-tests.sh, which point the game at a
 * throwaway database with -Drpg.db so real saves are never touched.
 */
public class SelfTest {

    private static int passed = 0;

    public static void main(String[] args) throws Exception {
        String dbPath = System.getProperty("rpg.db");
        if (dbPath == null || dbPath.equals("game_data.db")) {
            throw new IllegalStateException("Run with -Drpg.db=<temp file> so the real save is not touched.");
        }
        new File(dbPath).delete();

        testLegacyMigration();
        testSchemaAndBots();
        testSaveLoadUpsertDelete();
        testStatBuilds();
        testElo();
        testPasswords();
        testLevelSystem();
        testBattles();
        testHistoryAndLeaderboard();

        new File(dbPath).delete();
        System.out.println("\nALL " + passed + " CHECKS PASSED");
    }

    private static void check(boolean condition, String what) {
        if (!condition) throw new AssertionError("FAILED: " + what);
        passed++;
    }

    private static void testLegacyMigration() throws Exception {
        try (Connection c = Database.connect(); Statement s = c.createStatement()) {
            // A players table as created by the version before passwords existed.
            s.execute("CREATE TABLE players (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE COLLATE NOCASE, " +
                "specialty TEXT NOT NULL, level INTEGER NOT NULL DEFAULT 1, xp INTEGER NOT NULL DEFAULT 0, hp INTEGER NOT NULL, " +
                "attack INTEGER NOT NULL, defense INTEGER NOT NULL, magic_attack INTEGER NOT NULL, magic_defense INTEGER NOT NULL, " +
                "speed INTEGER NOT NULL, ability TEXT NOT NULL, move1 TEXT NOT NULL, move2 TEXT NOT NULL, move3 TEXT NOT NULL, " +
                "move4 TEXT NOT NULL, wins INTEGER NOT NULL DEFAULT 0, losses INTEGER NOT NULL DEFAULT 0, " +
                "rating INTEGER NOT NULL DEFAULT 1000, gold INTEGER NOT NULL DEFAULT 50, potions INTEGER NOT NULL DEFAULT 2, " +
                "is_bot INTEGER NOT NULL DEFAULT 0, created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')), " +
                "last_played TEXT NOT NULL DEFAULT (datetime('now','localtime')))");
            s.execute("CREATE TABLE player_stats (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, specialty TEXT, " +
                "level INTEGER, xp INTEGER, hp INTEGER, attack INTEGER, defense INTEGER, magic_attack INTEGER, " +
                "magic_defense INTEGER, speed INTEGER, ability TEXT, move1 TEXT, move2 TEXT, move3 TEXT, move4 TEXT)");
            s.execute("INSERT INTO player_stats (name, specialty, level, xp, hp, attack, defense, magic_attack, magic_defense, speed, ability, move1, move2, move3, move4) VALUES " +
                "('Oldie','Paladin',1,0,5,25,10,4,4,2,'Divine Strike : Deal 1.5x','Divine Strike — 1.5x','Judgment — x','Smite — x','Lay on Hands — x')," +
                "('Oldie','Paladin',23,3987,25,45,40,34,4,122,'Divine Strike : Deal 1.5x','Divine Strike — 1.5x','Judgment — x','Smite — x','Lay on Hands — x')," +
                "('Broken','Mage',2,0,1,1,1,1,1,1,'???','???','Fireball - x',NULL,'Meteor Storm - x')");
        }
        quietly(Database::init);

        Player oldie = Database.loadPlayer("oldie");
        check(oldie != null, "legacy hero migrated (case-insensitive lookup)");
        check(oldie.level == 23 && oldie.xp == 3987, "newest legacy row kept");
        check(oldie.moves[1].name().equals("Judgment") && oldie.ability.equals("Divine Strike"), "legacy move/ability strings parsed");
        check(oldie.isValid(), "migrated hero valid");
        check(Database.getPasswordHash("Oldie") == null, "password column added to old table; migrated hero has no password yet");

        Player broken = Database.loadPlayer("Broken");
        check(broken != null && broken.isValid(), "broken legacy row repaired into a valid hero");
        check(broken.moves[0].name().equals("Fireball"), "known move kept, locked/unknown moves replaced");

        try (Connection c = Database.connect(); Statement s = c.createStatement()) {
            ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM sqlite_master WHERE name = 'player_stats'");
            check(rs.next() && rs.getInt(1) == 0, "legacy table renamed so migration runs once");
        }
        Database.deletePlayer("Oldie");
        quietly(Database::init);
        check(Database.loadPlayer("Oldie") == null, "deleted migrated hero does not come back");
        Database.deletePlayer("Broken");
    }

    private static void testSchemaAndBots() {
        quietly(Database::init);
        quietly(Database::init); // idempotent
        int bots = 0;
        for (Player p : Database.listOpponents(Database.makeBot("Nobody", "Mage", 1))) {
            check(p.isValid(), "seeded hero valid: " + p.name);
            if (p.bot) bots++;
        }
        check(bots == 8, "8 arena bots seeded exactly once (got " + bots + ")");
        check(!Database.deletePlayer("Nyx"), "bots cannot be deleted");
    }

    private static void testSaveLoadUpsertDelete() {
        Player p = Database.makeBot("Tester", "Rogue", 1);
        p.bot = false;
        p.gold = 77;
        p.potions = 3;
        check(Database.savePlayer(p), "insert hero");
        check(Database.nameExists("TESTER"), "name check is case-insensitive");

        p.level = 2;
        p.wins = 5;
        p.setStats(Player.buildGains(Character.recommendedBuild("Rogue"), Player.statPointsForLevel(2), 1));
        Database.savePlayer(p);
        Database.savePlayer(p);
        check(Database.listHumanHeroes().size() == 1, "upsert does not create duplicate rows");

        Player loaded = Database.loadPlayer("Tester");
        check(loaded.level == 2 && loaded.wins == 5 && loaded.gold == 77 && loaded.potions == 3, "fields round-trip");
        check(loaded.isValid(), "loaded hero valid");
        check(Database.loadLastPlayed().name.equals("Tester"), "last played hero loads");

        loaded.rating = 1234;
        Database.updateRecord(loaded);
        check(Database.loadPlayer("Tester").rating == 1234, "updateRecord persists rating");

        check(Database.deletePlayer("Tester"), "delete hero");
        check(Database.loadPlayer("Tester") == null && Database.loadLastPlayed() == null, "hero gone after delete");
    }

    private static void testStatBuilds() {
        for (String spec : Character.SPECIALTIES) {
            for (int level = 1; level <= 50; level++) {
                int points = Player.statPointsForLevel(level);
                int[] g = Player.buildGains(Character.recommendedBuild(spec), points, 1);
                int sum = 0;
                for (int v : g) {
                    sum += v;
                    check(v >= 1, "every stat gets the minimum");
                }
                check(sum == points, spec + " build spends exactly " + points);
            }
            int[] levelUp = Player.buildGains(Character.recommendedBuild(spec), LevelSystem.STAT_POINTS_PER_LEVEL, 0);
            int sum = 0;
            for (int v : levelUp) sum += v;
            check(sum == LevelSystem.STAT_POINTS_PER_LEVEL, "level-up build spends exactly 10");
        }
    }

    private static void testPasswords() {
        String h1 = Auth.hash("dragon42"), h2 = Auth.hash("dragon42");
        check(!h1.contains("dragon42"), "hash does not contain the password");
        check(!h1.equals(h2), "each hash gets its own salt");
        check(Auth.verify("dragon42", h1) && Auth.verify("dragon42", h2), "correct password verifies");
        check(!Auth.verify("Dragon42", h1) && !Auth.verify("", h1), "wrong password rejected");
        check(!Auth.verify("x", "") && !Auth.verify("x", null) && !Auth.verify("x", "garbage$1$2"), "malformed hashes never verify");

        Player p = Database.makeBot("Locked", "Mage", 1);
        p.bot = false;
        Database.savePlayer(p);
        check(Database.getPasswordHash("Locked") == null, "new row starts without a password");
        check(Database.setPasswordHash("Locked", h1), "password saved");
        p.gold = 999;
        Database.savePlayer(p);
        check(Auth.verify("dragon42", Database.getPasswordHash("LOCKED")), "password survives later saves");
        check(!Database.setPasswordHash("Nobody Here", h1), "setting a password for a missing hero fails");
        Database.deletePlayer("Locked");
    }

    private static void testElo() {
        check(Arena.ratingChange(1000, 1000, 1) == 16, "even match win = +16");
        check(Arena.ratingChange(1000, 1000, 0.5) == 0, "even match draw = 0");
        check(Arena.ratingChange(1400, 1000, 1) < 16, "beating weaker opponent gives less");
        check(Arena.ratingChange(1000, 1400, 1) > 16, "upset gives more");
    }

    private static void testLevelSystem() {
        Player p = Database.makeBot("Leveler", "Warrior", 1);
        quietly(() -> LevelSystem.grantXp(null, p, 100_000));
        check(p.level > 10, "big XP grant gives several levels (got " + p.level + ")");
        check(p.isValid(), "stats stay valid through level-ups");
        quietly(() -> LevelSystem.grantXp(null, p, 100_000_000));
        check(p.level == LevelSystem.MAX_LEVEL && p.xp == 0, "level caps at max");
        check(LevelSystem.xpReward(5, 5) > LevelSystem.xpReward(1, 5), "stronger opponents give more XP");
    }

    private static void testBattles() {
        Random rng = new Random(42);
        int[] wins = new int[2];
        int fights = 0, draws = 0;
        for (String specA : Character.SPECIALTIES) {
            for (String specB : Character.SPECIALTIES) {
                for (int rotation = 0; rotation < 10; rotation++) {
                    Player a = withRotatedMoves(Database.makeBot("A-" + specA, specA, 20), rotation);
                    Player b = withRotatedMoves(Database.makeBot("B-" + specB, specB, 20), rotation + 3);
                    a.ability = Character.abilityOptions(specA)[rotation % 3].name();
                    b.ability = Character.abilityOptions(specB)[(rotation + 1) % 3].name();
                    Combat.Result[] r = new Combat.Result[1];
                    quietly(() -> r[0] = Combat.fight(null, a, false, b, false, true, rng));
                    fights++;
                    check(r[0].rounds() >= 1 && r[0].rounds() <= Combat.MAX_ROUNDS, "battle terminates");
                    if (r[0].winner() == null) {
                        draws++;
                    } else {
                        check(r[0].loser() != null && r[0].loser() != r[0].winner(), "winner and loser are distinct");
                        wins[r[0].winner() == a ? 0 : 1]++;
                    }
                }
            }
        }
        // Low-level heroes with small stats also need to finish.
        for (int i = 0; i < 50; i++) {
            Player a = Database.makeBot("L1a", Character.SPECIALTIES[i % 4], 1);
            Player b = Database.makeBot("L1b", Character.SPECIALTIES[(i / 4) % 4], 1);
            Combat.Result[] r = new Combat.Result[1];
            quietly(() -> r[0] = Combat.fight(null, a, false, b, false, true, rng));
            check(r[0].rounds() <= Combat.MAX_ROUNDS, "level 1 battle terminates");
        }
        System.out.println("  battles: " + fights + " (A wins " + wins[0] + ", B wins " + wins[1] + ", draws " + draws + ")");
        check(draws < fights / 4, "draws are rare");
    }

    /** Gives a level-20 hero 4 moves starting at the given catalog index so every move gets used. */
    private static Player withRotatedMoves(Player p, int start) {
        Moves.Move[] all = Moves.catalogFor(p.speciality);
        for (int i = 0; i < 4; i++) p.moves[i] = all[(start + i) % all.length];
        return p;
    }

    private static void testHistoryAndLeaderboard() {
        Database.recordBattle("TRAINING", "Someone", "Orc Brute", "Someone", 5, 50, 0);
        String[] out = new String[1];
        out[0] = capture(() -> Database.showHistory("Someone", 10));
        check(out[0].contains("Orc Brute") && out[0].contains("WIN"), "battle history lists the battle");
        out[0] = capture(Database::showLeaderboard);
        check(out[0].contains("Seraphine Dawn"), "leaderboard lists heroes");
    }

    private static void quietly(Runnable r) {
        capture(r);
    }

    private static String capture(Runnable r) {
        PrintStream original = System.out;
        java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        System.setOut(new PrintStream(buf));
        try {
            r.run();
        } finally {
            System.setOut(original);
        }
        return buf.toString();
    }
}
