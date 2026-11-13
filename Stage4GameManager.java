import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/*
- number of tables (2-4) and capacity of tables (2-6) are randomly assigned 
- CustomerGroup = mixed regular/vip/critic or just a homogenous group is randomly generated 
- 1 group sits at a table with enough seats 
- GroupOrder: combined order for all members in the table
 */
public class Stage4GameManager {
    private static final int MIN_TABLES = 2;
    private static final int MAX_TABLES = 4;
    private static final int MIN_CAPACITY = 2;
    private static final int MAX_CAPACITY = 6;

    private int maxTurns = 18;
    private int currentTurn;

    private final ConsoleUI ui;
    private final Random random;
    private final Restaurant restaurant;
    private final Waiter waiter;
    private final Chef chef;
    private final Cashier cashier;
    private final Stage4ArrivalSchedule schedule;
    private final GameStatistics stats;

    private final Map<Customer, Integer> servedTurn = new HashMap<>();
    private final Map<Customer, Integer> readyToPayTurn = new HashMap<>();

    /** @param seed the same seed always gives the same tables, parties and dish choices (reproducible runs) */
    public Stage4GameManager(ConsoleUI ui, long seed) {
        this.ui = ui;
        this.random = new Random(seed);

        // randomly assign the number of tables and each table's capacity
        int numberOfTables = MIN_TABLES + random.nextInt(MAX_TABLES - MIN_TABLES + 1);
        List<GroupTable> tables = new ArrayList<>();
        int largest = 0;
        for (int i = 1; i <= numberOfTables; i++) {
            int capacity = MIN_CAPACITY + random.nextInt(MAX_CAPACITY - MIN_CAPACITY + 1);
            tables.add(new GroupTable(i, capacity));
            largest = Math.max(largest, capacity);
        }
        this.restaurant = new Restaurant(tables, new Menu());

        this.waiter = new Waiter(1);
        this.chef = new Chef(2);
        this.cashier = new Cashier(3);
        this.schedule = new Stage4ArrivalSchedule(random, largest);   // every party fits the biggest table
        this.stats = new GameStatistics();
    }

    public void setMaxTurns(int maxTurns) {
        if (maxTurns < 1) {
            throw new IllegalArgumentException("Max turns must be at least 1");
        }
        this.maxTurns = maxTurns;
    }

    public void run() {
        ui.displayWelcome(restaurant.getMenu(), maxTurns);
        ui.showMessage("Stage 4: parties arrive together and share one table. Table layout tonight:");
        ui.displayTableLayout(restaurant);
        for (currentTurn = 1; currentTurn <= maxTurns; currentTurn++) {
            playTurn();
        }
        ui.displayFinalResult(stats.isVictory(), stats);
    }

    private void playTurn() {
        waiter.resetTurn();
        chef.resetTurn();
        cashier.resetTurn();
        for (Customer party : restaurant.getSeatedCustomers()) {
            if (party.getCurrentOrder() != null) {
                party.getCurrentOrder().clearTurnFlags();
            }
        }

        processArrival();                                   // Step 1
        ui.displayTurnStart(currentTurn, maxTurns, restaurant, stats);
        ui.displayTableLayout(restaurant);
        handleWaiterTask();                                 // Steps 2-3
        handleChefTask();
        processEatingAndPayment();                          // Step 4
        processWaitingAndAbandonment();                     // Step 5
        ui.displayTurnEnd(currentTurn, maxTurns, stats);
    }

