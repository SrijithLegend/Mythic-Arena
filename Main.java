import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        if (Database.loadLatestPlayer()) {
            System.out.println("\nWelcome back, " + Player.name + "!");
        } else {
            System.out.println("\nNo saved character found. Choose 'Create Hero' to begin.");
        }

        while (running) {
            System.out.println("\n===== MYSTIC ARENA =====");
            System.out.println("  1. Create Hero");
            System.out.println("  2. View Hero");
            System.out.println("  3. Gain XP");
            System.out.println("  4. Delete Character");
            System.out.println("  5. View Leaderboard");
            System.out.println("  6. Enter Multiplayer Arena");
            System.out.println("  0. Exit");

            int choice = readInt(scanner, "Your choice: ");
            switch (choice) {
                case 1 -> createHero(scanner);
                case 2 -> Player.displayPlayerStats();
                case 3 -> gainXp(scanner);
                case 4 -> deleteHero(scanner);
                case 5 -> Database.showLeaderboard();
                case 6 -> multiplayerArena(scanner);
                case 0 -> running = false;
                default -> System.out.println("Unknown option.");
            }
        }
        System.out.println("\nThanks for playing MYSTIC ARENA. Goodbye!");
    }

    private static void deleteHero(Scanner scanner) {
        if (Player.name == null) {
            System.out.println("No character to delete.");
            return;
        }
        System.out.print("Type your character's name (" + Player.name + ") to confirm deletion: ");
        String confirm = scanner.nextLine();
        if (!confirm.equals(Player.name)) {
            System.out.println("Name doesn't match. Deletion cancelled.");
            return;
        }
        Database.deletePlayer(Player.name);
        Player.resetPlayer();
        System.out.println("Character deleted.");
    }
    private static void createHero(Scanner scanner) {
        Player.setPlayerName(scanner);
        Player.setPlayerSpeciality(scanner);
        Player.setPlayerAbility(scanner);
        Player.setPlayerMoves(scanner);        
        Player.setPlayerstats.setPlayerStats(scanner);

        if (Player.validateHero()) {
            Player.displayPlayerStats();

            Database.savePlayer(
                Player.name,
                Player.speciality,
                Player.setPlayerstats.level,
                Player.xp,
                Player.setPlayerstats.hp,
                Player.setPlayerstats.attack,
                Player.setPlayerstats.defense,
                Player.setPlayerstats.magicAttack,
                Player.setPlayerstats.magicDefense,
                Player.setPlayerstats.speed,
                Player.ability,
                Moves.selectedMoves[0].description(),
                Moves.selectedMoves[1].description(),
                Moves.selectedMoves[2].description(),
                Moves.selectedMoves[3].description()
            );
        } else {
            System.out.println("Hero creation failed validation. Try again.");
        }
    }

    private static void gainXp(Scanner scanner) {
        if (Player.name == null) {
            System.out.println("Create a hero first.");
            return;
        }
        int amount = readInt(scanner, "How much XP to gain? ");
        LevelSystem.grantXp(scanner, amount);
    }

    private static int readInt(Scanner scanner, String prompt) {
        int value = -1;
        boolean valid = false;
        while (!valid) {
            System.out.print(prompt);
            if (scanner.hasNextInt()) {
                value = scanner.nextInt();
                scanner.nextLine();
                valid = true;
            } else {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine();
            }
        }
        return value;
    }

    // Add this near the bottom of Main.java
    private static void multiplayerArena(Scanner scanner) {
        if (Player.name == null) {
            System.out.println("\n[!] You must Create or Load a hero first before entering the Arena!");
            return;
        }

        boolean inArena = true;
        while (inArena) {
            System.out.println("\n⚔️ ===== MULTIPLAYER ARENA ===== ⚔️");
            System.out.println("  1. View Online Fighters (Lobby)");
            System.out.println("  2. Inspect Fighter Stats");
            System.out.println("  3. Challenge Fighter");
            System.out.println("  0. Leave Arena");

            int choice = readInt(scanner, "Your choice: ");
            switch (choice) {
                case 1 -> Database.showLeaderboard(); // Reuses your leaderboard code!
                case 2 -> {
                    System.out.print("Enter the name of the fighter to inspect: ");
                    String target = scanner.nextLine();
                    Database.inspectPlayer(target);
                }
                case 3 -> {
                    System.out.print("Enter the name of the fighter to challenge: ");
                    String target = scanner.nextLine();
                    
                    if (target.equalsIgnoreCase(Player.name)) {
                        System.out.println("\n[!] You can't challenge yourself!");
                    } else {
                        // For now, this is just a placeholder until we build the combat loop
                        System.out.println("\n🔥 You have challenged " + target + " to a battle! 🔥");
                        System.out.println("(Battle mechanics linking database opponents coming soon...)");
                    }
                }
                case 0 -> inArena = false;
                default -> System.out.println("Unknown option.");
            }
        }
    }
}