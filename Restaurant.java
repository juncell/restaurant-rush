import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// holds the state of the restaurant floor: tables, waiting queue, who sits where, and the menu
public class Restaurant {
    private final List<Table> tables = new ArrayList<>();
    private final List<Customer> waitingQueue = new ArrayList<>();       // in arrival order
    private final Map<Customer, Table> seating = new LinkedHashMap<>();  // seated customer -> table
    private final Menu menu;

    // constructor: creates tables numbered 1..numberOfTables, each seating one customer
    public Restaurant(int numberOfTables, Menu menu) {
        for (int i = 1; i <= numberOfTables; i++) {
            this.tables.add(new Table(i, 1));
        }
        this.menu = menu;
    }

    // Stage 4 constructor: uses the given tables (e.g. GroupTables with random capacities), numbered 1..n
    public Restaurant(List<? extends Table> tables, Menu menu) {
        this.tables.addAll(tables);
        this.menu = menu;
    }

    // a new arrival joins the end of the waiting queue
    public void enqueue(Customer customer) {
        this.waitingQueue.add(customer);
    }

    // read-only views so other classes cannot change the restaurant's lists directly
    public List<Customer> getWaitingQueue() {
        return Collections.unmodifiableList(this.waitingQueue);
    }

    public List<Table> getTables() {
        return Collections.unmodifiableList(this.tables);
    }

    public Menu getMenu() {
        return this.menu;
    }

    // finds a customer in the queue or at a table; null if they are not in the restaurant
    public Customer findCustomer(int customerID) {
        for (Customer customer : this.waitingQueue) {
            if (customer.getID() == customerID) {
                return customer;
            }
        }
        for (Customer customer : this.seating.keySet()) {
            if (customer.getID() == customerID) {
                return customer;
            }
        }
        return null;
    }

    // returns the table with this number; null if there is no such table
    public Table findTable(int tableNumber) {
        if (tableNumber < 1 || tableNumber > this.tables.size()) {
            return null;
        }
        return this.tables.get(tableNumber - 1);
    }

    // records a seating after the Waiter (or Host) has seated the customer at the table
    public void seat(Customer customer, Table table) {
        this.waitingQueue.remove(customer);
        this.seating.put(customer, table);
    }

    // seated customers, in the order they were seated
    public List<Customer> getSeatedCustomers() {
        return new ArrayList<>(this.seating.keySet());
    }

    // the table a customer sits at; null if they are not seated
    public Table getTableOf(Customer customer) {
        return this.seating.get(customer);
    }

    // customer leaves (after paying or abandoning): free their table, or take them out of the queue
    public void release(Customer customer) {
        this.waitingQueue.remove(customer);
        Table table = this.seating.remove(customer);
        if (table != null) {
            table.removeCustomer(customer);   // only this customer's seat(s); strangers sharing the table stay
        }
    }
}
