import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/** Turn-based 1v1 battle engine. Each side is controlled by a human (console) or the AI. */
public class Combat {

    public static final int MAX_ROUNDS = 40;
    public static final int MAX_POTIONS_PER_BATTLE = 2;
    public static final double POTION_HEAL = 0.35;
    public static final double SIGNATURE_BOOST = 1.5;
    public static final double DAMAGE_SCALE = 1.6;

    /** winner is null for a draw. */
    public record Result(Player winner, Player loser, int rounds) {}

    enum Stat { ATK, DEF, MATK, MDEF, SPD, CRIT }

    /** A timed buff (positive amount) or debuff (negative amount). */
    static class Mod {
        final Stat stat;
        final double amount;
        int turns;

        Mod(Stat stat, double amount, int turns) {
            this.stat = stat;
            this.amount = amount;
            this.turns = turns;
        }
    }

    record Action(Moves.Move move, boolean signature, boolean potion) {
        String label() {
            if (potion) return "Health Potion";
            return signature ? "SIGNATURE " + move.name() : move.name();
        }
    }

    /** A hero's state for the duration of one battle. */
    static class Fighter {
        final Player p;
        final boolean human;
        final int maxHp;
        int hp;
        final List<Mod> mods = new ArrayList<>();
        int stunned, poisonTurns, poisonDamage, reflectRounds, markHits, potionsUsed;
        boolean dodgeNext, immuneNext, guardian, guardianUsed, signatureUsed, lastWasSupport, actedFirst, extraTurn;

        Fighter(Player p, boolean human) {
            this.p = p;
            this.human = human;
            this.maxHp = p.maxBattleHp();
            this.hp = maxHp;
        }

        double hpPct() {
            return (double) hp / maxHp;
        }

        double stat(Stat s) {
            double base = switch (s) {
                case ATK -> p.attack;
                case DEF -> p.defense;
                case MATK -> p.magicAttack;
                case MDEF -> p.magicDefense;
                case SPD -> p.speed;
                case CRIT -> 0.05 + Math.min(0.15, p.speed * 0.001);
            };
            for (Mod m : mods) {
                if (m.stat != s) continue;
                base = s == Stat.CRIT ? base + m.amount : base * (1 + m.amount);
            }
            return Math.max(s == Stat.CRIT ? 0 : 1, base);
        }

        /** Re-applying the same buff/debuff refreshes it instead of stacking forever. */
        void addMod(Stat s, double amount, int turns) {
            mods.removeIf(m -> m.stat == s && Math.signum(m.amount) == Math.signum(amount));
            mods.add(new Mod(s, amount, turns));
        }

        boolean hasMod(Stat s, boolean positive) {
            for (Mod m : mods) {
                if (m.stat == s && (m.amount > 0) == positive) return true;
            }
            return false;
        }

        void heal(double fraction) {
            int amount = Math.min(maxHp - hp, (int) Math.round(maxHp * fraction));
            hp += amount;
            System.out.println("  " + p.name + " recovers " + amount + " HP.");
        }

        void payHp(int cost) {
            cost = Math.min(cost, hp - 1);
            if (cost > 0) {
                hp -= cost;
                System.out.println("  " + p.name + " sacrifices " + cost + " HP.");
            }
        }
    }

    private final Scanner scanner;
    private final Random rng;
    private final boolean potionsAllowed;

    private Combat(Scanner scanner, Random rng, boolean potionsAllowed) {
        this.scanner = scanner;
        this.rng = rng;
        this.potionsAllowed = potionsAllowed;
    }

    /**
     * Runs a full battle. Potions used by human fighters are deducted from their Player
     * object; the caller is responsible for saving.
     */
    public static Result fight(Scanner scanner, Player a, boolean aHuman, Player b, boolean bHuman,
                               boolean potionsAllowed, Random rng) {
        return new Combat(scanner, rng, potionsAllowed).run(new Fighter(a, aHuman), new Fighter(b, bHuman));
    }

