import java.util.Scanner;

public class LevelSystem {

    public static final int STAT_POINTS_PER_LEVEL = 10;

    public static int xpForLevel(int level) {
        return (int) (100 * Math.pow(level, 1.5));
    }

    public static void grantXp(Scanner scanner, int amount) {
        Player.xp += amount;
        System.out.println("\n+" + amount + " XP gained!");

        while (Player.xp >= xpForLevel(Player.setPlayerstats.level)) {
            Player.xp -= xpForLevel(Player.setPlayerstats.level);
            levelUp(scanner);
        }

        int needed = xpForLevel(Player.setPlayerstats.level) - Player.xp;
        System.out.println("XP: " + Player.xp + "/" + xpForLevel(Player.setPlayerstats.level) + " (" + needed + " to next level)");
    }

    private static void levelUp(Scanner scanner) {
        Player.setPlayerstats.level++;
        int lvl = Player.setPlayerstats.level;

        System.out.println("\n🎉 LEVEL UP! You reached Level " + lvl + "!");
        System.out.println("+" + STAT_POINTS_PER_LEVEL + " stat points to spend.");
        Player.setPlayerstats.allocateNewPoints(scanner, STAT_POINTS_PER_LEVEL);

        checkMoveUnlock(lvl);
        checkUltimateUnlock(lvl);
    }

    private static void checkMoveUnlock(int level) {
        if (level % 5 == 0) {
            String move = "Move Lvl " + level;
            Player.unlockedMoves.add(move);
            System.out.println("NEW MOVE unlocked: " + move);
        }
    }

    private static void checkUltimateUnlock(int level) {
        if (level % 10 == 0) {
            String ultimate = "Ultimate Lvl " + level;
            Player.unlockedUltimates.add(ultimate);
            System.out.println("ULTIMATE ABILITY unlocked: " + ultimate);
        }
    }

    public static double enemyDifficultyMultiplier(int playerLevel) {
        return 1.0 + (playerLevel - 1) * 0.15;
    }
}
