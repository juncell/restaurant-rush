import java.util.List;

public class Host extends Staff {
    // bonus constant for morale boost
    private static final int MORALE_BOOST = 5;

    // constructor matching Staff(int ID)
    public Host(int ID) {
        super(ID);
    }

    // host seating behaviour with additional feature: immediate morale boost
    public boolean seatCustomer(Customer customer, Table table) {
        if (hasActedThisTurn || customer == null || table == null || table.isOccupied()) {
            return false;
        }

        // assign customer to the table
        table.assignCustomer(customer);
        this.hasActedThisTurn = true;

        // additional feature: morale boost upon seating (setSatisfaction caps at 100)
        customer.setSatisfaction(customer.getSatisfaction() + MORALE_BOOST);
        System.out.println("Customer #" + customer.getID() + " was seated by Host #" + this.ID 
                           + " and gained +" + MORALE_BOOST + " satisfaction!");

        return true;
    }

    // additional feature: suggests the next queued customer to seat
    // prioritises the customer with the lowest satisfaction to prevent abandonment
    public Customer suggestNextCustomer(List<Customer> queue) {
        if (queue == null || queue.isEmpty()) {
            return null;
        }

        Customer mostUrgent = queue.get(0);

        for (int i = 1; i < queue.size(); i++) {
            Customer current = queue.get(i);
            if (current != null && current.getSatisfaction() < mostUrgent.getSatisfaction()) {
                mostUrgent = current;
            }
        }

        return mostUrgent;
    }
}
