import java.util.Scanner;

public class LevelSystem {

    public static final int STAT_POINTS_PER_LEVEL = 10;
    public static final int MAX_LEVEL = 50;

    public static int xpForLevel(int level) {
        return (int) (100 * Math.pow(level, 1.5));
    }

    /** XP for beating an opponent of the given level. */
    public static int xpReward(int opponentLevel, int ownLevel) {
        int base = (int) (30 * Math.pow(opponentLevel, 1.3)) + 20;
        double diff = 1.0 + 0.1 * (opponentLevel - ownLevel);
        return Math.max(10, (int) (base * Math.max(0.3, Math.min(diff, 2.0))));
    }

    /**
     * Adds XP and handles level-ups. With a scanner the player spends their new points
     * interactively; with null (bots, tests) the recommended build is used.
     */
    public static void grantXp(Scanner scanner, Player player, int amount) {
        if (player.level >= MAX_LEVEL) {
            System.out.println(player.name + " is at the maximum level (" + MAX_LEVEL + ").");
            return;
        }
        player.xp += amount;
        System.out.println("\n+" + amount + " XP gained!");

        while (player.level < MAX_LEVEL && player.xp >= xpForLevel(player.level)) {
            player.xp -= xpForLevel(player.level);
            levelUp(scanner, player);
        }
        if (player.level >= MAX_LEVEL) {
            player.xp = 0;
            System.out.println("MAX LEVEL reached!");
        } else {
            int needed = xpForLevel(player.level) - player.xp;
            System.out.println("XP: " + player.xp + "/" + xpForLevel(player.level) + " (" + needed + " to next level)");
        }
    }

    private static void levelUp(Scanner scanner, Player player) {
        player.level++;
        System.out.println("\n*** LEVEL UP! " + player.name + " reached Level " + player.level + "! ***");
        System.out.println("+" + STAT_POINTS_PER_LEVEL + " stat points to spend.");

        if (scanner == null) {
            int[] gains = Player.buildGains(Character.recommendedBuild(player.speciality), STAT_POINTS_PER_LEVEL, 0);
            int[] stats = player.stats();
            for (int i = 0; i < stats.length; i++) stats[i] += gains[i];
            player.setStats(stats);
        } else {
            player.setStats(player.spendPoints(scanner, player.stats(), STAT_POINTS_PER_LEVEL, 0));
        }

        for (Moves.Move m : Moves.catalogFor(player.speciality)) {
            if (m.unlockLevel() == player.level) {
                String kind = m.unlockLevel() >= Moves.MASTER_LEVEL ? "MASTER MOVE" : "ULTIMATE MOVE";
                System.out.println(kind + " unlocked: " + m.name() + "! Equip it from 'Manage Moves'.");
            }
        }
    }
}
