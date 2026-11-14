import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for Restaurant Rush (Stage 1: Regular customers, Waiter, Chef and Cashier).
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

    private final Map<Customer, Integer> servedTurn = new HashMap<>();
    private final Map<Customer, Integer> readyToPayTurn = new HashMap<>();

    public GameManager(ConsoleUI ui) {
        this(ui, 1);
    }

    /**
     * Creates a game controller for the given stage (only Stage 1 exists so far):
     *   Stage 1: Regular customers, 3 base menu items, Waiter/Chef/Cashier.
     */
    public GameManager(ConsoleUI ui, int stage) {
        if (stage != 1) {
            throw new IllegalArgumentException("Stage must be 1");
        }
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

    public void setMaxTurns(int maxTurns) {
        if (maxTurns < 1) {
            throw new IllegalArgumentException("Max turns must be at least 1");
        }
        this.maxTurns = maxTurns;
    }

    public void run() {
        ui.displayWelcome(restaurant.getMenu(), maxTurns);
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

        processArrival();                                   // Step 1: Arrivals
        ui.displayTurnStart(currentTurn, maxTurns, restaurant, stats);
        handleWaiterTask();                                 // Steps 2-3: Waiter acts first,
        handleChefTask();                                   //            then Chef
        processEatingAndPayment();                          // Step 4: Eating & Cashier
        processWaitingAndAbandonment();                     // Step 5: Satisfaction loss & abandonment
        ui.displayTurnEnd(currentTurn, maxTurns, stats);
    }

    /** Step 1: the scheduled customer joins the waiting queue with satisfaction 100. */
    private void processArrival() {
        Customer arrival = schedule.getArrival(currentTurn);
        if (arrival != null) {
            restaurant.enqueue(arrival);
            String type = arrival.getClass().getSimpleName().replace("Customer", "");
            if (type.isEmpty()) {
                type = "Regular";
            }
            ui.showMessage("Customer " + arrival.getID() + " (" + type + ") arrives and joins the waiting queue.");
        }
    }

    /** Steps 2-3 for the Waiter: at most one task, or Wait. */
    private void handleWaiterTask() {
        while (true) {
            WaiterAction action = ui.promptWaiterAction();
            String error;
            switch (action) {
                case SEAT_CUSTOMER:
                    error = trySeat();
                    break;
                case TAKE_ORDER:
                    error = tryTakeOrder();
                    break;
                case SERVE_ORDER:
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
        ui.showMessage("Waiter seats customer " + customer.getID() + " at table " + tableNumber + ".");
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
        Order order = waiter.takeOrder(customer, choice);
        if (order == null) {
            return "The waiter could not take that order.";
        }
        ui.showMessage("Customer " + customer.getID() + " orders " + choice.getName()
                + String.format(" ($%.2f).", order.getLockedPriceCents() / 100.0));
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
        ui.showMessage("Waiter serves customer " + customer.getID() + ". Order is SERVED.");
        return null;
    }

    /**
     * Dispatches food selection polymorphically to the customer object:
     * customer n orders menu item (n - 1) mod menu size.
     */
    private MenuItem chooseItemFor(Customer customer) {
        return customer.chooseDish(restaurant.getMenu());
    }

    private void handleChefTask() {
        while (true) {
            int customerId = ui.promptChefTarget();
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
                    && (payer == null || customer.getID() < payer.getID())) {
                payer = customer;
            }
        }
        if (payer == null) {
            return;
        }
        Order order = payer.getCurrentOrder();
        if (cashier.processPayment(order)) {
            stats.recordPayment(new PaymentRecord(payer.getID(), order.getLockedPriceCents(),
                    payer.getSatisfaction(), currentTurn));
            restaurant.release(payer);
            ui.showMessage("Cashier collects payment from customer " + payer.getID()
                    + ". Satisfaction " + payer.getSatisfaction() + " is recorded. Table freed.");
        }
    }

    private void processWaitingAndAbandonment() {
        List<Customer> present = new ArrayList<>(restaurant.getWaitingQueue());
        present.addAll(restaurant.getSeatedCustomers());
        for (Customer customer : present) {
            customer.satisfactionLoss();
            if (customer.hasAbandoned()) {
                if (customer.getCurrentOrder() != null) {
                    customer.getCurrentOrder().cancel();
                }
                restaurant.release(customer);
                stats.recordUnhappyDeparture();
                ui.showMessage("Customer " + customer.getID() + " runs out of patience and leaves.");
            }
        }
    }

    private void showResult() {
        ui.displayFinalResult(stats.isVictory(), stats);
    }
}
