import java.util.Scanner;

/** Class (specialty) definitions: the signature ability options and recommended stat builds. */
public class Character {

    public static final String[] SPECIALTIES = {"Warrior", "Mage", "Rogue", "Paladin"};

    public static final String[] SPECIALTY_BLURBS = {
        "Warrior - tough frontline fighter. High HP and attack, HP-costing power moves.",
        "Mage    - glass cannon. Huge magic damage, debuffs and a turn-bending ultimate.",
        "Rogue   - fast and deadly. Crits, poison, dodges and multi-hit combos.",
        "Paladin - holy tank. Strong defense, healing and comeback damage."
    };

    /** Stat weights (percent) in order HP, Attack, Defense, Magic Attack, Magic Defense, Speed. */
    public static int[] recommendedBuild(String specialty) {
        return switch (specialty) {
            case "Warrior" -> new int[] {30, 30, 15, 0, 15, 10};
            case "Mage" -> new int[] {20, 0, 15, 40, 15, 10};
            case "Rogue" -> new int[] {20, 30, 10, 5, 10, 25};
            case "Paladin" -> new int[] {25, 30, 15, 0, 20, 10};
            default -> throw new IllegalArgumentException("Unknown specialty: " + specialty);
        };
    }

    /**
     * The signature ability is one of the class's three core moves. In battle it can be
     * unleashed once per fight with 1.5x power and no HP cost.
     */
    public static Moves.Move[] abilityOptions(String specialty) {
        Moves.Move[] all = Moves.catalogFor(specialty);
        return new Moves.Move[] { all[0], all[1], all[2] };
    }

    public static String chooseAbility(Scanner scanner, String specialty) {
        Moves.Move[] options = abilityOptions(specialty);
        String[] labels = new String[options.length];
        for (int i = 0; i < options.length; i++) {
            labels[i] = options[i].name() + " : " + options[i].effect();
        }
        System.out.println("\nYour signature ability can be used ONCE per battle with 1.5x power and no HP cost.");
        int choice = Input.choose(scanner, "Choose Your Signature Ability", labels);
        String ability = options[choice].name();
        System.out.println("Ability set to: " + ability);
        return ability;
    }
}
