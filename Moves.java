import java.util.Scanner;

public class Moves {

    public record Move(String description) {}

    public static Move[] chooseWarriorMoves(Scanner scanner) {
        Move[] warriorMoves = {
            new Move("Whirlwind Slash — 1.2x attack, all enemies. Costs 20% current HP."),
            new Move("Shield Bash — 0.8x attack + 50% chance to stun 1 turn. Scales with defense."),
            new Move("Execute — 2.0x attack if opponent <30% HP, else 0.5x."),
            new Move("Slash — 1.0x attack, no cost. Reliable basic."),
            new Move("Rending Cleave — 1.1x attack + opponent defense -15% for 2 turns."),
            new Move("Counter Stance — Reflect 50% of next incoming hit back at attacker. Lasts 1 turn."),
            new Move("War Cry — Own attack +25% for 3 turns."),
            new Move("Adrenaline Surge — Own speed +30% + heal 10% max HP. 1 turn."),
            new Move("Berserker's Fury — 2.5x attack, all enemies. Costs 35% max HP. Unlock Lv10."),
            new Move("Last Stand — If own HP <20%: 3.0x attack + immune to next hit. Unlock Lv20.")
        };

        System.out.println("\n--- Choose Your Moves ---");
        for (int i = 0; i < warriorMoves.length; i++) {
            System.out.println((i + 1) + ". " + warriorMoves[i].description());
        }

        Move[] selectedMoves = new Move[4];
        for (int moveSlot = 0; moveSlot < 4; moveSlot++) {
            int choice = -1;
            
            while (choice < 1 || choice > warriorMoves.length) {
                System.out.print("Choose Move #" + (moveSlot + 1) + " (1-" + warriorMoves.length + "): ");
                
                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    scanner.nextLine();
                    
                    if (choice < 1 || choice > warriorMoves.length) {
                        System.out.println("Invalid selection.");
                    }
                } else {
                    System.out.println("Invalid input! Please enter a number.");
                    scanner.nextLine();
                }
            }
            
            selectedMoves[moveSlot] = warriorMoves[choice - 1];
            System.out.println("Added: " + selectedMoves[moveSlot].description());
        }
        
