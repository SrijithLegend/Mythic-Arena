import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n===== MYSTIC ARENA =====");
            System.out.println("  1. Create Hero");
            System.out.println("  2. View Hero");
            System.out.println("  3. Gain XP");
            System.out.println("  4. Load Game");
            System.out.println("  0. Exit");

            int choice = readInt(scanner, "Your choice: ");
            switch (choice) {
                case 1 -> createHero(scanner);
                case 2 -> Player.displayPlayerStats();
                case 3 -> gainXp(scanner);
                case 4 -> {
                    System.out.print("Enter character name to load: ");
                    String loadName = scanner.nextLine();
                    
                    if (Database.loadPlayer(loadName)) {
                        Player.displayPlayerStats(); 
                    }
                }
                case 0 -> running = false;
                default -> System.out.println("Unknown option.");
            }
        }
        System.out.println("\nThanks for playing MYSTIC ARENA. Goodbye!");
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
}