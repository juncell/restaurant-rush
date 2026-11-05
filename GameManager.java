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
        Customer arrival = schedule.getArrival(currentTurn);
        if (arrival != null) {
            restaurant.enqueue(arrival);
            ui.showMessage("Customer " + arrival.getCustomerID() + " arrives and joins the waiting queue.");
        }
    }

    /**
     * Steps 2-3 for the Waiter: at most one task, or Wait.
     * An invalid choice explains why and asks again, without using up the Waiter's task.
     */
    private void handleWaiterTask() {
        while (true) {
            int action = ui.promptWaiterAction();   // 1 Seat, 2 Take order, 3 Serve, 4 Wait
            String error;
            switch (action) {
                case 1:
                    error = trySeat();
                    break;
                case 2:
                    error = tryTakeOrder();
                    break;
                case 3:
                    error = tryServe();
                    break;
                default:
                    ui.showMessage("Waiter waits.");
                    return;
            }
            if (error == null) {
                return;
            }
            ui.showError(error);
        }
    }

    private String trySeat() {
        Customer customer = restaurant.findCustomer(ui.promptCustomerId("Seat which customer?"));
        if (customer == null || !restaurant.getWaitingQueue().contains(customer)) {
            return "That customer is not in the waiting queue.";
        }
        int tableNumber = ui.promptTableNumber();
        Table table = restaurant.findTable(tableNumber);
        if (table == null) {
            return "There is no table " + tableNumber + ".";
        }
        if (table.isOccupied()) {
            return "Table " + tableNumber + " is occupied.";
        }
        if (!waiter.seatCustomer(customer, table)) {
            return "The waiter could not seat that customer.";
        }
        restaurant.seat(customer, table);
        ui.showMessage("Waiter seats customer " + customer.getCustomerID() + " at table " + tableNumber + ".");
        return null;
    }

    private String tryTakeOrder() {
        Customer customer = restaurant.findCustomer(ui.promptCustomerId("Take whose order?"));
        if (customer == null || restaurant.getTableOf(customer) == null) {
            return "That customer is not seated.";
        }
        if (customer.getCurrentOrder() != null) {
            return "That customer has already ordered.";
        }
        MenuItem choice = chooseItemFor(customer);
        Order order = waiter.takeOrder(customer, choice, null);   // null: no Happy Hour in Stage 1
        if (order == null) {
            return "The waiter could not take that order.";
        }
        ui.showMessage("Customer " + customer.getCustomerID() + " orders " + choice.getName() + ".");
        return null;
    }

    private String tryServe() {
        Customer customer = restaurant.findCustomer(ui.promptCustomerId("Serve whose order?"));
        if (customer == null || customer.getCurrentOrder() == null) {
            return "That customer has no order.";
        }
        Order order = customer.getCurrentOrder();
        if (!order.canBeServed()) {
            return "That order is not READY yet (a dish finished this turn is served next turn).";
        }
        if (!waiter.serveOrder(order)) {
            return "The waiter could not serve that order.";
        }
        servedTurn.put(customer, currentTurn);
        stats.recordServed();
        ui.showMessage("Waiter serves customer " + customer.getCustomerID() + ". Order is SERVED.");
        return null;
    }

    /**
     * Documented deterministic rule: customer n orders menu item (n - 1) mod menu size,
     * so Salad, Burger, Pasta, Salad, Burger, Pasta.
     */
    private MenuItem chooseItemFor(Customer customer) {
        Menu menu = restaurant.getMenu();
        return menu.getItem((customer.getCustomerID() - 1) % menu.size());
    }

    /**
     * Steps 2-3 for the Chef: advance one order by one preparation unit, or Wait.
     * Runs after the Waiter, so the Chef can work on an order taken this turn.
     */
    private void handleChefTask() {
        while (true) {
            int customerId = ui.promptChefTarget();   // 0 = Wait
            if (customerId == 0) {
                ui.showMessage("Chef waits.");
                return;
            }
            Customer customer = restaurant.findCustomer(customerId);
            Order order = (customer == null) ? null : customer.getCurrentOrder();
            if (order == null) {
                ui.showError("That customer has no order to prepare.");
            } else if (order.getStatus() != Order.Status.PLACED) {
                ui.showError("That order is already " + order.getStatus() + ".");
            } else if (chef.prepOrder(order)) {
                if (order.getStatus() == Order.Status.READY) {
                    ui.showMessage("Chef finishes customer " + customerId + "'s "
                            + order.getMenuItem().getName() + ". Order is READY.");
                } else {
                    ui.showMessage("Chef works on customer " + customerId + "'s "
                            + order.getMenuItem().getName() + " (" + order.getRemainingPrepUnits()
                            + " unit(s) left).");
                }
                return;
            } else {
                ui.showError("The chef could not prepare that order.");
            }
        }
    }

    /**
     * Step 4: a customer served on an earlier turn finishes eating and becomes ready to pay.
     * The Cashier then handles at most one customer who became ready to pay on an earlier turn,
     * oldest (lowest ID) first.
     */
    private void processEatingAndPayment() {
        for (Customer customer : restaurant.getSeatedCustomers()) {
            Integer served = servedTurn.get(customer);
            if (served != null && served < currentTurn && !customer.isReadyToPay()) {
                customer.setReadyToPay(true);
                readyToPayTurn.put(customer, currentTurn);
            }
        }

        Customer payer = null;
        for (Customer customer : restaurant.getSeatedCustomers()) {
            Integer ready = readyToPayTurn.get(customer);
            if (customer.isReadyToPay() && ready != null && ready < currentTurn
                    && (payer == null || customer.getCustomerID() < payer.getCustomerID())) {
                payer = customer;
            }
        }
        if (payer == null) {
            return;
        }
        Order order = payer.getCurrentOrder();
        if (cashier.processPayment(order)) {
            stats.recordPayment(new PaymentRecord(payer.getCustomerID(), order.getLockedPriceCents(),
                    payer.getSatisfaction(), currentTurn));
            restaurant.release(payer);
            ui.showMessage("Cashier collects payment from customer " + payer.getCustomerID()
                    + ". Satisfaction " + payer.getSatisfaction() + " is recorded. Table freed.");
        }
    }

    /**
     * Step 5: every waiting or seated customer who has not been served loses satisfaction.
     * A customer who reaches 0 leaves without paying: their order is cancelled and their table freed.
     */
    private void processWaitingAndAbandonment() {
        List<Customer> present = new ArrayList<>(restaurant.getWaitingQueue());
        present.addAll(restaurant.getSeatedCustomers());
        for (Customer customer : present) {
            customer.satisfactionLoss();   // does nothing once the customer is served
            if (customer.hasAbandoned()) {
                if (customer.getCurrentOrder() != null) {
                    customer.getCurrentOrder().cancel();
                }
                restaurant.release(customer);
                stats.recordUnhappyDeparture();
                ui.showMessage("Customer " + customer.getCustomerID() + " runs out of patience and leaves.");
            }
        }
    }

    /** Step 6: after turn 18, Victory only if all three targets are met. */
    private void showResult() {
        ui.displayFinalResult(stats.isVictory(), stats);
    }
}
