import java.io.Console;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Scanner;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Hero passwords: salted PBKDF2 hashes in the database, never the password itself. */
public class Auth {

    public static final int MIN_LENGTH = 4;
    public static final int MAX_ATTEMPTS = 3;
    private static final int ITERATIONS = 120_000;
    private static final SecureRandom RANDOM = new SecureRandom();

    // ------------------------------------------------------------------ hashing

    /** Returns "pbkdf2$iterations$salt$hash" (Base64), ready to store. */
    public static String hash(String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return "pbkdf2$" + ITERATIONS + "$" + b64(salt) + "$" + b64(pbkdf2(password, salt, ITERATIONS));
    }

    public static boolean verify(String password, String stored) {
        if (password == null || stored == null) return false;
        String[] parts = stored.split("\\$");
        if (parts.length != 4 || !parts[0].equals("pbkdf2")) return false;
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            return MessageDigest.isEqual(expected, pbkdf2(password, salt, iterations));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(String password, byte[] salt, int iterations) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("PBKDF2 is unavailable in this JVM", e);
        }
    }

    private static String b64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    // ------------------------------------------------------------------ prompts

    /** Reads a password, hiding the typing when running in a real terminal. */
    public static String readPassword(Scanner scanner, String prompt) {
        Console console = System.console();
        if (console != null && isTerminal(console)) {
            char[] chars = console.readPassword(prompt);
            if (chars == null) throw new Input.InputClosedException();
            return new String(chars);
        }
        return Input.readLine(scanner, prompt);
    }

    /** Console.isTerminal() only exists on Java 22+; older JDKs only return a console for terminals. */
    private static boolean isTerminal(Console console) {
        try {
            return (Boolean) Console.class.getMethod("isTerminal").invoke(console);
        } catch (NoSuchMethodException e) {
            return true;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    /** Asks for a new password twice and returns its hash. */
    public static String createPassword(Scanner scanner, String heroName) {
        while (true) {
            String first = readPassword(scanner, "Choose a password for " + heroName + " (min " + MIN_LENGTH + " characters): ");
            if (first.length() < MIN_LENGTH) {
                System.out.println("  -> Password must be at least " + MIN_LENGTH + " characters.");
                continue;
            }
            String second = readPassword(scanner, "Confirm password: ");
            if (!first.equals(second)) {
                System.out.println("  -> Passwords don't match. Try again.");
                continue;
            }
            return hash(first);
        }
    }

    /**
     * Checks the hero's password (up to MAX_ATTEMPTS tries). Heroes saved before passwords
     * existed must set one now. Returns true if the player is allowed in.
     */
    public static boolean login(Scanner scanner, Player hero) {
        String stored = Database.getPasswordHash(hero.name);
        if (stored == null) {
            System.out.println("\n" + hero.name + " has no password yet. Set one to protect this hero.");
            Database.setPasswordHash(hero.name, createPassword(scanner, hero.name));
            System.out.println("Password set.");
            return true;
        }
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String entered = readPassword(scanner, "Password for " + hero.name + " (blank to cancel): ");
            if (entered.isEmpty()) return false;
            if (verify(entered, stored)) return true;
            int left = MAX_ATTEMPTS - attempt;
            System.out.println("  -> Wrong password." + (left > 0 ? " " + left + " attempt(s) left." : ""));
        }
        System.out.println("Too many wrong attempts.");
        return false;
    }

    public static void changePassword(Scanner scanner, Player hero) {
        String stored = Database.getPasswordHash(hero.name);
        if (stored != null && !verify(readPassword(scanner, "Current password: "), stored)) {
            System.out.println("  -> Wrong password. Nothing changed.");
            return;
        }
        Database.setPasswordHash(hero.name, createPassword(scanner, hero.name));
        System.out.println("Password changed.");
    }
}