        System.out.println("\nAll 4 moves set!");
        return selectedMoves;
    }

    public static Move[] chooseMageMoves(Scanner scanner) {
        Move[] mageMoves = {
            new Move("Fireball — 1.5x magic attack, all enemies. 50% chance to chain-hit same target again."),
            new Move("Arcane Seal — Opponent magic attack -40% for 3 turns. No direct damage."),
            new Move("Mana Burst — 2.0x magic attack. Costs 30% max HP. Resets cooldown on kill."),
            new Move("Frost Bolt — 1.0x magic attack + opponent speed -20% for 2 turns."),
            new Move("Arcane Missile — 0.9x magic attack, guaranteed hit, no cost."),
            new Move("Chain Lightning — 1.3x magic attack, 30% chance to hit again for 0.5x."),
            new Move("Mana Focus — Own magic attack +25% for 3 turns."),
            new Move("Ward of Insight — Own magic defense +30% for 3 turns."),
            new Move("Meteor Storm — 2.8x magic attack, all enemies. Costs 30% max HP. Unlock Lv10."),
            new Move("Time Fracture — Take an extra turn immediately after this one. Costs 20% max HP. Unlock Lv20.")
        };

        System.out.println("\n--- Choose Your Moves ---");
        for (int i = 0; i < mageMoves.length; i++) {
            System.out.println((i + 1) + ". " + mageMoves[i].description());
        }

        Move[] selectedMoves = new Move[4];
        for (int moveSlot = 0; moveSlot < 4; moveSlot++) {
            int choice = -1;

            while (choice < 1 || choice > mageMoves.length) {
                System.out.print("Choose Move #" + (moveSlot + 1) + " (1-" + mageMoves.length + "): ");

                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    scanner.nextLine();

                    if (choice < 1 || choice > mageMoves.length) {
                        System.out.println("Invalid selection. Please choose between 1 and " + mageMoves.length + ".");
                    }
                } else {
                    System.out.println("Invalid input! Please enter a number.");
                    scanner.nextLine();
                }
            }

            selectedMoves[moveSlot] = mageMoves[choice - 1];
            System.out.println("Added: " + selectedMoves[moveSlot].description());
        }
        
        System.out.println("\nAll 4 moves set!");
        return selectedMoves;
    }

    public static Move[] chooseRogueMoves(Scanner scanner) {
        Move[] rogueMoves = {
            new Move("Assassinate — 1.8x attack + guaranteed crit (2x). Only if moving first this turn."),
            new Move("Evasion Stance — Dodge next hit + speed +30% for 2 turns."),
            new Move("Poison Dart — 0.6x attack + poison 5% max HP/turn for 4 turns."),
            new Move("Quick Strike — 1.0x attack, always goes first regardless of speed."),
            new Move("Backstab — 1.4x attack, 2.0x if opponent used a defensive move last turn."),
            new Move("Throwing Knives — 0.8x attack, all enemies. No cost."),
            new Move("Shadow Step — Own speed +40% for 2 turns."),
            new Move("Sharpen Blades — Own attack +20% + crit chance +15% for 3 turns."),
            new Move("Death Mark — Marks opponent; next 2 attacks against them deal 1.5x. Unlock Lv10."),
            new Move("Thousand Cuts — 5 hits of 0.4x attack each, independent crit rolls. Unlock Lv20.")
        };

        System.out.println("\n--- Choose Your Moves ---");
        for (int i = 0; i < rogueMoves.length; i++) {
            System.out.println((i + 1) + ". " + rogueMoves[i].description());
        }

        Move[] selectedMoves = new Move[4];
        for (int moveSlot = 0; moveSlot < 4; moveSlot++) {
            int choice = -1;

            while (choice < 1 || choice > rogueMoves.length) {
                System.out.print("Choose Move #" + (moveSlot + 1) + " (1-" + rogueMoves.length + "): ");

                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    scanner.nextLine();

                    if (choice < 1 || choice > rogueMoves.length) {
                        System.out.println("Invalid selection. Please choose between 1 and " + rogueMoves.length + ".");
                    }
                } else {
                    System.out.println("Invalid input! Please enter a number.");
                    scanner.nextLine();
                }
            }

            selectedMoves[moveSlot] = rogueMoves[choice - 1];
            System.out.println("Added: " + selectedMoves[moveSlot].description());
        }
        
        System.out.println("\nAll 4 moves set!");
        return selectedMoves;
    }

    public static Move[] choosePaladinMoves(Scanner scanner) {
        Move[] paladinMoves = {
            new Move("Divine Strike — 1.5x attack, 50% chance to heal self 20% max HP."),
            new Move("Blessing of Light — All allies defense +20% for 3 turns. No direct damage."),
            new Move("Judgment — 2.0x attack if opponent <50% HP, else 1.0x."),
            new Move("Smite — 1.1x attack, guaranteed hit, no cost."),
            new Move("Consecration — 0.9x attack, all enemies + heal self 10% max HP."),
            new Move("Holy Retribution — 1.2x attack, 1.6x if own HP <40%."),
            new Move("Sacred Vow — Own defense + magic defense +25% for 3 turns."),
            new Move("Lay on Hands — Heal self 25% max HP. No cost."),
            new Move("Wrath of Heaven — 2.5x attack, all enemies + heal self 15% max HP. Unlock Lv10."),
            new Move("Guardian Angel — Passive: survive one lethal hit per battle at 1 HP. Unlock Lv20.")
        };

        System.out.println("\n--- Choose Your Moves ---");
        for (int i = 0; i < paladinMoves.length; i++) {
            System.out.println((i + 1) + ". " + paladinMoves[i].description());
        }

        Move[] selectedMoves = new Move[4];
        for (int moveSlot = 0; moveSlot < 4; moveSlot++) {
            int choice = -1;

            while (choice < 1 || choice > paladinMoves.length) {
                System.out.print("Choose Move #" + (moveSlot + 1) + " (1-" + paladinMoves.length + "): ");

                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    scanner.nextLine();

                    if (choice < 1 || choice > paladinMoves.length) {
                        System.out.println("Invalid selection. Please choose between 1 and " + paladinMoves.length + ".");
                    }
                } else {
                    System.out.println("Invalid input! Please enter a number.");
                    scanner.nextLine();
                }
            }

            selectedMoves[moveSlot] = paladinMoves[choice - 1];
            System.out.println("Added: " + selectedMoves[moveSlot].description());
        }
        
        System.out.println("\nAll 4 moves set!");
        return selectedMoves;
    }
}