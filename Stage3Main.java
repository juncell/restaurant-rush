import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
Run this file to play Stage 3 (New role and menu policy).
Additional to stages 1 and 2: 
- Host: seats at most one queued customer per turn in addition to the Waiter;
- ComboMeal: two-item combos at 90% combined price and summed preparation units;
- Happy Hour: activated once per game at turn start (lasts 2 turns, 20% off new orders,
                 +15 satisfaction recovery for waiting/unserved customers).

Ways to run (after compiling with: javac *.java):
 *   java Stage3Main                                   : play interactively from the keyboard
 *   java Stage3Main tests/stage3_victory.txt          : play using the inputs in stage3_victory.txt
 *   java Stage3Main tests/stage3_defeat.txt           : play using the inputs in stage3_defeat.txt
 *   java Stage3Main --turns 3 tests/stage3_invalid_input.txt
 */
public class Stage3Main {

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
        GameManager game = new GameManager(ui, 3);
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
        System.out.println("Usage: java Stage3Main [--echo] [--turns N] [testScript.txt]");
        System.out.println("  testScript.txt  read inputs from this file instead of the keyboard");
        System.out.println("  --echo          print each input line (use when piping a script into stdin)");
        System.out.println("  --turns N       play N turns instead of the default 18");
    }
}
