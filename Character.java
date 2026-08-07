import java.util.Scanner;

public class Character {


        public static void setWarriorSpeciality(Scanner scanner) {

        String[] warriorOptions = {
                "Whirlwind Slash : Deal 1.2x attack damage to all enemies. Costs 20% of current HP.", 
                "Shield Bash : Deal 0.8x attack damage + stun opponent for 1 turn (50% hit chance). Scales with defense.", 
                "Execute : Deal 2.0x attack damage if opponent is below 30% HP, otherwise 0.5x damage. High risk/reward.", 
            };

        System.out.println("\n--- Choose Your Specialty ---");
        for (int i = 0; i < warriorOptions.length; i++) {
            System.out.println((i + 1) + ". " + warriorOptions[i]);
        }

        int choice = -1;

        while (choice < 1 || choice > warriorOptions.length) {
            System.out.print("Enter choice (1-" + warriorOptions.length + "): ");

            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); 

                if (choice < 1 || choice > warriorOptions.length) {
                    System.out.println("Invalid selection. Please choose between 1 and " + warriorOptions.length + ".");
                }
            } else {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine(); 
            }
        }

        String warriorSpeciality = warriorOptions[choice - 1];
        System.out.println("Specialty set to: " + warriorSpeciality + "\n");
    }   

        public static void setMageSpeciality(Scanner scanner) {

        String[] mageOptions = {
                "Fireball : Deal 1.5x magic attack damage to all enemies. Can chain (50% chance to hit same target twice)..", 
                "Arcane Seal : Reduce opponent's magic attack by 40% for 3 turns. No direct damage; pure utility..", 
                "Mana Burst : Deal 2.0x magic attack damage. Costs 30% max HP. Resets cooldown if it kills opponent.", 
            };

        System.out.println("\n--- Choose Your Specialty ---");
        for (int i = 0; i < mageOptions.length; i++) {
            System.out.println((i + 1) + ". " + mageOptions[i]);
        }

        int choice = -1;

        while (choice < 1 || choice > mageOptions.length) {
            System.out.print("Enter choice (1-" + mageOptions.length + "): ");

            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); 

                if (choice < 1 || choice > mageOptions.length) {
                    System.out.println("Invalid selection. Please choose between 1 and " + mageOptions.length + ".");
                }
            } else {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine(); 
            }
        }

        String mageSpeciality = mageOptions[choice - 1];
        System.out.println("Specialty set to: " + mageSpeciality + "\n");
    }


    public static void setRogueSpeciality(Scanner scanner) {

        String[] rogueOptions = {
                "Assassinate : Deal 1.8x attack damage + guaranteed critical (2x multiplier). Only usable if you move first this turn.", 
                "Evasion Stance : Dodge next incoming attack + gain +30% speed for 2 turns. Defensive pivot.", 
                "Poison Dart : Deal 0.6x attack damage + apply poison (5% max HP damage per turn for 4 turns). Low burst, high sustained.", 
            };

        System.out.println("\n--- Choose Your Specialty ---");
        for (int i = 0; i < rogueOptions.length; i++) {
            System.out.println((i + 1) + ". " + rogueOptions[i]);
        }

        int choice = -1;

        while (choice < 1 || choice > rogueOptions.length) {
            System.out.print("Enter choice (1-" + rogueOptions.length + "): ");

            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); 

                if (choice < 1 || choice > rogueOptions.length) {
                    System.out.println("Invalid selection. Please choose between 1 and " + rogueOptions.length + ".");
                }
            } else {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine(); 
            }
        }

        String rogueSpeciality = rogueOptions[choice - 1];
        System.out.println("Specialty set to: " + rogueSpeciality + "\n");
    }   

        public static void setPaladinSpeciality(Scanner scanner) {

        String[] paladinOptions = {
                "Divine Strike : Deal 1.5x attack damage with a 50% chance to heal self for 20% max HP.", 
                "Blessing of Light : Grant all allies +20% defense for 3 turns. No direct damage; pure support.", 
                "Judgment : Deal 2.0x attack damage if opponent is below 50% HP, otherwise 1.0x damage. High risk/reward.", 
            };

        System.out.println("\n--- Choose Your Specialty ---");
        for (int i = 0; i < paladinOptions.length; i++) {
            System.out.println((i + 1) + ". " + paladinOptions[i]);
        }

        int choice = -1;

        while (choice < 1 || choice > paladinOptions.length) {
            System.out.print("Enter choice (1-" + paladinOptions.length + "): ");

            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); 

                if (choice < 1 || choice > paladinOptions.length) {
                    System.out.println("Invalid selection. Please choose between 1 and " + paladinOptions.length + ".");
                }
            } else {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine(); 
            }
        }

        String paladinSpeciality = paladinOptions[choice - 1];
        System.out.println("Specialty set to: " + paladinSpeciality + "\n");
    }

}   
