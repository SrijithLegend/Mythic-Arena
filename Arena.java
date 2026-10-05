import java.util.List;
import java.util.Random;
import java.util.Scanner;

/** Game modes built on top of the combat engine: training, ranked arena and local duels. */
public class Arena {

    public static final int ELO_K = 32;

    private static final Random RNG = new Random();

    private static final String[][] MONSTERS = {
        {"Orc Brute", "Warrior"}, {"Troll Berserker", "Warrior"},
        {"Goblin Shaman", "Mage"}, {"Lich Acolyte", "Mage"},
        {"Shadow Wolf", "Rogue"}, {"Bandit Cutthroat", "Rogue"},
        {"Skeleton Knight", "Paladin"}, {"Fallen Templar", "Paladin"}
    };

    // ------------------------------------------------------------------ PvE

    public static void training(Scanner scanner, Player hero) {
        String[] difficulties = {
            "Easy    (monster 2 levels below you)",
            "Normal  (monster at your level)",
            "Hard    (monster 2 levels above you)",
            "Boss    (monster 4 levels above you, double gold)"
        };
        int[] offsets = {-2, 0, 2, 4};
        int choice = Input.choose(scanner, "Training Grounds - Choose Difficulty", difficulties);

        String[] template = MONSTERS[RNG.nextInt(MONSTERS.length)];
        int level = Math.max(1, hero.level + offsets[choice]);
        String name = (choice == 3 ? "Elder " : "") + template[0];
        Player monster = Database.makeBot(name, template[1], level);

        System.out.println("\nA wild " + monster.name + " (Lv" + level + " " + monster.speciality + ") appears!");
        Combat.Result result = Combat.fight(scanner, hero, true, monster, false, true, RNG);

        int xp = 0;
        if (result.winner() == hero) {
            xp = LevelSystem.xpReward(level, hero.level);
            int gold = (10 + 5 * level) * (choice == 3 ? 2 : 1);
            hero.gold += gold;
            System.out.println("You earned " + gold + " gold.");
            LevelSystem.grantXp(scanner, hero, xp);
        } else {
            System.out.println("You retreat to lick your wounds. (No XP gained)");
        }
        Database.savePlayer(hero);
        Database.recordBattle("TRAINING", hero.name, monster.name, result.winner() == null ? null : result.winner().name,
            result.rounds(), xp, 0);
        System.out.println("Progress saved.");
    }

    // ------------------------------------------------------------------ PvP

    public static void lobby(Scanner scanner, Player hero) {
        while (true) {
            System.out.println("\n========== MULTIPLAYER ARENA ==========");
            System.out.println("  " + hero.name + "  |  Rating " + hero.rating + "  |  " + hero.wins + "W-" + hero.losses + "L");
            System.out.println("  1. View Fighters (Lobby)");
            System.out.println("  2. Inspect a Fighter");
            System.out.println("  3. Ranked Challenge (opponent fights with AI)");
            System.out.println("  4. Local Duel (2 players, same keyboard)");
            System.out.println("  0. Leave Arena");
            switch (Input.readInt(scanner, "Your choice: ", 0, 4)) {
                case 1 -> printLobby(Database.listOpponents(hero));
                case 2 -> {
                    Player target = pickOpponent(scanner, Database.listOpponents(hero));
                    if (target != null) target.display();
                }
                case 3 -> {
                    Player target = pickOpponent(scanner, Database.listOpponents(hero));
                    if (target != null) rankedBattle(scanner, hero, target);
                }
                case 4 -> localDuel(scanner, hero);
                case 0 -> { return; }
            }
        }
    }

    private static void printLobby(List<Player> fighters) {
        System.out.println();
        System.out.printf("%-4s %-22s %-9s %-6s %-7s %s%n", "#", "NAME", "CLASS", "LEVEL", "RATING", "W-L");
        System.out.println("---------------------------------------------------------------");
        for (int i = 0; i < fighters.size(); i++) {
            Player p = fighters.get(i);
            System.out.printf("%-4d %-22s %-9s %-6d %-7d %d-%d%n", i + 1, p.name + (p.bot ? " (bot)" : ""),
                p.speciality, p.level, p.rating, p.wins, p.losses);
        }
        if (fighters.isEmpty()) System.out.println("Nobody else is in the arena yet.");
    }

