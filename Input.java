import java.util.NoSuchElementException;
import java.util.Scanner;

/** Shared console input helpers so every menu validates input the same way. */
public class Input {

    /** Thrown when standard input is closed (e.g. Ctrl+Z / end of piped input). */
    public static class InputClosedException extends RuntimeException {}

    public static String readLine(Scanner scanner, String prompt) {
        System.out.print(prompt);
        try {
            // Strip a byte-order mark some shells prepend to piped input.
            return scanner.nextLine().replace("﻿", "").trim();
        } catch (NoSuchElementException e) {
            throw new InputClosedException();
        }
    }

    public static int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            String line = readLine(scanner, prompt);
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) return value;
                System.out.println("  -> Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  -> Invalid input! Please enter a number.");
            }
        }
    }

    /** Prints a numbered list and returns the chosen index (0-based). */
    public static int choose(Scanner scanner, String title, String[] options) {
        System.out.println("\n--- " + title + " ---");
        for (int i = 0; i < options.length; i++) {
            System.out.println("  " + (i + 1) + ". " + options[i]);
        }
        return readInt(scanner, "Enter choice (1-" + options.length + "): ", 1, options.length) - 1;
    }

    public static boolean confirm(Scanner scanner, String prompt) {
        while (true) {
            String answer = readLine(scanner, prompt + " (y/n): ").toLowerCase();
            if (answer.equals("y") || answer.equals("yes")) return true;
            if (answer.equals("n") || answer.equals("no")) return false;
        }
    }

    public static void pause(Scanner scanner) {
        readLine(scanner, "\n(press Enter to continue)");
    }
}
