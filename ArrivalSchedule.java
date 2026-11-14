/**
 * Creates one customer at the start of turns 1, 4, 7, 10, 13 and 16.
 * Stage 1: all 6 arrivals are Regular customers.
 * Stages 2 & 3: C2 is a VIPCustomer and C4 is a CriticCustomer.
 */
public class ArrivalSchedule {
    private static final int[] ARRIVAL_TURNS = {1, 4, 7, 10, 13, 16};

    private final boolean includeSpecialCustomers;
    private int nextCustomerId = 1;

    public ArrivalSchedule() {
        this(false);
    }

    public ArrivalSchedule(boolean includeSpecialCustomers) {
        this.includeSpecialCustomers = includeSpecialCustomers;
    }

    /** Returns the customer arriving this turn, or null if nobody arrives. */
    public Customer getArrival(int turn) {
        for (int arrivalTurn : ARRIVAL_TURNS) {
            if (arrivalTurn == turn) {
                int id = nextCustomerId++;
                if (includeSpecialCustomers) {
                    if (id == 2) {
                        return new VIPCustomer(id);
                    }
                    if (id == 4) {
                        return new CriticCustomer(id);
                    }
                }
                return new Customer(id);
            }
        }
        return null;
    }
}
