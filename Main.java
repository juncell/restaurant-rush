import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Entry point for Restaurant Rush.
 *
 * Ways to run (after compiling with: javac *.java):
 *
 *   java Main                          play interactively from the keyboard
 *   java Main test1.txt                play using the inputs in test1.txt (inputs are echoed)
 *   java Main --echo < test1.txt       same, but the script is piped in through standard input
 *   java Main --turns 6 test1.txt      shorter game for a focused test (default is 18 turns)
 *
 * Test script format (handled by ConsoleUI): one number or y/n per line, in the order
 * the game asks for them. Blank lines and lines starting with '#' are skipped, so scripts
 * can be commented. If the script runs out early, every remaining prompt chooses 0 (Wait).
 */
public class Main {

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
        GameManager game = new GameManager(ui);
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
        System.out.println("Usage: java Main [--echo] [--turns N] [testScript.txt]");
        System.out.println("  testScript.txt  read inputs from this file instead of the keyboard");
        System.out.println("  --echo          print each input line (use when piping a script into stdin)");
        System.out.println("  --turns N       play N turns instead of the default 18");
    }
}