    private Result run(Fighter a, Fighter b) {
        System.out.println("\n==================================================");
        System.out.println("  BATTLE START: " + a.p.name + " (Lv" + a.p.level + " " + a.p.speciality + ")");
        System.out.println("            vs  " + b.p.name + " (Lv" + b.p.level + " " + b.p.speciality + ")");
        System.out.println("==================================================");

        int round = 0;
        while (a.hp > 0 && b.hp > 0 && round < MAX_ROUNDS) {
            round++;
            System.out.println("\n------------------- ROUND " + round + " -------------------");
            printStatus(a);
            printStatus(b);

            Action actA = a.stunned > 0 ? null : choose(a, b);
            Action actB = b.stunned > 0 ? null : choose(b, a);

            boolean aFirst = goesFirst(a, actA, b, actB);
            Fighter first = aFirst ? a : b, second = aFirst ? b : a;
            first.actedFirst = true;
            second.actedFirst = false;

            takeTurn(first, second, aFirst ? actA : actB);
            if (second.hp > 0 && first.hp > 0) takeTurn(second, first, aFirst ? actB : actA);

            endOfRound(a, b);
        }

        Fighter winner = null, loser = null;
        if (a.hp > 0 && b.hp <= 0) { winner = a; loser = b; }
        else if (b.hp > 0 && a.hp <= 0) { winner = b; loser = a; }
        else if (a.hp > 0 && b.hp > 0) {
            System.out.println("\nThe battle drags on for " + MAX_ROUNDS + " rounds! The judges decide by remaining health...");
            if (a.hpPct() > b.hpPct()) { winner = a; loser = b; }
            else if (b.hpPct() > a.hpPct()) { winner = b; loser = a; }
        }

        System.out.println("\n==================================================");
        System.out.println(winner == null ? "  The battle ends in a DRAW!" : "  " + winner.p.name.toUpperCase() + " WINS!");
        System.out.println("==================================================");
        return new Result(winner == null ? null : winner.p, loser == null ? null : loser.p, round);
    }

    private boolean goesFirst(Fighter a, Action actA, Fighter b, Action actB) {
        int prioA = priority(actA), prioB = priority(actB);
        if (prioA != prioB) return prioA > prioB;
        double spdA = a.stat(Stat.SPD), spdB = b.stat(Stat.SPD);
        if (spdA != spdB) return spdA > spdB;
        return rng.nextBoolean();
    }

    private static int priority(Action act) {
        if (act == null) return 0;
        if (act.potion()) return 1;
        return act.move().name().equals("Quick Strike") ? 1 : 0;
    }

    private void takeTurn(Fighter user, Fighter target, Action action) {
        if (user.stunned > 0) {
            user.stunned--;
            System.out.println("\n" + user.p.name + " is stunned and can't move!");
            return;
        }
        perform(user, target, action, false);
        if (user.extraTurn && user.hp > 0 && target.hp > 0) {
            user.extraTurn = false;
            System.out.println("\nTime fractures around " + user.p.name + "... an extra action!");
            if (user.human) {
                printStatus(user);
                printStatus(target);
            }
            perform(user, target, choose(user, target), true);
        }
    }

    // ------------------------------------------------------------------ choosing actions

    private Action choose(Fighter self, Fighter foe) {
        return self.human ? chooseHuman(self) : chooseAi(self, foe);
    }

    private List<Action> availableActions(Fighter self) {
        List<Action> actions = new ArrayList<>();
        for (Moves.Move m : self.p.moves) actions.add(new Action(m, false, false));
        if (!self.signatureUsed) {
            Moves.Move sig = Moves.find(self.p.speciality, self.p.ability);
            if (sig != null) actions.add(new Action(sig, true, false));
        }
        if (potionsAllowed && self.human && self.p.potions > 0 && self.potionsUsed < MAX_POTIONS_PER_BATTLE && self.hp < self.maxHp) {
            actions.add(new Action(null, false, true));
        }
        return actions;
    }

