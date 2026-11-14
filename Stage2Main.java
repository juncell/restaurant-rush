import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/*
Run this file to play Stage 2 (Customer behaviour extension).
Additional to stage 1: 
- VIPCustomer (loses 5 satisfaction/turn, 10% discount)
- CriticCustomer (loses 12 satisfaction/turn, always orders the most demanding dish).
- Base-setting arrivals: C1 Regular, C2 VIP, C3 Regular, C4 Critic, C5 Regular, C6 Regular.

Ways to run (after compiling with: javac *.java):
 *   java Stage2Main.java                        : play interactively from the keyboard
 *   java Stage2Main tests/stage2_victory.txt    : play using the inputs in stage2_victory.txt
 *   java Stage2Main tests/stage2_defeat.txt     : play using the inputs in stage2_defeat.txt
 *   java Stage2Main --turns 6 test.txt          : shorter game for a focused test (default is 18 turns)
 */
public class Stage2Main {

    public static void main(String[] args) {
        String scriptPath = null;
        boolean echoInput = false;
        Integer maxTurns = null;

        // Read the command-line options
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--echo")) {
                echoInput = true;
            } else if (arg.equals("--turns")) {
                if (i + 1 >= args.length) {
                    exitWithUsage("--turns needs a number after it.");
                }
                maxTurns = parseTurns(args[++i]);
            } else if (arg.equals("--help") || arg.equals("-h")) {
                printUsage();
                return;
            } else if (arg.startsWith("--")) {
                exitWithUsage("Unknown option: " + arg);
            } else if (scriptPath == null) {
                scriptPath = arg;
            } else {
                exitWithUsage("Only one test script can be given.");
            }
        }

        Scanner scanner;
        if (scriptPath != null) {
            try {
                scanner = new Scanner(new File(scriptPath));
            } catch (FileNotFoundException e) {
                System.err.println("Test script not found: " + scriptPath);
                System.exit(1);
                return;
            }
            echoInput = true;   // a transcript is only readable if it shows what was "typed"
            System.out.println("Running test script: " + scriptPath);
        } else {
            scanner = new Scanner(System.in);
        }

        ConsoleUI ui = new ConsoleUI(scanner, echoInput);
        GameManager game = new GameManager(ui, 2);
        if (maxTurns != null) {
            game.setMaxTurns(maxTurns);
        }

        try {
            game.run();
        } finally {
            scanner.close();
        }
    }

    private static int parseTurns(String value) {
        try {
            int turns = Integer.parseInt(value);
            if (turns < 1) {
                exitWithUsage("--turns must be at least 1.");
            }
            return turns;
        } catch (NumberFormatException e) {
            exitWithUsage("--turns needs a whole number, got \"" + value + "\".");
            return -1;   // not reached
        }
    }

    private static void exitWithUsage(String message) {
        System.err.println(message);
        printUsage();
        System.exit(1);
    }

    private static void printUsage() {
        System.out.println("Usage: java Stage2Main [--echo] [--turns N] [testScript.txt]");
        System.out.println("  testScript.txt  read inputs from this file instead of the keyboard");
        System.out.println("  --echo          print each input line (use when piping a script into stdin)");
        System.out.println("  --turns N       play N turns instead of the default 18");
    }
}
