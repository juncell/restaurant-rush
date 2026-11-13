import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Stage 4: a party of customers (e.g. a family) that arrives, sits, orders and pays together.
 *
 * A CustomerGroup IS-A Customer, so the existing Waiter, Chef, Cashier and Restaurant can handle it
 * polymorphically, and it HAS individual Customer members (Regular / VIP / Critic) that keep their
 * own satisfaction rules. The group's satisfaction is always the (rounded) average of its members.
 */
public class CustomerGroup extends Customer {
    private final List<Customer> members;

    // constructor: the group ID is what the manager types; members keep their own IDs
    public CustomerGroup(int groupID, List<Customer> members) {
        super(groupID);
        // a group needs at least one member
        if (members == null || members.isEmpty()) {
            throw new IllegalArgumentException("Customer Group must have at least 1 member.");
        }
        this.members = new ArrayList<>(members);
        syncGroupSatisfaction();
    }

    /**
     * Randomly generates a group of minSize..maxSize members of random types.
     * Member n of group g gets ID g*100 + n (e.g. group 3 -> members 301, 302, ...),
     * so member IDs never clash with group IDs.
     * Pass a seeded Random for reproducible runs.
     */
    public static CustomerGroup generateRandomGroup(int groupID, int minSize, int maxSize, Random random) {
        if (minSize < 1 || maxSize < minSize) {
            throw new IllegalArgumentException("Invalid group size range.");
        }
        int size = random.nextInt((maxSize - minSize) + 1) + minSize;
        List<Customer> generated = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            int memberID = groupID * 100 + i;
            int roll = random.nextInt(3);
            if (roll == 0) {
                generated.add(new Customer(memberID));
            } else if (roll == 1) {
                generated.add(new VIPCustomer(memberID));
            } else {
                generated.add(new CriticCustomer(memberID));
            }
        }
        return new CustomerGroup(groupID, generated);
    }

    // read-only view of the members
    public List<Customer> getMembers() {
        return Collections.unmodifiableList(this.members);
    }

    public int getPartySize() {
        return this.members.size();
    }

    // average member discount (for display only; GroupOrder prices each member individually)
    @Override
    public double getDiscount() {
        double sum = 0;
        for (Customer member : this.members) {
            sum += member.getDiscount();
        }
        return sum / this.members.size();
    }

    // changing the group's satisfaction (e.g. Host morale boost) shifts every member by the same amount
    @Override
    public void setSatisfaction(int satisfaction) {
        int delta = satisfaction - this.satisfaction;
        for (Customer member : this.members) {
            member.setSatisfaction(member.getSatisfaction() + delta);
        }
        syncGroupSatisfaction();
    }

    @Override
    public void setTableNumber(int tableNumber) {
        super.setTableNumber(tableNumber);
        for (Customer member : this.members) {
            member.setTableNumber(tableNumber);
        }
    }

    @Override
    public void setServed(boolean served) {
        super.setServed(served);
        for (Customer member : this.members) {
            member.setServed(served);
        }
    }

    @Override
    public void setReadyToServe(boolean readyToServe) {
        super.setReadyToServe(readyToServe);
        for (Customer member : this.members) {
            member.setReadyToServe(readyToServe);
        }
    }

    @Override
    public void setReadyToPay(boolean readyToPay) {
        super.setReadyToPay(readyToPay);
        for (Customer member : this.members) {
            member.setReadyToPay(readyToPay);
        }
    }

    // each member loses satisfaction by its own rule (Regular 8, VIP 5, Critic 12/15)
    @Override
    public void satisfactionLoss() {
        if (!this.served) {
            for (Customer member : this.members) {
                member.satisfactionLoss();
            }
            syncGroupSatisfaction();
        }
    }

    @Override
    public void applyHappyHourRecovery() {
        if (!this.served) {
            for (Customer member : this.members) {
                member.applyHappyHourRecovery();
            }
            syncGroupSatisfaction();
        }
    }

    // a family leaves together once their average satisfaction reaches 0
    @Override
    public boolean hasAbandoned() {
        return this.satisfaction == 0 && !this.served;
    }

    // group satisfaction = rounded average of members (bypasses the overridden setter)
    private void syncGroupSatisfaction() {
        int sum = 0;
        for (Customer member : this.members) {
            sum += member.getSatisfaction();
        }
        super.setSatisfaction(Math.round((float) sum / this.members.size()));
    }
}