    private Action chooseHuman(Fighter self) {
        List<Action> actions = availableActions(self);
        System.out.println("\n>> " + self.p.name + ", choose your action:");
        for (int i = 0; i < actions.size(); i++) {
            Action a = actions.get(i);
            String text;
            if (a.potion()) {
                text = "Drink Health Potion - heal " + (int) (POTION_HEAL * 100) + "% max HP (" + self.p.potions + " left)";
            } else if (a.signature()) {
                text = "SIGNATURE: " + a.move().name() + " - 1.5x power, no HP cost (once per battle)";
            } else {
                text = a.move().description();
            }
            System.out.println("  " + (i + 1) + ". " + text);
        }
        int choice = Input.readInt(scanner, "Action (1-" + actions.size() + "): ", 1, actions.size());
        return actions.get(choice - 1);
    }

    /** Scores each option and usually picks the best one, sometimes something else to stay unpredictable. */
    private Action chooseAi(Fighter self, Fighter foe) {
        List<Action> actions = availableActions(self);
        double best = -1;
        Action bestAction = actions.get(0);
        List<Action> viable = new ArrayList<>();
        for (Action a : actions) {
            double score = scoreAction(self, foe, a);
            if (score > 0) viable.add(a);
            if (score > best) {
                best = score;
                bestAction = a;
            }
        }
        if (!viable.isEmpty() && rng.nextDouble() < 0.2) {
            return viable.get(rng.nextInt(viable.size()));
        }
        return bestAction;
    }

    private double scoreAction(Fighter self, Fighter foe, Action action) {
        if (action.potion()) return self.hpPct() < 0.35 ? 100 : 0;
        Moves.Move m = action.move();
        boolean magic = m.type() == Moves.Type.MAGIC;
        double damageScore = effectivePower(self, foe, m) * baseDamage(
            self.stat(magic ? Stat.MATK : Stat.ATK), foe.stat(magic ? Stat.MDEF : Stat.DEF));
        // What a plain 1.0x hit with the hero's best offense would do; support moves are scored against it.
        double baseline = Math.max(baseDamage(self.stat(Stat.ATK), foe.stat(Stat.DEF)),
                                   baseDamage(self.stat(Stat.MATK), foe.stat(Stat.MDEF)));

        if (action.signature()) {
            // Save the signature for when it matters.
            boolean worthIt = foe.hpPct() < 0.6 || self.hpPct() < 0.4;
            if (!worthIt) return 0;
            damageScore *= SIGNATURE_BOOST;
        }
        int cost = action.signature() ? 0 : hpCost(self, m);
        if (cost >= self.hp) return 0;
        // Losing HP matters more the closer we are to dying.
        damageScore -= cost * (self.hpPct() < 0.35 ? 2.0 : 1.0);

        if (m.dealsDamage()) return Math.max(0.01, damageScore);

        boolean low = self.hpPct() < 0.4;
        return baseline * switch (m.name()) {
            case "Lay on Hands" -> low ? 2.5 : 0;
            case "Blessing of Light" -> low ? 1.8 : self.hasMod(Stat.DEF, true) ? 0 : 0.7;
            case "Adrenaline Surge" -> low ? 1.2 : self.hasMod(Stat.SPD, true) ? 0 : 0.6;
            case "War Cry" -> self.hasMod(Stat.ATK, true) ? 0 : 1.1;
            case "Mana Focus" -> self.hasMod(Stat.MATK, true) ? 0 : 1.1;
            case "Sharpen Blades" -> self.hasMod(Stat.CRIT, true) ? 0 : 1.0;
            case "Shadow Step" -> self.hasMod(Stat.SPD, true) ? 0 : 0.6;
            case "Ward of Insight", "Sacred Vow" -> self.hasMod(Stat.DEF, true) ? 0 : 0.8;
            case "Arcane Seal" -> foe.hasMod(Stat.ATK, false) ? 0 : 1.0;
            case "Counter Stance" -> self.reflectRounds > 0 ? 0 : 0.7;
            case "Evasion Stance" -> self.dodgeNext ? 0 : 0.8;
            case "Guardian Angel" -> self.guardian || self.guardianUsed ? 0 : 1.2;
            case "Time Fracture" -> self.hpPct() > 0.4 ? 1.4 : 0;
            default -> 0.5;
        };
    }

