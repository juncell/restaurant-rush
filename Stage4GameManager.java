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
- Mixed mode (default): arrivals are either individuals or groups
    - an individual follows the Stage 1-3 rules (1 seat, own order, own bill) and may share a table with strangers
    - a group still needs an empty table of its own; strangers never join a group's table
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
    private final boolean allowIndividuals;

    private final Map<Customer, Integer> servedTurn = new HashMap<>();
    private final Map<Customer, Integer> readyToPayTurn = new HashMap<>();

    /** @param seed the same seed always gives the same tables, parties and dish choices (reproducible runs) */
    public Stage4GameManager(ConsoleUI ui, long seed) {
        this(ui, seed, true);
    }

    /** @param allowIndividuals true = individuals and groups arrive (mixed); false = groups only (original Stage 4) */
    public Stage4GameManager(ConsoleUI ui, long seed, boolean allowIndividuals) {
        this.ui = ui;
        this.random = new Random(seed);
        this.allowIndividuals = allowIndividuals;

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
        this.schedule = new Stage4ArrivalSchedule(random, largest, allowIndividuals);   // every party fits the biggest table
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
        if (allowIndividuals) {
            ui.showMessage("Stage 4: individuals (C) may share a table with strangers; parties (G) need a table"
                    + " of their own. Table layout tonight:");
        } else {
            ui.showMessage("Stage 4: parties arrive together and share one table. Table layout tonight:");
        }
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
        Customer arrival = schedule.getArrival(currentTurn);
        if (arrival == null) {
            return;
        }
        restaurant.enqueue(arrival);
        if (arrival instanceof CustomerGroup) {
            ui.showMessage("Party G" + arrival.getID() + " of " + ((CustomerGroup) arrival).getPartySize()
                    + " arrives and joins the waiting queue.");
        } else {
            ui.showMessage("Customer C" + arrival.getID() + " arrives alone and joins the waiting queue.");
        }
    }

    // "party G2" or "customer C3", for messages
    private static String describe(Customer customer) {
        return (customer instanceof CustomerGroup) ? "party G" + customer.getID() : "customer C" + customer.getID();
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
        Customer customer = restaurant.findCustomer(ui.promptCustomerId("Seat which customer/party?"));
        if (customer == null || !restaurant.getWaitingQueue().contains(customer)) {
            return "That customer/party is not in the waiting queue.";
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
        if (!groupTable.canSeat(customer)) {
            if (customer instanceof CustomerGroup && !groupTable.isEmpty()) {
                return "Table " + tableNumber + " is shared by other customers; a party needs a table of its own.";
            }
            return "Table " + tableNumber + " only seats " + groupTable.getCapacity()
                    + " but the party has " + GroupTable.seatsNeeded(customer) + ".";
        }
        if (!waiter.seatCustomer(customer, table)) {   // GroupTable.assignCustomer seats every member of a party
            return "The waiter could not seat that customer/party.";
        }
        restaurant.seat(customer, table);
        String sharing = (customer instanceof CustomerGroup || groupTable.getIndividuals().size() == 1)
                ? "" : " (sharing with other customers)";
        ui.showMessage("Waiter seats " + describe(customer) + " at table " + tableNumber + sharing + ".");
        return null;
    }

    private String tryTakeOrder() {
        Customer customer = restaurant.findCustomer(ui.promptCustomerId("Take whose order? (customer/party)"));
        if (customer == null || restaurant.getTableOf(customer) == null) {
            return "That customer/party is not seated.";
        }
        if (customer.getCurrentOrder() != null) {
            return "That customer/party has already ordered.";
        }
        if (waiter.hasActed()) {
            return "The waiter has already acted this turn.";
        }
        if (!(customer instanceof CustomerGroup)) {
            return takeIndividualOrder(customer);
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

    // an individual orders one dish for themselves, exactly as in Stages 1-3
    private String takeIndividualOrder(Customer customer) {
        MenuItem choice = chooseItemFor(customer);
        Order order = waiter.takeOrder(customer, choice, null);   // no Happy Hour in Stage 4 base run
        if (order == null) {
            return "The waiter could not take that order.";
        }
        ui.showMessage("Customer C" + customer.getID() + " orders " + choice.getName()
                + String.format(" ($%.2f)", order.getLockedPriceCents() / 100.0) + ".");
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
        Customer party = restaurant.findCustomer(ui.promptCustomerId("Serve which customer/party?"));
        if (party == null || party.getCurrentOrder() == null) {
            return "That customer/party has no order.";
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
        ui.showMessage("Waiter serves " + describe(party) + ". Order is SERVED.");
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
                ui.showError("That customer/party has no order to prepare.");
            } else if (order.getStatus() != Order.Status.PLACED) {
                ui.showError("That order is already " + order.getStatus() + ".");
            } else if (chef.prepOrder(order)) {
                if (order.getStatus() == Order.Status.READY) {
                    ui.showMessage("Chef finishes " + describe(party) + "'s order. Order is READY.");
                } else {
                    ui.showMessage("Chef works on " + describe(party) + "'s order ("
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
            restaurant.release(payer);   // a party frees every seat; an individual frees only their own seat
            String bill = (payer instanceof CustomerGroup) ? "the group bill" : "payment";
            String freed = (payer instanceof CustomerGroup) ? "Table freed." : "Seat freed.";
            ui.showMessage("Cashier collects " + bill + " from " + describe(payer)
                    + ". Satisfaction " + payer.getSatisfaction() + " is recorded. " + freed);
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
                if (party instanceof CustomerGroup) {
                    ui.showMessage("Party G" + party.getID() + " runs out of patience and leaves together.");
                } else {
                    ui.showMessage("Customer C" + party.getID() + " runs out of patience and leaves.");
                }
            }
        }
    }
}
