import java.util.Scanner;

/**
 * Entry point for Stage 4.
 *
 *   java Stage4Main                    seed 42, individuals and parties both arrive (mixed)
 *   java Stage4Main 7                  seed 7
 *   java Stage4Main 7 --groups-only    original Stage 4: every arrival is a party
 */
public class Stage4Main {
    public static void main(String[] args) {
        long seed = 42;
        boolean allowIndividuals = true;
        for (String arg : args) {
            if (arg.equals("--groups-only")) {
                allowIndividuals = false;
                continue;
            }
            try {
                seed = Long.parseLong(arg);
            } catch (NumberFormatException e) {
                System.out.println("Invalid seed \"" + arg + "\", using 42.");
            }
        }
        Scanner scanner = new Scanner(System.in);
        new Stage4GameManager(new ConsoleUI(scanner), seed, allowIndividuals).run();
    }
}
