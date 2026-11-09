
import java.util.List;

public class Host extends Staff {

    private static final int moraleBoost = 5;

    public Host(int ID) {
        super(ID);
    }

    // Normal seating for Stages 1–3
    public boolean seatCustomer(Customer customer, Table table) {
        if (hasActedThisTurn || customer == null || table == null || table.isOccupied()) {
            return false;
        }

        table.assignCustomer(customer);
        this.hasActedThisTurn = true;

        return true;
    }

    // Additional Stage 4 feature: seating with morale boost
    public boolean seatCustomerStage4(Customer customer, Table table) {
        boolean success = seatCustomer(customer, table);

        if (success) {
            customer.setSatisfaction(
                customer.getSatisfaction() + moraleBoost
            );

            System.out.println(
                "Customer #" + customer.getID()
                + " gained +" + moraleBoost + " satisfaction!"
            );
        }

        return success;
    }

    // Additional Stage 4 feature: suggest the customer with lowest satisfaction
    public Customer suggestNextCustomer(List<Customer> queue) {
        if (queue == null || queue.isEmpty()) {
            return null;
        }

        Customer mostUrgent = null;

        for (Customer current : queue) {
            if (current != null && (mostUrgent == null ||
                current.getSatisfaction() < mostUrgent.getSatisfaction())) {
                mostUrgent = current;
            }
        }

        return mostUrgent;
    }
}
