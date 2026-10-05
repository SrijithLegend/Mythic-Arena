import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Moves {

    public enum Type { PHYSICAL, MAGIC, SUPPORT }

    /**
     * A move. Combat.java implements the special effect of each move by name;
     * power is the base damage multiplier (0 for pure support moves).
     */
    public record Move(String name, Type type, double power, int unlockLevel, String effect) {
        public String description() {
            String lock = unlockLevel > 1 ? " [Unlock Lv" + unlockLevel + "]" : "";
            return name + " (" + type.name().toLowerCase() + ") - " + effect + lock;
        }

        public boolean dealsDamage() {
            return type != Type.SUPPORT;
        }
    }

    public static final int ULTIMATE_LEVEL = 10;
    public static final int MASTER_LEVEL = 20;

    private static final Move[] WARRIOR = {
        new Move("Whirlwind Slash", Type.PHYSICAL, 1.7, 1, "1.7x attack. Costs 5% of current HP."),
        new Move("Shield Bash", Type.PHYSICAL, 0.8, 1, "0.8x attack + bonus from defense. 50% chance to stun for 1 turn."),
        new Move("Execute", Type.PHYSICAL, 0.7, 1, "2.5x attack if opponent is below 30% HP, otherwise 0.7x."),
        new Move("Slash", Type.PHYSICAL, 1.2, 1, "1.2x attack. Reliable, no cost."),
        new Move("Rending Cleave", Type.PHYSICAL, 1.2, 1, "1.2x attack + opponent defense -20% for 2 turns."),
        new Move("Counter Stance", Type.SUPPORT, 0, 1, "Reflect 50% of the next hit taken back at the attacker (2 turns)."),
        new Move("War Cry", Type.SUPPORT, 0, 1, "Own attack +25% for 3 turns."),
        new Move("Adrenaline Surge", Type.SUPPORT, 0, 1, "Own speed +30% for 2 turns + heal 8% max HP."),
        new Move("Berserker's Fury", Type.PHYSICAL, 2.5, ULTIMATE_LEVEL, "2.5x attack. Costs 10% max HP."),
        new Move("Last Stand", Type.PHYSICAL, 1.0, MASTER_LEVEL, "If own HP < 25%: 3.0x attack + immune to the next hit. Otherwise 1.0x.")
    };

    private static final Move[] MAGE = {
        new Move("Fireball", Type.MAGIC, 1.3, 1, "1.3x magic attack. 50% chance to chain-hit again for 0.4x."),
        new Move("Arcane Seal", Type.SUPPORT, 0, 1, "Opponent attack and magic attack -30% for 3 turns."),
        new Move("Mana Burst", Type.MAGIC, 2.0, 1, "2.0x magic attack. Costs 8% max HP."),
        new Move("Frost Bolt", Type.MAGIC, 1.0, 1, "1.0x magic attack + opponent speed -25% for 2 turns."),
        new Move("Arcane Missile", Type.MAGIC, 0.9, 1, "0.9x magic attack. Never misses, no cost."),
        new Move("Chain Lightning", Type.MAGIC, 1.3, 1, "1.3x magic attack. 30% chance to strike again for 0.5x."),
        new Move("Mana Focus", Type.SUPPORT, 0, 1, "Own magic attack +25% for 3 turns."),
        new Move("Ward of Insight", Type.SUPPORT, 0, 1, "Own defense and magic defense +30% for 3 turns."),
        new Move("Meteor Storm", Type.MAGIC, 2.8, ULTIMATE_LEVEL, "2.8x magic attack. Costs 12% max HP."),
        new Move("Time Fracture", Type.SUPPORT, 0, MASTER_LEVEL, "Take an extra action immediately. Costs 12% max HP.")
    };

    private static final Move[] ROGUE = {
        new Move("Assassinate", Type.PHYSICAL, 0.8, 1, "1.4x attack + guaranteed critical if you acted first this turn, otherwise 0.8x."),
        new Move("Evasion Stance", Type.SUPPORT, 0, 1, "Dodge the next incoming attack + own speed +30% for 2 turns."),
        new Move("Poison Dart", Type.PHYSICAL, 0.6, 1, "0.6x attack + poison (5% of opponent max HP per turn for 4 turns)."),
        new Move("Quick Strike", Type.PHYSICAL, 1.1, 1, "1.1x attack. Always acts first regardless of speed."),
        new Move("Backstab", Type.PHYSICAL, 1.5, 1, "1.5x attack, 2.2x if the opponent used a non-damaging move last turn."),
        new Move("Throwing Knives", Type.PHYSICAL, 0.45, 1, "3 hits of 0.45x attack. No cost."),
        new Move("Shadow Step", Type.SUPPORT, 0, 1, "Own speed +40% for 2 turns."),
        new Move("Sharpen Blades", Type.SUPPORT, 0, 1, "Own attack +20% and critical chance +15% for 3 turns."),
        new Move("Death Mark", Type.PHYSICAL, 0.8, ULTIMATE_LEVEL, "0.8x attack + marks the opponent: the next 3 hits against them deal 1.5x."),
        new Move("Thousand Cuts", Type.PHYSICAL, 0.5, MASTER_LEVEL, "5 hits of 0.5x attack, each with its own critical roll.")
    };

    private static final Move[] PALADIN = {
        new Move("Divine Strike", Type.PHYSICAL, 1.3, 1, "1.3x attack. 50% chance to heal self for 8% max HP."),
        new Move("Blessing of Light", Type.SUPPORT, 0, 1, "Heal 8% max HP + own defense +20% for 3 turns."),
        new Move("Judgment", Type.PHYSICAL, 0.9, 1, "1.8x attack if opponent is below 50% HP, otherwise 0.9x."),
        new Move("Smite", Type.PHYSICAL, 1.1, 1, "1.1x attack. Never misses, no cost."),
        new Move("Consecration", Type.PHYSICAL, 0.9, 1, "0.9x attack + heal self 6% max HP."),
        new Move("Holy Retribution", Type.PHYSICAL, 1.2, 1, "1.2x attack, 1.6x if own HP is below 40%."),
        new Move("Sacred Vow", Type.SUPPORT, 0, 1, "Own defense and magic defense +25% for 3 turns."),
        new Move("Lay on Hands", Type.SUPPORT, 0, 1, "Heal self 20% max HP."),
        new Move("Wrath of Heaven", Type.PHYSICAL, 1.6, ULTIMATE_LEVEL, "1.6x attack + heal self 6% max HP."),
        new Move("Guardian Angel", Type.SUPPORT, 0, MASTER_LEVEL, "Survive the next lethal hit at 1 HP (once per battle).")
    };

    public static Move[] catalogFor(String specialty) {
        return switch (specialty) {
            case "Warrior" -> WARRIOR;
            case "Mage" -> MAGE;
            case "Rogue" -> ROGUE;
            case "Paladin" -> PALADIN;
            default -> throw new IllegalArgumentException("Unknown specialty: " + specialty);
        };
    }

    /**
     * Finds a move by name. Also accepts the old "Name — description" strings
     * that earlier versions of the game stored in the database.
     */
    public static Move find(String specialty, String text) {
        if (text == null) return null;
        for (Move m : catalogFor(specialty)) {
            if (text.trim().toLowerCase().startsWith(m.name().toLowerCase())) return m;
        }
        return null;
    }

    public static List<Move> unlockedFor(String specialty, int level) {
        List<Move> list = new ArrayList<>();
        for (Move m : catalogFor(specialty)) {
            if (m.unlockLevel() <= level) list.add(m);
        }
        return list;
    }

    /** The first four moves of a class: a sane default loadout. */
    public static Move[] defaultMoves(String specialty) {
        Move[] all = catalogFor(specialty);
        return new Move[] { all[0], all[2], all[3], all[4] };
    }

    /** Lets the player pick 4 different moves from those unlocked at their level. */
    public static Move[] chooseMoves(Scanner scanner, String specialty, int level) {
        Move[] all = catalogFor(specialty);

        System.out.println("\n--- Choose Your 4 Moves ---");
        for (int i = 0; i < all.length; i++) {
            String locked = all[i].unlockLevel() > level ? "  (LOCKED)" : "";
            System.out.println("  " + (i + 1) + ". " + all[i].description() + locked);
        }

        Move[] selected = new Move[4];
        for (int slot = 0; slot < 4; slot++) {
            while (true) {
                int choice = Input.readInt(scanner, "Choose Move #" + (slot + 1) + " (1-" + all.length + "): ", 1, all.length);
                Move move = all[choice - 1];
                if (move.unlockLevel() > level) {
                    System.out.println("  -> " + move.name() + " unlocks at level " + move.unlockLevel() + ".");
                } else if (contains(selected, move)) {
                    System.out.println("  -> You already picked " + move.name() + ".");
                } else {
                    selected[slot] = move;
                    System.out.println("Added: " + move.name());
                    break;
                }
            }
        }
        System.out.println("\nAll 4 moves set!");
        return selected;
    }

    private static boolean contains(Move[] moves, Move move) {
        for (Move m : moves) {
            if (m == move) return true;
        }
        return false;
    }
}
