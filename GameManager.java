import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for one game of Restaurant Rush (Stage 1).
 * Runs all 18 turns and calls the six turn steps in order:
 * 1 arrivals, 2-3 Waiter then Chef, 4 eating and payment, 5 waiting and abandonment, 6 result.
 */
public class GameManager {
    private int maxTurns = 18;   // base setting: 18 turns
    private static final int NUMBER_OF_TABLES = 2;

    private int currentTurn;
    private final ConsoleUI ui;
    private final Restaurant restaurant;
    private final Waiter waiter;
    private final Chef chef;
    private final Cashier cashier;
    private final ArrivalSchedule schedule;
    private final GameStatistics stats;

    // Turn stamps for the eating and payment delays (Customer only stores true/false flags)
    private final Map<Customer, Integer> servedTurn = new HashMap<>();
    private final Map<Customer, Integer> readyToPayTurn = new HashMap<>();

    public GameManager(ConsoleUI ui) {
        this.ui = ui;
        this.restaurant = new Restaurant(NUMBER_OF_TABLES, new Menu());
        this.waiter = new Waiter(1);
        this.chef = new Chef(2);
        this.cashier = new Cashier(3);
        this.schedule = new ArrivalSchedule();
        this.stats = new GameStatistics();
    }

    public int getMaxTurns() {
        return this.maxTurns;
    }

    /** Changes the number of turns; call before run(). Must be at least 1. */
    public void setMaxTurns(int maxTurns) {
        if (maxTurns < 1) {
            throw new IllegalArgumentException("Max turns must be at least 1");
        }
        this.maxTurns = maxTurns;
    }

    /** Plays all the turns (there is no early ending), then shows the result. */
    public void run() {
        for (currentTurn = 1; currentTurn <= maxTurns; currentTurn++) {
            playTurn();
        }
        showResult();
    }

    private void playTurn() {
        waiter.resetTurn();
        chef.resetTurn();
        cashier.resetTurn();
        for (Customer customer : restaurant.getSeatedCustomers()) {
            if (customer.getCurrentOrder() != null) {
                customer.getCurrentOrder().clearTurnFlags();
            }
        }

        processArrival();                                   // Step 1
        ui.displayTurnStart(currentTurn, restaurant, stats);
        handleWaiterTask();                                 // Steps 2-3: the Waiter acts first,
        handleChefTask();                                   //            then the Chef
        processEatingAndPayment();                          // Step 4
        processWaitingAndAbandonment();                     // Step 5
        ui.displayTurnEnd(currentTurn, stats);
    }

    /** Step 1: the scheduled customer joins the waiting queue with satisfaction 100. */
    private void processArrival() {
        Customer arrival =