    /** Total damage multiplier a move would have right now (used by the AI). */
    private double effectivePower(Fighter self, Fighter foe, Moves.Move m) {
        return switch (m.name()) {
            case "Execute" -> foe.hpPct() < 0.3 ? 2.5 : 0.7;
            case "Judgment" -> foe.hpPct() < 0.5 ? 1.8 : 0.9;
            case "Last Stand" -> self.hpPct() < 0.25 ? 3.0 : 1.0;
            case "Holy Retribution" -> self.hpPct() < 0.4 ? 1.6 : 1.2;
            case "Assassinate" -> self.stat(Stat.SPD) >= foe.stat(Stat.SPD) ? 2.1 : 0.8;
            case "Backstab" -> foe.lastWasSupport ? 2.2 : 1.5;
            case "Throwing Knives" -> 1.3;
            case "Thousand Cuts" -> 2.4;
            case "Fireball" -> 1.5;
            case "Chain Lightning" -> 1.45;
            case "Shield Bash" -> shieldBashPower(self) + 0.3;
            case "Poison Dart" -> foe.poisonTurns > 0 ? 0.6 : 1.4;
            case "Rending Cleave" -> foe.hasMod(Stat.DEF, false) ? 1.1 : 1.35;
            case "Frost Bolt" -> foe.hasMod(Stat.SPD, false) ? 1.0 : 1.2;
            case "Death Mark" -> foe.markHits > 0 ? 0.8 : 2.0;
            case "Divine Strike" -> 1.3 + (self.hpPct() < 0.6 ? 0.2 : 0);
            case "Consecration" -> 0.9 + (self.hpPct() < 0.6 ? 0.3 : 0);
            case "Wrath of Heaven" -> 1.6 + (self.hpPct() < 0.6 ? 0.2 : 0);
            default -> m.power();
        };
    }

    /**
     * Damage of a 1.0x hit. Scales with level: doubling both stats doubles damage,
     * matching how battle HP grows.
     */
    static double baseDamage(double offense, double defense) {
        return DAMAGE_SCALE * offense * offense / (offense + defense);
    }

    private static double shieldBashPower(Fighter self) {
        return 0.8 + Math.min(0.5, 0.4 * self.stat(Stat.DEF) / self.stat(Stat.ATK));
    }

    private static int hpCost(Fighter self, Moves.Move m) {
        return switch (m.name()) {
            case "Whirlwind Slash" -> (int) (self.hp * 0.05);
            case "Berserker's Fury" -> (int) (self.maxHp * 0.10);
            case "Mana Burst" -> (int) (self.maxHp * 0.08);
            case "Meteor Storm", "Time Fracture" -> (int) (self.maxHp * 0.12);
            default -> 0;
        };
    }

    // ------------------------------------------------------------------ resolving actions

