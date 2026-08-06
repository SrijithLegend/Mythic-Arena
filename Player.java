import java.util.Scanner;

public class Player {

    public static String name;

    public static String speciality;

    public static int level = 1;
    public static int hp;
    public static int attack;
    public static int defense;
    public static int magic_attack;
    public static int magic_defense;
    public static int speed;
    public static String ability;
    public static int total_stats;

    public static void setPlayerName(Scanner scanner) {
        System.out.print("What is your name? ");
        name = scanner.nextLine();
    }

    public static void setPlayerSpeciality(Scanner scanner) {

        String[] options = {"Warrior", "Mage", "Rogue", "Paladin"};

        System.out.println("\n--- Choose Your Specialty ---");
        for (int i = 0; i < options.length; i++) {
            System.out.println((i + 1) + ". " + options[i]);
        }

        int choice = -1;

        while (choice < 1 || choice > options.length) {
            System.out.print("Enter choice (1-" + options.length + "): ");

            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); 

                if (choice < 1 || choice > options.length) {
                    System.out.println("Invalid selection. Please choose between 1 and " + options.length + ".");
                }
            } else {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine(); 
            }
        }

        speciality = options[choice - 1];
        System.out.println("Specialty set to: " + speciality + "\n");
    }

    public static void setPlayerAbility(Scanner scanner) {

        String[] ability_options = {"Warrior", "Mage", "Rogue", "Paladin"};

        System.out.println("\n--- Choose Your Ability ---");
        for (int i = 0; i < ability_options.length; i++) {
            System.out.println((i + 1) + ". " + ability_options[i]);
        }

        int choice = -1;

        while (choice < 1 || choice > ability_options.length) {
            System.out.print("Enter choice (1-" + ability_options.length + "): ");

            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); 

                if (choice < 1 || choice > ability_options.length) {
                    System.out.println("Invalid selection. Please choose between 1 and " + ability_options.length + ".");
                }
            } else {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine(); 
            }
        }

        ability = ability_options[choice - 1];
        System.out.println("Ability set to: " + ability + "\n");
    }

    public class setPlayerstats {

        public static int level = 1;
        
        public static int hp = 0;
        public static int attack = 0;
        public static int defense = 0;
        public static int magicAttack = 0;
        public static int magicDefense = 0;
        public static int speed = 0;
        public static int ability = 0;

        public static int getMaxStatsForLevel() {
            return 60 + (level - 1) * 10;
        }

        public static void setPlayerStats(Scanner scanner) {
            int maxPoints = getMaxStatsForLevel();
            boolean validAllocation = false;

            while (!validAllocation) {
                int remaining = maxPoints;
                System.out.println("\n=================================");
                System.out.println("        ALLOCATE YOUR STATS      ");
                System.out.println("=================================");
                System.out.println("Level: " + level + " | Available Points: " + maxPoints + "\n");

                hp = getValidStatInput(scanner, "HP", remaining);
                remaining -= hp;

                attack = getValidStatInput(scanner, "Attack", remaining);
                remaining -= attack;

                defense = getValidStatInput(scanner, "Defense", remaining);
                remaining -= defense;

                magicAttack = getValidStatInput(scanner, "Magic Attack", remaining);
                remaining -= magicAttack;

                magicDefense = getValidStatInput(scanner, "Magic Defense", remaining);
                remaining -= magicDefense;

                speed = getValidStatInput(scanner, "Speed", remaining);
                remaining -= speed;

                ability = getValidStatInput(scanner, "Ability", remaining);
                remaining -= ability;

                int totalAllocated = hp + attack + defense + magicAttack + magicDefense + speed + ability;

                if (totalAllocated == maxPoints) {
                    validAllocation = true;
                    System.out.println("\n✓ Stats successfully set!");
                } else {
                    System.out.println("\n[ERROR] You used " + totalAllocated + " out of " + maxPoints + " points.");
                    System.out.println("You must allocate ALL " + maxPoints + " points. Let's try again.\n");
                }
            }
        }

        private static int getValidStatInput(Scanner scanner, String statName, int remainingPoints) {

            int input = 0;
            
            while (input < 1 || input > remainingPoints) {
                System.out.printf("Enter %-15s (Points Left: %d, Min: 1): ", statName, remainingPoints);
                
                if (scanner.hasNextInt()) {
                    input = scanner.nextInt();
                    scanner.nextLine(); 

                    if (input < 1) {
                        System.out.println("  -> Each stat must have at least 1 point!");
                    } else if (input > remainingPoints) {
                        System.out.println("  -> You only have " + remainingPoints + " points remaining!");
                    }
                } else {
                    System.out.println("  -> Invalid input! Please enter a number.");
                    scanner.nextLine();
                }
            }
            return input;
        }

        public static void levelUp(Scanner scanner) {
            level++;
            System.out.println("\n🎉 LEVEL UP! You reached Level " + level + "!");
            System.out.println("You received +10 new stat points. Time to redistribute your stats!");
            setPlayerStats(scanner);
        }
    }

    public static void displayPlayerStats() {
        System.out.println("\n--- PLAYER STATS ---");
        System.out.println("Name: " + name);
        System.out.println("Specialty: " + speciality);
        System.out.println("Level: " + setPlayerstats.level);
        System.out.println("HP: " + setPlayerstats.hp);
        System.out.println("Attack: " + setPlayerstats.attack);
        System.out.println("Defense: " + setPlayerstats.defense);
        System.out.println("Magic Attack: " + setPlayerstats.magicAttack);
        System.out.println("Magic Defense: " + setPlayerstats.magicDefense);
        System.out.println("Speed: " + setPlayerstats.speed);
        System.out.println("Ability: " + setPlayerstats.ability);
    }
}