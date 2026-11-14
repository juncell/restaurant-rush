import java.util.List;

public class Host extends Staff {
    public static final int moraleBoost = 5;

    public Host(int ID) {
        super(ID);
    }

    // Standard Stage 3 seating: seats at most one queued customer per turn, in addition to Waiter
    public boolean seatCustomer(Customer customer, Table table) {
        if (hasActedThisTurn || customer == null || table == null || table.isOccupied()) {
            return false;
        }
        table.assignCustomer(customer);
        this.hasActedThisTurn = true;
        return true;
    }

    // Stage 4 extra feature: seats customer/party and grants +5 satisfaction
    public boolean seatCustomerWithMoraleBoost(Customer customer, Table table) {
        if (!seatCustomer(customer, table)) {
            return false;
        }
        customer.setSatisfaction(customer.getSatisfaction() + moraleBoost);
        return true;
    }

    // Stage 4 extra feature: suggests the queued customer with the lowest satisfaction
    public Customer suggestNextCustomer(List<Customer> queue) {
        if (queue == null || queue.isEmpty()) {
            return null;
        }
        Customer mostUrgent = null;
        for (Customer current : queue) {
            if (current != null && (mostUrgent == null
                    || current.getSatisfaction() < mostUrgent.getSatisfaction())) {
                mostUrgent = current;
            }
        }
        return mostUrgent;
    }
}