    private void perform(Fighter user, Fighter target, Action action, boolean extraTurn) {
        if (action.potion()) {
            System.out.println("\n" + user.p.name + " drinks a Health Potion!");
            user.p.potions--;
            user.potionsUsed++;
            user.heal(POTION_HEAL);
            user.lastWasSupport = true;
            return;
        }

        Moves.Move m = action.move();
        double boost = 1.0;
        if (action.signature()) {
            user.signatureUsed = true;
            boost = SIGNATURE_BOOST;
            System.out.println("\n*** " + user.p.name + " unleashes their SIGNATURE: " + m.name() + "! ***");
        } else {
            System.out.println("\n" + user.p.name + " uses " + m.name() + "!");
            user.payHp(hpCost(user, m));
        }
        int bonusTurns = boost > 1 ? 1 : 0;

        switch (m.name()) {
            case "Shield Bash" -> {
                if (hit(user, target, m, shieldBashPower(user) * boost, false, false) && rng.nextDouble() < 0.5) {
                    target.stunned = 1;
                    System.out.println("  " + target.p.name + " is STUNNED!");
                }
            }
            case "Execute" -> hit(user, target, m, (target.hpPct() < 0.3 ? 2.5 : m.power()) * boost, false, false);
            case "Rending Cleave" -> {
                if (hit(user, target, m, m.power() * boost, false, false)) {
                    target.addMod(Stat.DEF, -0.2, 2 + bonusTurns);
                    System.out.println("  " + target.p.name + "'s defense is torn open!");
                }
            }
            case "Counter Stance" -> {
                user.reflectRounds = 2 + bonusTurns;
                System.out.println("  " + user.p.name + " braces to counter the next blow.");
            }
            case "War Cry" -> buff(user, Stat.ATK, 0.25, 3 + bonusTurns, "attack rises");
            case "Adrenaline Surge" -> {
                buff(user, Stat.SPD, 0.3, 2 + bonusTurns, "speed surges");
                user.heal(0.08 * boost);
            }
            case "Last Stand" -> {
                if (user.hpPct() < 0.25) {
                    System.out.println("  Back against the wall, " + user.p.name + " fights with everything left!");
                    hit(user, target, m, 3.0 * boost, false, false);
                    user.immuneNext = true;
                } else {
                    hit(user, target, m, 1.0 * boost, false, false);
                }
            }
            case "Fireball" -> {
                hit(user, target, m, m.power() * boost, false, false);
                if (rng.nextDouble() < 0.5) {
                    System.out.println("  The flames chain back for another hit!");
                    hit(user, target, m, 0.4 * boost, false, false);
                }
            }
            case "Arcane Seal" -> {
                target.addMod(Stat.ATK, -0.3, 3 + bonusTurns);
                target.addMod(Stat.MATK, -0.3, 3 + bonusTurns);
                System.out.println("  Arcane runes seal " + target.p.name + "'s power! (attack & magic attack -30%)");
            }
            case "Frost Bolt" -> {
                if (hit(user, target, m, m.power() * boost, false, false)) {
                    target.addMod(Stat.SPD, -0.25, 2 + bonusTurns);
                    System.out.println("  " + target.p.name + " is slowed by frost!");
                }
            }
            case "Arcane Missile", "Smite" -> hit(user, target, m, m.power() * boost, false, true);
            case "Chain Lightning" -> {
                hit(user, target, m, m.power() * boost, false, false);
                if (rng.nextDouble() < 0.3) {
                    System.out.println("  Lightning arcs again!");
                    hit(user, target, m, 0.5 * boost, false, false);
                }
            }
            case "Mana Focus" -> buff(user, Stat.MATK, 0.25, 3 + bonusTurns, "magic attack rises");
            case "Ward of Insight" -> {
                buff(user, Stat.DEF, 0.3, 3 + bonusTurns, "defense rises");
                buff(user, Stat.MDEF, 0.3, 3 + bonusTurns, "magic defense rises");
            }
            case "Time Fracture" -> {
                if (extraTurn) {
                    System.out.println("  Time refuses to bend twice in a row...");
                } else {
                    user.extraTurn = true;
                }
            }
            case "Assassinate" -> {
                if (user.actedFirst) {
                    hit(user, target, m, 1.4 * boost, true, false);
                } else {
                    System.out.println("  " + target.p.name + " saw it coming - no assassination bonus.");
                    hit(user, target, m, m.power() * boost, false, false);
                }
            }
            case "Evasion Stance" -> {
                user.dodgeNext = true;
                buff(user, Stat.SPD, 0.3, 2 + bonusTurns, "speed rises");
                System.out.println("  " + user.p.name + " will dodge the next attack.");
            }
            case "Poison Dart" -> {
                if (hit(user, target, m, m.power() * boost, false, false)) {
                    target.poisonTurns = 4 + bonusTurns;
                    target.poisonDamage = Math.max(1, (int) (target.maxHp * 0.05));
                    System.out.println("  " + target.p.name + " is POISONED!");
                }
            }
            case "Backstab" -> hit(user, target, m, (target.lastWasSupport ? 2.2 : m.power()) * boost, false, false);
            case "Throwing Knives" -> multiHit(user, target, m, 3, m.power() * boost);
            case "Shadow Step" -> buff(user, Stat.SPD, 0.4, 2 + bonusTurns, "speed rises sharply");
            case "Sharpen Blades" -> {
                buff(user, Stat.ATK, 0.2, 3 + bonusTurns, "attack rises");
                buff(user, Stat.CRIT, 0.15, 3 + bonusTurns, "critical chance rises");
            }
            case "Death Mark" -> {
                if (hit(user, target, m, m.power() * boost, false, false)) {
                    target.markHits = 3 + bonusTurns;
                    System.out.println("  " + target.p.name + " is MARKED for death!");
                }
            }
            case "Thousand Cuts" -> multiHit(user, target, m, 5, m.power() * boost);
            case "Divine Strike" -> {
                if (hit(user, target, m, m.power() * boost, false, false) && rng.nextDouble() < 0.5) {
                    user.heal(0.08 * boost);
                }
            }
            case "Blessing of Light" -> {
                user.heal(0.08 * boost);
                buff(user, Stat.DEF, 0.2, 3 + bonusTurns, "defense rises");
            }
            case "Judgment" -> hit(user, target, m, (target.hpPct() < 0.5 ? 1.8 : m.power()) * boost, false, false);
            case "Consecration" -> {
                hit(user, target, m, m.power() * boost, false, false);
                user.heal(0.06 * boost);
            }
            case "Holy Retribution" -> hit(user, target, m, (user.hpPct() < 0.4 ? 1.6 : 1.2) * boost, false, false);
            case "Sacred Vow" -> {
                buff(user, Stat.DEF, 0.25, 3 + bonusTurns, "defense rises");
                buff(user, Stat.MDEF, 0.25, 3 + bonusTurns, "magic defense rises");
            }
            case "Lay on Hands" -> user.heal(0.20 * boost);
            case "Wrath of Heaven" -> {
                hit(user, target, m, m.power() * boost, false, false);
                user.heal(0.06 * boost);
            }
            case "Guardian Angel" -> {
                if (user.guardianUsed) {
                    System.out.println("  The angel has already answered once this battle...");
                } else {
                    user.guardian = true;
                    System.out.println("  A guardian angel watches over " + user.p.name + ".");
                }
            }
            // Plain damage moves: Whirlwind Slash, Slash, Berserker's Fury, Mana Burst, Meteor Storm, Quick Strike
            default -> hit(user, target, m, m.power() * boost, false, false);
        }
        user.lastWasSupport = !m.dealsDamage();
    }