    private void processArrival() {
        CustomerGroup party = schedule.getArrival(currentTurn);
        if (party != null) {
            restaurant.enqueue(party);
            ui.showMessage("Party G" + party.getID() + " of " + party.getPartySize()
                    + " arrives and joins the waiting queue.");
        }
    }

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
        Customer party = restaurant.findCustomer(ui.promptCustomerId("Seat which party?"));
        if (party == null || !restaurant.getWaitingQueue().contains(party)) {
            return "That party is not in the waiting queue.";
        }
        int tableNumber = ui.promptTableNumber();
        Table table = restaurant.findTable(tableNumber);
        if (table == null) {
            return "There is no table " + tableNumber + ".";
        }
        if (table.isOccupied()) {
            return "Table " + tableNumber + " is occupied.";
        }
        GroupTable groupTable = (GroupTable) table;
        if (!groupTable.canSeat(party)) {
            return "Table " + tableNumber + " only seats " + groupTable.getCapacity()
                    + " but the party has " + GroupTable.seatsNeeded(party) + ".";
        }
        if (!waiter.seatCustomer(party, table)) {   // GroupTable.assignCustomer seats every member
            return "The waiter could not seat that party.";
        }
        restaurant.seat(party, table);
        ui.showMessage("Waiter seats party G" + party.getID() + " at table " + tableNumber + ".");
        return null;
    }

    private String tryTakeOrder() {
        Customer customer = restaurant.findCustomer(ui.promptCustomerId("Take whose order? (party)"));
        if (!(customer instanceof CustomerGroup) || restaurant.getTableOf(customer) == null) {
            return "That party is not seated.";
        }
        if (customer.getCurrentOrder() != null) {
            return "That party has already ordered.";
        }
        if (waiter.hasActed()) {
            return "The waiter has already acted this turn.";
        }
        CustomerGroup party = (CustomerGroup) customer;
        List<MenuItem> choices = new ArrayList<>();
        for (Customer member : party.getMembers()) {
            choices.add(chooseItemFor(member));
        }
        GroupOrder order = new GroupOrder(party, choices, null);   // no Happy Hour in Stage 4 base run
        waiter.setActed(true);

        ui.showMessage("Party G" + party.getID() + " places a group order (" + order.getRemainingPrepUnits()
                + " prep units, bill " + String.format("$%.2f", order.getLockedPriceCents() / 100.0) + "):");
        for (Order line : order.getLineOrders()) {
            ui.showMessage("   member " + line.getCustomer().getID() + " orders " + line.getMenuItem().getName()
                    + String.format(" ($%.2f)", line.getLockedPriceCents() / 100.0));
        }
        return null;
    }

    private MenuItem chooseItemFor(Customer member) {
        Menu menu = restaurant.getMenu();
        if (member instanceof CriticCustomer) {
            String preferred = ((CriticCustomer) member).getPreferredDish();
            for (int i = 0; i < menu.getItemCount(); i++) {
                if (menu.getItem(i).getName().equals(preferred)) {
                    return menu.getItem(i);
                }
            }
        }
        return menu.getItem(random.nextInt(menu.getItemCount()));
    }

    private String tryServe() {
        Customer party = restaurant.findCustomer(ui.promptCustomerId("Serve which party?"));
        if (party == null || party.getCurrentOrder() == null) {
            return "That party has no order.";
        }
        Order order = party.getCurrentOrder();
        if (!order.canBeServed()) {
            return "That order is not READY yet (a dish finished this turn is served next turn).";
        }
        if (!waiter.serveOrder(order)) {
            return "The waiter could not serve that order.";
        }
        servedTurn.put(party, currentTurn);
        stats.recordServed();
        ui.showMessage("Waiter serves party G" + party.getID() + ". Order is SERVED.");
        return null;
    }

    private void handleChefTask() {
        while (true) {
            int partyId = ui.promptChefTarget();
            if (partyId == 0) {
                ui.showMessage("Chef waits.");
                return;
            }
            Customer party = restaurant.findCustomer(partyId);
            Order order = (party == null) ? null : party.getCurrentOrder();
            if (order == null) {
                ui.showError("That party has no order to prepare.");
            } else if (order.getStatus() != Order.Status.PLACED) {
                ui.showError("That order is already " + order.getStatus() + ".");
            } else if (chef.prepOrder(order)) {
                if (order.getStatus() == Order.Status.READY) {
                    ui.showMessage("Chef finishes party G" + partyId + "'s order. Order is READY.");
                } else {
                    ui.showMessage("Chef works on party G" + partyId + "'s order ("
                            + order.getRemainingPrepUnits() + " unit(s) left).");
                }
                return;
            } else {
                ui.showError("The chef could not prepare that order.");
            }
        }
    }

    private void processEatingAndPayment() {
        for (Customer party : restaurant.getSeatedCustomers()) {
            Integer served = servedTurn.get(party);
            if (served != null && served < currentTurn && !party.isReadyToPay()) {
                party.setReadyToPay(true);
                readyToPayTurn.put(party, currentTurn);
            }
        }

        Customer payer = null;
        for (Customer party : restaurant.getSeatedCustomers()) {
            Integer ready = readyToPayTurn.get(party);
            if (party.isReadyToPay() && ready != null && ready < currentTurn
                    && (payer == null || party.getID() < payer.getID())) {
                payer = party;
            }
        }
        if (payer == null) {
            return;
        }
        Order order = payer.getCurrentOrder();
        if (cashier.processPayment(order)) {
            stats.recordPayment(new PaymentRecord(payer.getID(), order.getLockedPriceCents(),
                    payer.getSatisfaction(), currentTurn));
            restaurant.release(payer);   // GroupTable frees every seat
            ui.showMessage("Cashier collects the group bill from party G" + payer.getID()
                    + ". Satisfaction " + payer.getSatisfaction() + " is recorded. Table freed.");
        }
    }

    private void processWaitingAndAbandonment() {
        List<Customer> present = new ArrayList<>(restaurant.getWaitingQueue());
        present.addAll(restaurant.getSeatedCustomers());
        for (Customer party : present) {
            party.satisfactionLoss();   // each member loses by their own type
            if (party.hasAbandoned()) {
                if (party.getCurrentOrder() != null) {
                    party.getCurrentOrder().cancel();
                }
                restaurant.release(party);
                stats.recordUnhappyDeparture();
                ui.showMessage("Party G" + party.getID() + " runs out of patience and leaves together.");
            }
        }
    }
}
