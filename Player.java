import java.util.ArrayList;
import java.util.List;
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

    public static int xp = 0;
    public static List<String> unlockedMoves = new ArrayList<>();
    public static List<String> unlockedUltimates = new ArrayList<>();

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
    if (speciality == null) {
        System.out.println("Please set your specialty first.");
        return;
    }
    switch (speciality) {
        case "Warrior" -> ability = Character.chooseWarriorAbility(scanner);
        case "Mage" -> ability = Character.chooseMageAbility(scanner);
        case "Rogue" -> ability = Character.chooseRogueAbility(scanner);
        case "Paladin" -> ability = Character.choosePaladinAbility(scanner);
    }
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

                hp = getValidStatInput(scanner, "HP", remaining, 1);
                remaining -= hp;

                attack = getValidStatInput(scanner, "Attack", remaining, 1);
                remaining -= attack;

                defense = getValidStatInput(scanner, "Defense", remaining, 1);
                remaining -= defense;

                magicAttack = getValidStatInput(scanner, "Magic Attack", remaining, 1);
                remaining -= magicAttack;

                magicDefense = getValidStatInput(scanner, "Magic Defense", remaining, 1);
                remaining -= magicDefense;

                speed = getValidStatInput(scanner, "Speed", remaining, 1);
                remaining -= speed;

                ability = getValidStatInput(scanner, "Ability", remaining, 1);
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

        private static int getValidStatInput(Scanner scanner, String statName, int remainingPoints, int minPoints) {

            int input = minPoints - 1;

            while (input < minPoints || input > remainingPoints) {
                System.out.printf("Enter %-15s (Points Left: %d, Min: %d): ", statName, remainingPoints, minPoints);

                if (scanner.hasNextInt()) {
                    input = scanner.nextInt();
                    scanner.nextLine();

                    if (input < minPoints) {
                        System.out.println("  -> Each stat must have at least " + minPoints + " point(s)!");
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

        public static void allocateNewPoints(Scanner scanner, int newPoints) {
            boolean validAllocation = false;

            while (!validAllocation) {
                int remaining = newPoints;
                System.out.println("\n=================================");
                System.out.println("      ALLOCATE NEW STAT POINTS   ");
                System.out.println("=================================");
                System.out.println("Level: " + level + " | New Points to Spend: " + newPoints + "\n");

                int hpGain = getValidStatInput(scanner, "HP", remaining, 0);
                remaining -= hpGain;

                int attackGain = getValidStatInput(scanner, "Attack", remaining, 0);
                remaining -= attackGain;

                int defenseGain = getValidStatInput(scanner, "Defense", remaining, 0);
                remaining -= defenseGain;

                int magicAttackGain = getValidStatInput(scanner, "Magic Attack", remaining, 0);
                remaining -= magicAttackGain;

                int magicDefenseGain = getValidStatInput(scanner, "Magic Defense", remaining, 0);
                remaining -= magicDefenseGain;

                int speedGain = getValidStatInput(scanner, "Speed", remaining, 0);
                remaining -= speedGain;

                int abilityGain = getValidStatInput(scanner, "Ability", remaining, 0);
                remaining -= abilityGain;

                int totalAllocated = hpGain + attackGain + defenseGain + magicAttackGain + magicDefenseGain + speedGain + abilityGain;

                if (totalAllocated == newPoints) {
                    hp += hpGain;
                    attack += attackGain;
                    defense += defenseGain;
                    magicAttack += magicAttackGain;
                    magicDefense += magicDefenseGain;
                    speed += speedGain;
                    ability += abilityGain;
                    validAllocation = true;
                    System.out.println("\n✓ Stat points allocated!");
                } else {
                    System.out.println("\n[ERROR] You used " + totalAllocated + " out of " + newPoints + " points.");
                    System.out.println("You must allocate ALL " + newPoints + " points. Let's try again.\n");
                }
            }
        }
    }

    public static boolean validateHero() {
            if (Player.name == null || Player.name.isBlank()) {
                System.out.println("Error: name not set.");
                return false;
            }
            if (Player.speciality == null) {
                System.out.println("Error: specialty not set.");
                return false;
            }
            if (Player.ability == null) {
                System.out.println("Error: ability not set.");
                return false;
            }
            int total = Player.setPlayerstats.hp + Player.setPlayerstats.attack
                    + Player.setPlayerstats.defense + Player.setPlayerstats.magicAttack
                    + Player.setPlayerstats.magicDefense + Player.setPlayerstats.speed
                    + Player.setPlayerstats.ability;
            int expected = Player.setPlayerstats.getMaxStatsForLevel();
            if (total != expected) {
                System.out.println("Error: stat points don't add up (" + total + "/" + expected + ").");
                return false;
            }
            return true;
        }


    public static void displayPlayerStats() {
        System.out.println("\n--- PLAYER STATS ---");
        System.out.println("Name: " + name);
        System.out.println("Specialty: " + speciality);
        System.out.println("Level: " + setPlayerstats.level);
        System.out.println("XP: " + xp + " / " + LevelSystem.xpForLevel(setPlayerstats.level));
        System.out.println("HP: " + setPlayerstats.hp);
        System.out.println("Attack: " + setPlayerstats.attack);
        System.out.println("Defense: " + setPlayerstats.defense);
        System.out.println("Magic Attack: " + setPlayerstats.magicAttack);
        System.out.println("Magic Defense: " + setPlayerstats.magicDefense);
        System.out.println("Speed: " + setPlayerstats.speed);
        System.out.println("Total Stats: " + (setPlayerstats.hp + setPlayerstats.attack + setPlayerstats.defense + setPlayerstats.magicAttack + setPlayerstats.magicDefense + setPlayerstats.speed + setPlayerstats.ability));
        System.out.println("Ability: " + setPlayerstats.ability);
    }
}