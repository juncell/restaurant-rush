import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
- Stage 4: a table that seats a whole group up to its capacity
- One group per table: strangers never share with a group, so the table counts as occupied as soon as any group sits (even with empty seats)
- Individual customers (Stages 1-3 style) may share a table with other individuals until every seat is taken,
  but never with a group, and a group can only sit at a completely empty table
 */
public class GroupTable extends Table {
    private final int capacity;
    private Customer party;   // null when no group sits here
    private final List<Customer> individuals = new ArrayList<>();   // strangers sharing the table

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

    // read-only view of the individual customers sharing this table
    public List<Customer> getIndividuals() {
        return Collections.unmodifiableList(this.individuals);
    }

    // no group and no individuals at the table
    public boolean isEmpty() {
        return this.party == null && this.individuals.isEmpty();
    }

    // a CustomerGroup needs one seat per member
    // a single customer needs one seat
    public static int seatsNeeded(Customer customer) {
        return (customer instanceof CustomerGroup) ? ((CustomerGroup) customer).getPartySize() : 1;
    }

    // a group needs an empty table that is big enough
    // an individual needs a free seat at a table with no group
    public boolean canSeat(Customer customer) {
        if (customer == null) {
            return false;
        }
        if (customer instanceof CustomerGroup) {
            return isEmpty() && seatsNeeded(customer) <= this.capacity;
        }
        return this.party == null && get_seatsRemaining() > 0;
    }

    // seats the whole party (one seat per member), or one individual in one seat
    @Override
    public void assignCustomer(Customer customer) {
        if (!canSeat(customer)) {
            System.out.println("Table " + getTableNumber() + " cannot seat that customer.");
            return;
        }
        if (customer instanceof CustomerGroup) {
            for (int i = 0; i < seatsNeeded(customer); i++) {
                super.assignCustomer(customer);
            }
            customer.setTableNumber(getTableNumber());   // also propagates to group members
            this.party = customer;
        } else {
            super.assignCustomer(customer);
            this.individuals.add(customer);
        }
    }

    // a specific customer leaves: a group frees every seat, an individual frees only their own seat
    @Override
    public void removeCustomer(Customer customer) {
        if (customer != null && customer == this.party) {
            removeCustomer();
        } else if (this.individuals.remove(customer)) {
            super.removeCustomer();
        } else {
            System.out.println("That customer is not seated at table " + getTableNumber() + ".");
        }
    }

    // everyone at the table leaves together, freeing every seat
    @Override
    public void removeCustomer() {
        if (isEmpty()) {
            System.out.println("No one to remove from empty table!");
            return;
        }
        while (get_seatsRemaining() < this.capacity) {
            super.removeCustomer();
        }
        this.party = null;
        this.individuals.clear();
    }

    // occupied = nobody else can sit here: a group is seated, or every seat is taken by individuals
    @Override
    public boolean isOccupied() {
        return this.party != null || get_seatsRemaining() == 0;
    }
}