    private void buff(Fighter f, Stat s, double amount, int turns, String text) {
        f.addMod(s, amount, turns);
        System.out.println("  " + f.p.name + "'s " + text + "! (" + turns + " turns)");
    }

    private void multiHit(Fighter user, Fighter target, Moves.Move m, int hits, double power) {
        int landed = 0;
        for (int i = 0; i < hits && target.hp > 0; i++) {
            if (hit(user, target, m, power, false, false)) landed++;
        }
        System.out.println("  " + landed + " of " + hits + " hits landed.");
    }

    /** Resolves one hit. Returns true if it connected. */
    private boolean hit(Fighter user, Fighter target, Moves.Move m, double power, boolean forceCrit, boolean neverMiss) {
        if (target.hp <= 0) return false;
        if (!neverMiss && rng.nextDouble() < 0.05) {
            System.out.println("  The attack misses!");
            return false;
        }
        if (target.dodgeNext) {
            target.dodgeNext = false;
            System.out.println("  " + target.p.name + " gracefully dodges!");
            return false;
        }
        if (target.immuneNext) {
            target.immuneNext = false;
            System.out.println("  " + target.p.name + " shrugs off the blow, completely unharmed!");
            return false;
        }

        boolean magic = m.type() == Moves.Type.MAGIC;
        double damage = power * baseDamage(user.stat(magic ? Stat.MATK : Stat.ATK), target.stat(magic ? Stat.MDEF : Stat.DEF));
        damage *= 0.9 + rng.nextDouble() * 0.2;

        boolean crit = forceCrit || rng.nextDouble() < user.stat(Stat.CRIT);
        if (crit) damage *= 1.5;
        if (target.markHits > 0) {
            target.markHits--;
            damage *= 1.5;
        }
        int dmg = Math.max(1, (int) Math.round(damage));
        System.out.println("  " + (crit ? "CRITICAL HIT! " : "") + target.p.name + " takes " + dmg + " damage.");
        applyDamage(target, dmg);

        if (target.reflectRounds > 0 && target.hp > 0) {
            target.reflectRounds = 0;
            int reflected = Math.max(1, dmg / 2);
            System.out.println("  " + target.p.name + " counters! " + user.p.name + " takes " + reflected + " damage.");
            applyDamage(user, reflected);
        }
        return true;
    }

