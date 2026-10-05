import java.util.List;
import java.util.Scanner;

public class Main {

    public static final int POTION_PRICE = 30;
    public static final int RESPEC_PRICE = 100;

    private static Player hero;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            Database.init();
        } catch (IllegalStateException e) {
            System.out.println("[FATAL] " + e.getMessage());
            System.out.println("Make sure sqlite-jdbc-3.53.2.1.jar is on the classpath (use run.bat / run.sh).");
            return;
        }

        System.out.println("\n  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("           M Y S T I C   A R E N A");
        System.out.println("  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        try {
            Player last = Database.loadLastPlayed();
            if (last == null) {
                System.out.println("\nNo saved hero found. Choose 'Create New Hero' to begin.");
            } else {
                System.out.println("\nLast played: " + last.name + " the " + last.speciality);
                if (Auth.login(scanner, last)) {
                    hero = last;
                    System.out.println("Welcome back, " + hero.name + "!");
                } else {
                    System.out.println("Not logged in. Use 'Switch Hero' or 'Create New Hero'.");
                }
            }
            mainMenu(scanner);
        } catch (Input.InputClosedException e) {
            System.out.println("\nInput closed.");
        }
        if (hero != null) Database.savePlayer(hero);
        System.out.println("\nThanks for playing MYSTIC ARENA. Goodbye!");
    }

    private static void mainMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n============ MYSTIC ARENA ============");
            System.out.println(hero == null ? "  (no hero loaded)"
                : "  " + hero.name + " | Lv" + hero.level + " " + hero.speciality + " | " + hero.gold + " gold | Rating " + hero.rating);
            System.out.println("--------------------------------------");
            System.out.println("  1. Create New Hero");
            System.out.println("  2. Switch Hero");
            System.out.println("  3. View Hero");
            System.out.println("  4. Training Grounds (PvE)");
            System.out.println("  5. Multiplayer Arena (PvP)");
            System.out.println("  6. Manage Hero (moves, ability, password)");
            System.out.println("  7. Shop");
            System.out.println("  8. Battle History");
            System.out.println("  9. Leaderboard");
            System.out.println(" 10. Delete Hero");
            System.out.println("  0. Exit");

            int choice = Input.readInt(scanner, "Your choice: ", 0, 10);
            if (choice == 0) return;
            if (choice >= 3 && choice <= 8 || choice == 10) {
                if (hero == null) {
                    System.out.println("\n[!] Create or load a hero first.");
                    continue;
                }
            }
            switch (choice) {
                case 1 -> createHero(scanner);
                case 2 -> switchHero(scanner);
                case 3 -> hero.display();
                case 4 -> Arena.training(scanner, hero);
                case 5 -> Arena.lobby(scanner, hero);
                case 6 -> manageHero(scanner);
                case 7 -> shop(scanner);
                case 8 -> Database.showHistory(hero.name, 15);
                case 9 -> Database.showLeaderboard();
                case 10 -> deleteHero(scanner);
            }
        }
    }

    private static void createHero(Scanner scanner) {
        Player created = Player.createInteractive(scanner);
        if (created == null) return;
        String passwordHash = Auth.createPassword(scanner, created.name);
        created.display();
        if (Database.savePlayer(created) && Database.setPasswordHash(created.name, passwordHash)) {
            hero = created;
            System.out.println("\n" + hero.name + " has entered the world and is saved to the database!");
        }
    }

    private static void switchHero(Scanner scanner) {
        List<Player> heroes = Database.listHumanHeroes();
        if (heroes.isEmpty()) {
            System.out.println("\nNo heroes saved yet.");
            return;
        }
        System.out.println("\n--- Saved Heroes ---");
        for (int i = 0; i < heroes.size(); i++) {
            Player p = heroes.get(i);
            String current = hero != null && p.name.equalsIgnoreCase(hero.name) ? "  <- current" : "";
            System.out.printf("  %d. %-18s Lv%-3d %-8s Rating %d%s%n", i + 1, p.name, p.level, p.speciality, p.rating, current);
        }
        int choice = Input.readInt(scanner, "Load which hero? (0 to cancel): ", 0, heroes.size());
        if (choice == 0) return;
        Player chosen = heroes.get(choice - 1);
        if (hero != null && chosen.name.equalsIgnoreCase(hero.name)) {
            System.out.println("\n" + hero.name + " is already loaded.");
            return;
        }
        if (!Auth.login(scanner, chosen)) {
            System.out.println("Could not log in as " + chosen.name + ".");
            return;
        }
        if (hero != null) Database.savePlayer(hero);
        hero = chosen;
        Database.savePlayer(hero); // marks it as the most recently played
        System.out.println("\nLoaded " + hero.name + " the " + hero.speciality + ". Welcome back!");
    }

    private static void manageHero(Scanner scanner) {
        System.out.println("\nCurrent moves:");
        for (int i = 0; i < hero.moves.length; i++) {
            System.out.println("  " + (i + 1) + ". " + hero.moves[i].description());
        }
        System.out.println("Signature ability: " + hero.ability);

        String[] options = {"Change moves", "Change signature ability", "Change password", "Back"};
        switch (Input.choose(scanner, "Manage Hero", options)) {
            case 0 -> {
                hero.moves = Moves.chooseMoves(scanner, hero.speciality, hero.level);
                Database.savePlayer(hero);
                System.out.println("Moveset saved.");
            }
            case 1 -> {
                hero.ability = Character.chooseAbility(scanner, hero.speciality);
                Database.savePlayer(hero);
                System.out.println("Ability saved.");
            }
            case 2 -> Auth.changePassword(scanner, hero);
            default -> { }
        }
    }

    private static void shop(Scanner scanner) {
        while (true) {
            System.out.println("\n=============== SHOP ===============");
            System.out.println("  Gold: " + hero.gold + "   Potions: " + hero.potions);
            System.out.println("  1. Health Potion  (" + POTION_PRICE + " gold) - heals " + (int) (Combat.POTION_HEAL * 100)
                + "% HP in battle, max " + Combat.MAX_POTIONS_PER_BATTLE + " per battle");
            System.out.println("  2. Scroll of Respec (" + RESPEC_PRICE + " gold) - reallocate all your stat points");
            System.out.println("  0. Leave");
            int choice = Input.readInt(scanner, "Buy: ", 0, 2);
            if (choice == 0) return;

            int price = choice == 1 ? POTION_PRICE : RESPEC_PRICE;
            if (hero.gold < price) {
                System.out.println("  -> Not enough gold! Win some battles first.");
                continue;
            }
            if (choice == 1) {
                int max = hero.gold / POTION_PRICE;
                int qty = Input.readInt(scanner, "How many? (1-" + max + "): ", 1, max);
                hero.gold -= qty * POTION_PRICE;
                hero.potions += qty;
                System.out.println("Bought " + qty + " potion(s).");
            } else {
                hero.gold -= RESPEC_PRICE;
                int[] zero = new int[Player.STAT_NAMES.length];
                hero.setStats(hero.spendPoints(scanner, zero, Player.statPointsForLevel(hero.level), 1));
                System.out.println("Your stats have been reforged.");
            }
            Database.savePlayer(hero);
        }
    }

    private static void deleteHero(Scanner scanner) {
        if (!Input.confirm(scanner, "PERMANENTLY delete " + hero.name + "?")) {
            System.out.println("Deletion cancelled.");
            return;
        }
        System.out.println("Enter the password to confirm.");
        if (!Auth.login(scanner, hero)) {
            System.out.println("Deletion cancelled.");
            return;
        }
        if (Database.deletePlayer(hero.name)) {
            System.out.println(hero.name + " has been deleted.");
            hero = null;
        }
    }
}
