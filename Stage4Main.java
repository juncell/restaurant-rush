import java.util.Scanner;

public class Stage4Main {
    public static void main(String[] args) {
        long seed = 42;
        if (args.length > 0) {
            try {
                seed = Long.parseLong(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid seed \"" + args[0] + "\", using 42.");
            }
        }
        Scanner scanner = new Scanner(System.in);
        new Stage4GameManager(new ConsoleUI(scanner), seed).run();
    }
}
