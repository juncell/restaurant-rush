public class Host extends Staff {

    public Host(int ID) {
        super(ID);
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