    private static Player pickOpponent(Scanner scanner, List<Player> fighters) {
        if (fighters.isEmpty()) {
            System.out.println("Nobody else is in the arena yet.");
            return null;
        }
        printLobby(fighters);
        int choice = Input.readInt(scanner, "Choose a fighter (1-" + fighters.size() + ", 0 to cancel): ", 0, fighters.size());
        return choice == 0 ? null : fighters.get(choice - 1);
    }

    /** Elo rating change for the first player given the score (1 win, 0.5 draw, 0 loss). */
    public static int ratingChange(int rating, int opponentRating, double score) {
        double expected = 1.0 / (1.0 + Math.pow(10, (opponentRating - rating) / 400.0));
        return (int) Math.round(ELO_K * (score - expected));
    }

    private static void rankedBattle(Scanner scanner, Player hero, Player defender) {
        System.out.println("\nYou challenge " + defender.name + "! " +
            (defender.bot ? "" : "(their hero is controlled by the AI while they're away)"));
        Combat.Result result = Combat.fight(scanner, hero, true, defender, false, true, RNG);

        double score = result.winner() == hero ? 1 : result.winner() == null ? 0.5 : 0;
        int change = ratingChange(hero.rating, defender.rating, score);
        hero.rating += change;
        defender.rating -= change;

        int xp = 0;
        if (score == 1) {
            hero.wins++;
            defender.losses++;
            xp = (int) (LevelSystem.xpReward(defender.level, hero.level) * 1.25);
            int gold = 20 + 5 * defender.level;
            hero.gold += gold;
            System.out.println("Victory! Rating " + hero.rating + " (+" + change + "), +" + gold + " gold.");
            LevelSystem.grantXp(scanner, hero, xp);
        } else if (score == 0) {
            hero.losses++;
            defender.wins++;
            System.out.println("Defeat. Rating " + hero.rating + " (" + change + ").");
        } else {
            System.out.println("Draw. Rating " + hero.rating + " (" + (change >= 0 ? "+" : "") + change + ").");
        }

        Database.updateRecord(defender);
        Database.savePlayer(hero);
        Database.recordBattle("RANKED", hero.name, defender.name, result.winner() == null ? null : result.winner().name,
            result.rounds(), xp, change);
        System.out.println("Results saved.");
    }

    private static void localDuel(Scanner scanner, Player hero) {
        List<Player> others = Database.listHumanHeroes();
        others.removeIf(p -> p.name.equalsIgnoreCase(hero.name));
        if (others.isEmpty()) {
            System.out.println("\nThere is no other player hero to duel. Have your friend create one from the main menu first!");
            return;
        }
        System.out.println("\nPlayer 2, pick your hero:");
        Player rival = pickOpponent(scanner, others);
        if (rival == null) return;
        System.out.println("Player 2 must log in as " + rival.name + ".");
        if (!Auth.login(scanner, rival)) {
            System.out.println("Duel cancelled.");
            return;
        }

        System.out.println("\nLOCAL DUEL: " + hero.name + " (Player 1) vs " + rival.name + " (Player 2)");
        Combat.Result result = Combat.fight(scanner, hero, true, rival, true, true, RNG);

        int change = 0, xp = 0;
        if (result.winner() == null) {
            change = ratingChange(hero.rating, rival.rating, 0.5);
            hero.rating += change;
            rival.rating -= change;
        } else {
            Player winner = result.winner(), loser = result.loser();
            int winnerChange = ratingChange(winner.rating, loser.rating, 1);
            winner.rating += winnerChange;
            loser.rating -= winnerChange;
            winner.wins++;
            loser.losses++;
            change = winner == hero ? winnerChange : -winnerChange;
            xp = LevelSystem.xpReward(loser.level, winner.level);
            int gold = 15 + 5 * loser.level;
            winner.gold += gold;
            System.out.println(winner.name + " earns " + gold + " gold.");
            LevelSystem.grantXp(scanner, winner, xp);
        }
        Database.savePlayer(rival);
        Database.savePlayer(hero);
        Database.recordBattle("DUEL", hero.name, rival.name, result.winner() == null ? null : result.winner().name,
            result.rounds(), result.winner() == hero ? xp : 0, change);
        System.out.println("Results saved for both players.");
    }
}
