import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n===== RPG SIMULATOR =====");
            System.out.println("  1. Create Hero");
            System.out.println("  2. View Hero");
            System.out.println("  3. Gain XP");
            System.out.println("  0. Exit");

            int choice = readInt(scanner, "Your choice: ");
            switch (choice) {
                case 1 -> createHero(scanner);
                case 2 -> Player.displayPlayerStats();
                case 3 -> gainXp(scanner);
                case 0 -> running = false;
                default -> System.out.println("Unknown option.");
            }
        }
        System.out.println("\nThanks for playing the RPG Simulator. Goodbye!");
    }

    private static void createHero(Scanner scanner) {
        Player.setPlayerName(scanner);
        Player.setPlayerSpeciality(scanner);
        Player.setPlayerAbility(scanner);
        Player.setPlayerstats.setPlayerStats(scanner);

        if (Player.validateHero()) {
            Player.displayPlayerStats();
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