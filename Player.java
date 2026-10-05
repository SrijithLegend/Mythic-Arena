import java.util.Scanner;

/** A hero stored in the database: either a real player or an arena bot. */
public class Player {

    public static final String[] STAT_NAMES = {"HP", "Attack", "Defense", "Magic Attack", "Magic Defense", "Speed"};
    public static final int STARTING_RATING = 1000;
    public static final int STARTING_GOLD = 50;
    public static final int STARTING_POTIONS = 2;

    public String name;
    public String speciality;
    public String ability;

    public int level = 1;
    public int xp = 0;

    public int hp, attack, defense, magicAttack, magicDefense, speed;

    public Moves.Move[] moves;

    public int wins = 0, losses = 0;
    public int rating = STARTING_RATING;
    public int gold = STARTING_GOLD;
    public int potions = STARTING_POTIONS;
    public boolean bot = false;

    public static int statPointsForLevel(int level) {
        return 50 + (level - 1) * LevelSystem.STAT_POINTS_PER_LEVEL;
    }

    public int[] stats() {
        return new int[] {hp, attack, defense, magicAttack, magicDefense, speed};
    }

    public void setStats(int[] s) {
        hp = s[0]; attack = s[1]; defense = s[2]; magicAttack = s[3]; magicDefense = s[4]; speed = s[5];
    }

    public int totalStats() {
        int total = 0;
        for (int s : stats()) total += s;
        return total;
    }

    /** Battle HP derived from the HP stat. */
    public int maxBattleHp() {
        return 60 + hp * 9 + level * 12;
    }

    // ------------------------------------------------------------------ creation

    public static Player createInteractive(Scanner scanner) {
        Player p = new Player();
        p.name = chooseName(scanner);
        if (p.name == null) return null;

        int spec = Input.choose(scanner, "Choose Your Specialty", Character.SPECIALTY_BLURBS);
        p.speciality = Character.SPECIALTIES[spec];
        System.out.println("Specialty set to: " + p.speciality);

        p.ability = Character.chooseAbility(scanner, p.speciality);
        p.moves = Moves.chooseMoves(scanner, p.speciality, p.level);

        int[] base = new int[STAT_NAMES.length];
        p.setStats(p.spendPoints(scanner, base, statPointsForLevel(1), 1));

        if (!p.isValid()) {
            System.out.println("Hero creation failed validation. Try again.");
            return null;
        }
        return p;
    }

    private static String chooseName(Scanner scanner) {
        while (true) {
            String name = Input.readLine(scanner, "What is your hero's name? (blank to cancel) ");
            if (name.isEmpty()) return null;
            if (!name.matches("[A-Za-z0-9 _'-]{2,16}")) {
                System.out.println("  -> Names must be 2-16 characters (letters, numbers, spaces, _ ' -).");
            } else if (Database.nameExists(name)) {
                System.out.println("  -> A hero named '" + name + "' already exists. Pick another name.");
            } else {
                return name;
            }
        }
    }

    /**
     * Distributes points on top of the given base stats, either automatically using the
     * class's recommended build or manually. Returns the new stat array.
     */
    public int[] spendPoints(Scanner scanner, int[] base, int points, int minEach) {
        String[] options = {"Use the recommended " + speciality + " build", "Allocate manually"};
        int choice = Input.choose(scanner, "Spend " + points + " Stat Points", options);
        int[] gains = choice == 0
            ? buildGains(Character.recommendedBuild(speciality), points, minEach)
            : allocateManually(scanner, points, minEach);

        int[] result = new int[base.length];
        for (int i = 0; i < base.length; i++) result[i] = base[i] + gains[i];
        System.out.println("\nStat points allocated!");
        return result;
    }

    /** Splits points by percentage weights, guaranteeing minEach per stat and an exact total. */
    public static int[] buildGains(int[] weights, int points, int minEach) {
        int[] gains = new int[weights.length];
        int spent = 0;
        for (int i = 0; i < weights.length; i++) {
            gains[i] = Math.max(minEach, points * weights[i] / 100);
            spent += gains[i];
        }
        // Fix rounding: hand leftovers to the heaviest-weighted stats, or take back from them.
        int i = 0;
        while (spent != points) {
            int idx = heaviest(weights, i++ % weights.length);
            if (spent < points) {
                gains[idx]++;
                spent++;
            } else if (gains[idx] > minEach) {
                gains[idx]--;
                spent--;
            }
        }
        return gains;
    }

    private static int heaviest(int[] weights, int rank) {
        Integer[] order = new Integer[weights.length];
        for (int i = 0; i < order.length; i++) order[i] = i;
        java.util.Arrays.sort(order, (a, b) -> weights[b] - weights[a]);
        return order[rank];
    }

    private static int[] allocateManually(Scanner scanner, int points, int minEach) {
        while (true) {
            int[] gains = new int[STAT_NAMES.length];
            int remaining = points;
            System.out.println("\nPoints to spend: " + points + (minEach > 0 ? " (every stat needs at least " + minEach + ")" : ""));
            for (int i = 0; i < STAT_NAMES.length; i++) {
                int statsLeft = STAT_NAMES.length - i - 1;
                int max = remaining - statsLeft * minEach;
                if (i == STAT_NAMES.length - 1) {
                    gains[i] = remaining;
                    System.out.println(String.format("%-15s gets the remaining %d point(s).", STAT_NAMES[i], remaining));
                } else {
                    String prompt = String.format("Enter %-15s (Points Left: %d, %d-%d): ", STAT_NAMES[i], remaining, minEach, max);
                    gains[i] = Input.readInt(scanner, prompt, minEach, max);
                }
                remaining -= gains[i];
            }
            if (Input.confirm(scanner, "Confirm this allocation?")) return gains;
        }
    }

    public boolean isValid() {
        if (name == null || name.isBlank() || speciality == null || ability == null) return false;
        if (moves == null || moves.length != 4) return false;
        for (Moves.Move m : moves) {
            if (m == null || m.unlockLevel() > level) return false;
        }
        for (int s : stats()) {
            if (s < 0) return false;
        }
        return totalStats() == statPointsForLevel(level);
    }

    // ------------------------------------------------------------------ display

    public void display() {
        System.out.println("\n=================================");
        System.out.println("  " + name + (bot ? " [BOT]" : ""));
        System.out.println("=================================");
        System.out.println("Specialty: " + speciality + "   Level: " + level);
        System.out.println("XP: " + xp + " / " + LevelSystem.xpForLevel(level));
        System.out.println("Rating: " + rating + "   Record: " + wins + "W - " + losses + "L");
        System.out.println("Gold: " + gold + "   Health Potions: " + potions);
        System.out.println("---------------------------------");
        int[] s = stats();
        for (int i = 0; i < s.length; i++) {
            System.out.printf("%-15s %d%n", STAT_NAMES[i] + ":", s[i]);
        }
        System.out.println("Total Stats:    " + totalStats() + "   (Battle HP: " + maxBattleHp() + ")");
        System.out.println("Signature:      " + ability);
        System.out.println("Moves:");
        for (int i = 0; i < moves.length; i++) {
            System.out.println("  " + (i + 1) + ". " + moves[i].description());
        }
    }
}
