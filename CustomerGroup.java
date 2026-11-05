import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// to implement group orders 
public class CustomerGroup extends Customer {
    private final List<Customer> members;  // array of customers 

    // constructor 
    public CustomerGroup(List<Customer> members) {
        super();

        // if no members in the group 
        if (members == null || members.isEmpty()) {
            throw new IllegalArgumentException("Customer Group must have at least 1 member.");
        }
        this.members = new ArrayList<>(members);
        this.partySize = this.members.size();
        syncGroupSatisfaction();
    }

    // 
    public static CustomerGroup generateRandomGroup(int minSize, int maxSize, Random random) {
        int size = random.nextInt((maxSize - minSize) + 1) + minSize;
        List<Customer> generated = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int roll = random.nextInt(3);
            if (roll == 0) {
                generated.add(new Customer());
            } else if (roll == 1) {
                generated.add(new VIPCustomer());
            } else {
                generated.add(new CriticCustomer());
            }
        }
        return new CustomerGroup(generated);
    }

    public List<Customer> getMembers() {
        return this.members;
    }

    @Override
    public int getPartySize() {
        return this.members.size();
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

    private void syncGroupSatisfaction() {
        int sum = 0;
        for (Customer member : this.members) {
            sum += member.getSatisfaction();
        }
        setSatisfaction(Math.round((float) sum / this.members.size()));
    }
}