    private void applyDamage(Fighter f, int dmg) {
        f.hp -= dmg;
        if (f.hp <= 0 && f.guardian) {
            f.hp = 1;
            f.guardian = false;
            f.guardianUsed = true;
            System.out.println("  A GUARDIAN ANGEL saves " + f.p.name + " from defeat! (1 HP)");
        }
        f.hp = Math.max(0, f.hp);
    }

    private void endOfRound(Fighter... fighters) {
        for (Fighter f : fighters) {
            if (f.hp > 0 && f.poisonTurns > 0) {
                f.poisonTurns--;
                System.out.println("  Poison deals " + f.poisonDamage + " damage to " + f.p.name + ".");
                applyDamage(f, f.poisonDamage);
            }
            f.mods.removeIf(mod -> --mod.turns <= 0);
            if (f.reflectRounds > 0) f.reflectRounds--;
        }
    }

    // ------------------------------------------------------------------ display

    private void printStatus(Fighter f) {
        int width = 20;
        int filled = (int) Math.round(width * f.hpPct());
        String bar = "#".repeat(filled) + "-".repeat(width - filled);
        System.out.printf("%-18s [%s] %4d/%-4d %s%n", f.p.name, bar, f.hp, f.maxHp, statusTags(f));
    }

    private static String statusTags(Fighter f) {
        StringBuilder sb = new StringBuilder();
        for (Mod m : f.mods) {
            sb.append('[').append(m.stat).append(m.amount > 0 ? "+" : "-").append(' ').append(m.turns).append("t] ");
        }
        if (f.stunned > 0) sb.append("[STUN] ");
        if (f.poisonTurns > 0) sb.append("[POISON ").append(f.poisonTurns).append("t] ");
        if (f.dodgeNext) sb.append("[DODGE] ");
        if (f.immuneNext) sb.append("[IMMUNE] ");
        if (f.reflectRounds > 0) sb.append("[COUNTER] ");
        if (f.markHits > 0) sb.append("[MARKED x").append(f.markHits).append("] ");
        if (f.guardian) sb.append("[ANGEL] ");
        return sb.toString().trim();
    }
}
