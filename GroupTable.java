/**
- Stage 4: a table that seats a whole group up to its capacity
- One group per table: strangers never share, so the table counts as occupied as soon as any group (even with empty seats)
 */
public class GroupTable extends Table {
    private final int capacity;
    private Customer party;   // null when free

    public GroupTable(int tableNumber, int capacity) {
        super(tableNumber, capacity);
        if (capacity < 1) {
            throw new IllegalArgumentException("Table capacity must be at least 1.");
        }
        this.capacity = capacity;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public Customer getParty() {
        return this.party;
    }

    // a CustomerGroup needs one seat per member
    // a single customer needs one seat
    public static int seatsNeeded(Customer customer) {
        return (customer instanceof CustomerGroup) ? ((CustomerGroup) customer).getPartySize() : 1;
    }

    public boolean canSeat(Customer customer) {
        return customer != null && !isOccupied() && seatsNeeded(customer) <= this.capacity;
    }

    // seats the whole party, taking one seat per member
    @Override
    public void assignCustomer(Customer customer) {
        if (!canSeat(customer)) {
            System.out.println("Table " + getTableNumber() + " cannot seat that party.");
            return;
        }
        for (int i = 0; i < seatsNeeded(customer); i++) {
            super.assignCustomer(customer);
        }
        customer.setTableNumber(getTableNumber());   // also propagates to group members
        this.party = customer;
    }

    // the whole party leaves together, freeing every seat
    @Override
    public void removeCustomer() {
        if (this.party == null) {
            System.out.println("No one to remove from empty table!");
            return;
        }
        while (get_seatsRemaining() < this.capacity) {
            super.removeCustomer();
        }
        this.party = null;
    }

    @Override
    public boolean isOccupied() {
        return this.party != null;
    }
}
