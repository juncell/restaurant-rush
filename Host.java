public class Host extends Staff {

    public Host(String name) {
        super(name);
    }

    public boolean seatCustomer(Customer customer, Table table) {
        if (hasActedThisTurn || customer == null || table == null || table.isOccupied()) {
            return false;
        }

        table.assignCustomer(customer);
        this.hasActedThisTurn = true;
        return true;
    }
}


