import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class GameManager {
    private static final int MAX_TURNS = 18;

    private final Scanner scanner;
    private final List<TableState> tables;
    private final List<Customer> waitingQueue;
    private final List<Customer> paidCustomers;
    private final List<String> paidNames;
    private final List<Order> allOrders;
    private final Map<Customer, String> customerNames;

    private final Waiter waiter;
    private final Chef chef;
    private final Host host;
    private final Cashier cashier;
    private final HappyHour happyHour;

    private final MenuItem salad = new MenuItem("Salad", 900, 1);
    private final MenuItem burger = new MenuItem("Burger", 1500, 1);
    private final MenuItem pasta = new MenuItem("Pasta", 1800, 2);
    private final ComboMeal saladPastaCombo;

    private int currentTurn = 1;
    private int revenueCents = 0;
    private int unhappyDepartures = 0;
    private int servedCount = 0;
    private int nextCustomerNumber = 1;
    private int nextArrivalSequence = 1;

    public GameManager() {
        this(new Scanner(System.in));
    }

    GameManager(Scanner scanner) {
        this.scanner = scanner;
        this.tables = new ArrayList<>();
        this.waitingQueue = new ArrayList<>();
        this.paidCustomers = new ArrayList<>();
        this.paidNames = new ArrayList<>();
        this.allOrders = new ArrayList<>();
        this.customerNames = new IdentityHashMap<>();


        tables.add(new TableState(new ManagedTable(1)));
        tables.add(new TableState(new ManagedTable(2)));

        waiter = new Waiter("Waiter");
        chef = new Chef("Chef");
        host = new Host("Host");
        cashier = new Cashier("Cashier");
        happyHour = new HappyHour();
        saladPastaCombo = new ComboMeal(salad, pasta);
    }

    public void startGame() {
        printWelcome();
        while (currentTurn <= MAX_TURNS) {
            runTurn();
            currentTurn++;
        }
        evaluateEndGame();
    }

    private void runTurn() {
        resetStaffForTurn();
        clearOrderTurnFlags();
        printStartOfTurn();

        processArrivals();
        activateHappyHourIfChosen();
        applyHappyHourRecovery();
        processHostChoice();
        processWaiterChoice();
        processChefChoice();
        processEatingAndPayment();
        processWaitingAndAbandonment();
        happyHour.advanceTurn();
        printEndOfTurnSummary();
    }

    private void resetStaffForTurn() {
        waiter.resetTurn();
        chef.resetTurn();
        host.resetTurn();
        cashier.resetTurn();
    }

    private void clearOrderTurnFlags() {
        for (Order order : allOrders) {
            order.clearTurnFlags();
        }
    }

    private void processArrivals() {
        if (currentTurn == 1 || currentTurn == 4 || currentTurn == 7
                || currentTurn == 10 || currentTurn == 13 || currentTurn == 16) {
            Customer customer;
            String name;
            if (currentTurn == 7) {
                customer = new VIPCustomer();
                name = "VIP-Alice";
            } else if (currentTurn == 10) {
                customer = new CriticCustomer();
                name = "Critic-Bob";
            } else {
                customer = new Customer();
                name = "Customer-" + nextCustomerNumber;
            }
            nextCustomerNumber++;
            customerNames.put(customer, name);
            waitingQueue.add(customer);
            System.out.println("[ARRIVAL] " + name + " arrived with satisfaction 100.");
        }
    }

    private void activateHappyHourIfChosen() {
        if (!happyHour.isUsedOnce() && currentTurn < MAX_TURNS) {
            System.out.print("Activate Happy Hour now? It lasts this turn and next turn (y/n): ");
            if (readYesNo()) {
                if (happyHour.activate()) {
                    System.out.println("[HAPPY HOUR] Activated for turns " + currentTurn
                            + " and " + (currentTurn + 1) + ".");
                }
            }
        }
    }

    private void applyHappyHourRecovery() {
        if (!happyHour.isActive()) return;
        for (Customer customer : waitingQueue) {
            customer.applyHappyHourRecovery();
        }
        for (TableState state : tables) {
            if (state.customer != null && !state.customer.isServed()) {
                state.customer.applyHappyHourRecovery();
            }
        }
    }

    private void processHostChoice() {
        if (waitingQueue.isEmpty()) return;
        TableState freeTable = firstFreeTable();
        if (freeTable == null) return;

        System.out.print("Host: seat " + nameOf(waitingQueue.get(0))
                + " at Table " + freeTable.table.getTableNumberForDisplay() + "? (y/n): ");
        if (!readYesNo()) return;

        Customer customer = waitingQueue.get(0);
        if (host.seatCustomer(customer, freeTable.table)) {
            waitingQueue.remove(0);
            freeTable.customer = customer;
            freeTable.arrivalSequence = nextArrivalSequence++;
            System.out.println("[HOST] Seated " + nameOf(customer) + ".");
        }
    }

    private void processWaiterChoice() {
        while (!waiter.hasActed()) {
            System.out.println("\nWaiter tasks (one task maximum):");
            System.out.println("1. Seat a waiting customer");
            System.out.println("2. Take an order from a seated customer");
            System.out.println("3. Serve an order that was READY before this turn");
            System.out.println("0. Wait");
            System.out.print("Choice: ");
            int choice = readInt();

            if (choice == 0) return;
            if (choice == 1) {
                TableState free = firstFreeTable();
                if (free == null || waitingQueue.isEmpty()) {
                    System.out.println("[INVALID] Seating requires both a free table and a waiting customer.");
                    continue;
                }
                Customer customer = waitingQueue.get(0);
                if (waiter.seatCustomer(customer, free.table)) {
                    waitingQueue.remove(0);
                    free.customer = customer;
                    free.arrivalSequence = nextArrivalSequence++;
                    System.out.println("[WAITER] Seated " + nameOf(customer) + ".");
                }
                return;
            }
            if (choice == 2) {
                TableState state = firstSeatedWithoutOrder();
                if (state == null) {
                    System.out.println("[INVALID] No seated customer is waiting to order.");
                    continue;
                }
                MenuItem chosenItem = customerChoosesItem(state.customer);
                Order order = waiter.takeOrder(state.customer, chosenItem, happyHour);
                if (order != null) {
                    state.order = order;
                    allOrders.add(order);
                    System.out.println("[WAITER] " + nameOf(state.customer) + " chose "
                            + chosenItem.getName() + ". Locked price: $"
                            + formatMoney(order.getLockedPriceCents()) + ".");
                }
                return;
            }
            if (choice == 3) {
                TableState ready = firstOrderReadyToServe();
                if (ready == null) {
                    System.out.println("[INVALID] No order was READY before the Waiter's action this turn.");
                    continue;
                }
                if (waiter.serveOrder(ready.order)) {
                    ready.servedTurn = currentTurn;
                    servedCount++;
                    System.out.println("[WAITER] Served " + nameOf(ready.customer) + " at Table "
                            + ready.table.getTableNumberForDisplay() + ".");
                }
                return;
            }
            System.out.println("[INVALID] Enter 0, 1, 2 or 3. Your task has not been used.");
        }
    }

    private MenuItem customerChoosesItem(Customer customer) {
        int sequence = currentTurn + customer.getSatisfaction();
        if (customer instanceof VIPCustomer) return burger;
        if (customer instanceof CriticCustomer) return pasta;
        if (sequence % 4 == 0) return saladPastaCombo;
        int choice = Math.floorMod(sequence, 3);
        if (choice == 0) return salad;
        if (choice == 1) return burger;
        return pasta;
    }

    private void processChefChoice() {
        List<TableState> inProgress = new ArrayList<>();
        for (TableState state : tables) {
            if (state.order != null && state.order.getStatus() == Order.Status.PLACED) {
                inProgress.add(state);
            }
        }
        if (inProgress.isEmpty()) {
            System.out.println("[CHEF] No orders need preparation.");
            return;
        }

        System.out.println("\nChef: choose one order to advance by one preparation unit:");
        for (int i = 0; i < inProgress.size(); i++) {
            TableState state = inProgress.get(i);
            System.out.println((i + 1) + ". " + nameOf(state.customer) + " - "
                    + state.order.getMenuItem().getName() + " ("
                    + state.order.getRemainingPrepUnits() + " units remaining)");
        }
        System.out.print("Choice (0 to wait): ");
        int choice = readInt();
        if (choice == 0) {
            System.out.println("[CHEF] Waited.");
            return;
        }
        if (choice < 1 || choice > inProgress.size()) {
            System.out.println("[INVALID] Chef waited; enter a listed order number or 0.");
            return;
        }
        Order selected = inProgress.get(choice - 1).order;
        if (chef.prepOrder(selected)) {
            System.out.println("[CHEF] Advanced preparation. Remaining units: "
                    + selected.getRemainingPrepUnits() + "; status: " + selected.getStatus() + ".");
        }
    }

    private void processEatingAndPayment() {
        for (TableState state : tables) {
            if (state.customer != null && state.order != null
                    && state.order.getStatus() == Order.Status.SERVED
                    && state.servedTurn < currentTurn
                    && !state.customer.isReadyToPay()) {
                state.customer.setReadyToPay(true);
                state.readyToPayTurn = currentTurn;
                System.out.println("[EATING] " + nameOf(state.customer) + " is READY_TO_PAY.");
            }
        }

        TableState oldest = null;
        for (TableState state : tables) {
            if (state.customer != null && state.order != null
                    && state.customer.isReadyToPay()
                    && state.readyToPayTurn < currentTurn) {
                if (oldest == null || state.arrivalSequence < oldest.arrivalSequence) {
                    oldest = state;
                }
            }
        }
        if (oldest != null && cashier.collectPayment()) {
            int bill = oldest.order.getLockedPriceCents();
            revenueCents += bill;
            oldest.order.markPaid();
            paidCustomers.add(oldest.customer);
            paidNames.add(nameOf(oldest.customer));
            System.out.println("[CASHIER] Collected $" + formatMoney(bill) + " from "
                    + nameOf(oldest.customer) + ". Table freed.");
            oldest.customer = null;
            oldest.order = null;
            oldest.servedTurn = -1;
            oldest.readyToPayTurn = -1;
            oldest.arrivalSequence = -1;
            oldest.table.removeCustomer();
        }
    }

    private void processWaitingAndAbandonment() {
        List<Customer> queueDepartures = new ArrayList<>();
        for (Customer customer : waitingQueue) {
            customer.satisfactionLoss();
            if (customer.hasAbandoned()) queueDepartures.add(customer);
        }
        for (Customer customer : queueDepartures) {
            waitingQueue.remove(customer);
            unhappyDepartures++;
            System.out.println("[ABANDONMENT] " + nameOf(customer) + " left the waiting queue.");
        }

        for (TableState state : tables) {
            if (state.customer != null && !state.customer.isServed()) {
                state.customer.satisfactionLoss();
                if (state.customer.hasAbandoned()) {
                    System.out.println("[ABANDONMENT] " + nameOf(state.customer)
                            + " left Table " + state.table.getTableNumberForDisplay() + ".");
                    if (state.order != null) state.order.cancel();
                    state.customer = null;
                    state.order = null;
                    state.servedTurn = -1;
                    state.readyToPayTurn = -1;
                    state.arrivalSequence = -1;
                    state.table.removeCustomer();
                    unhappyDepartures++;
                }
            }
        }
    }

    private TableState firstFreeTable() {
        for (TableState state : tables) if (!state.table.isOccupied()) return state;
        return null;
    }

    private TableState firstSeatedWithoutOrder() {
        for (TableState state : tables) {
            if (state.customer != null && state.order == null) return state;
        }
        return null;
    }

    private TableState firstOrderReadyToServe() {
        for (TableState state : tables) {
            if (state.order != null && state.order.canBeServed()) return state;
        }
        return null;
    }

    private String nameOf(Customer customer) {
        String name = customerNames.get(customer);
        return name == null ? "Customer" : name;
    }

    private void printWelcome() {
        System.out.println("===============================================");
        System.out.println("          RESTAURANT RUSH - SC2002");
        System.out.println("===============================================");
    }

    private void printStartOfTurn() {
        System.out.println("\n--------------- TURN " + currentTurn + " / " + MAX_TURNS + " ---------------");
        System.out.println("Revenue: $" + formatMoney(revenueCents));
        System.out.println("Average satisfaction of paid customers: " + averageSatisfactionText());
        System.out.println("Waiting queue:");
        if (waitingQueue.isEmpty()) System.out.println("  (empty)");
        for (Customer customer : waitingQueue) {
            System.out.println("  " + nameOf(customer) + " - satisfaction " + customer.getSatisfaction());
        }
        System.out.println("Tables:");
        for (TableState state : tables) {
            if (state.customer == null) {
                System.out.println("  Table " + state.table.getTableNumberForDisplay() + ": FREE");
            } else {
                String orderText = state.order == null ? "NO ORDER" : state.order.getMenuItem().getName()
                        + " [" + state.order.getStatus() + "]";
                System.out.println("  Table " + state.table.getTableNumberForDisplay() + ": "
                        + nameOf(state.customer) + ", satisfaction " + state.customer.getSatisfaction()
                        + ", " + orderText);
            }
        }
        System.out.println("Staff available: Waiter (one task), Chef (one prep unit), Host (one seating), Cashier (one payment).");
        if (happyHour.isActive()) {
            System.out.println("[HAPPY HOUR ACTIVE] Remaining active turns: " + happyHour.getRemainingTurns());
        }
    }

    private void printEndOfTurnSummary() {
        System.out.println("\n--- END OF TURN " + currentTurn + " ---");
        System.out.println("Customers served: " + servedCount);
        System.out.println("Unhappy departures: " + unhappyDepartures);
        System.out.println("Revenue: $" + formatMoney(revenueCents));
        System.out.println("Average satisfaction of paid customers: " + averageSatisfactionText());
        System.out.println("Turns used: " + currentTurn + " / " + MAX_TURNS);
    }

    private void evaluateEndGame() {
        double average = averageSatisfaction();
        System.out.println("\n================ GAME OVER ================");
        System.out.println("Paid customers: " + paidCustomers.size() + " (target: at least 4)");
        System.out.println("Revenue: $" + formatMoney(revenueCents) + " (target: at least $45.00)");
        System.out.println("Average paid-customer satisfaction: " + averageSatisfactionText() + " (target: at least 60)");
        if (paidCustomers.size() >= 4 && revenueCents >= 4500 && average >= 60.0) {
            System.out.println("VICTORY! All three objectives were achieved.");
        } else {
            System.out.println("DEFEAT. One or more objectives were not achieved.");
        }
    }

    private double averageSatisfaction() {
        if (paidCustomers.isEmpty()) return 0.0;
        int total = 0;
        for (Customer customer : paidCustomers) total += customer.getSatisfaction();
        return (double) total / paidCustomers.size();
    }

    private String averageSatisfactionText() {
        if (paidCustomers.isEmpty()) return "N/A";
        return String.format("%.2f", averageSatisfaction());
    }

    private static String formatMoney(int cents) {
        return String.format("%.2f", cents / 100.0);
    }

    private int readInt() {
        while (true) {
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException ex) {
                System.out.print("[INVALID] Enter a whole number: ");
            }
        }
    }

    private boolean readYesNo() {
        while (true) {
            String answer = scanner.nextLine().trim();
            if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) return true;
            if (answer.equalsIgnoreCase("n") || answer.equalsIgnoreCase("no")) return false;
            System.out.print("Please enter y or n: ");
        }
    }

    private static final class ManagedTable extends Table {
        private boolean occupied;
        private final int displayNumber;

        ManagedTable(int number) {
            super(number, 1);
            this.displayNumber = number;
        }

        @Override
        public void assignCustomer(Customer customer) {
            if (customer != null && !occupied) {
                occupied = true;
                customer.setTableNumber(displayNumber);
            }
        }

        @Override
        public void removeCustomer() {
            occupied = false;
        }

        @Override
        public boolean isOccupied() {
            return occupied;
        }

        int getTableNumberForDisplay() {
            return displayNumber;
        }
    }

    private static final class TableState {
        private final ManagedTable table;
        private Customer customer;
        private Order order;
        private int servedTurn = -1;
        private int readyToPayTurn = -1;
        private int arrivalSequence = -1;

        TableState(ManagedTable table) {
            this.table = table;
        }
    }

    private static final class Cashier extends Staff {
        Cashier(String name) { super(name); }
        boolean collectPayment() {
            if (hasActedThisTurn) return false;
            hasActedThisTurn = true;
            return true;
        }
    }
}
