public class Host extends Staff {

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
}
