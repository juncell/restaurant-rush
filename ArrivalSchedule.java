/**
 * Creates one customer at the start of turns 1, 4, 7, 10, 13 and 16.
 * Stage 1: all 6 arrivals are Regular customers.
 */
public class ArrivalSchedule {
    private static final int[] ARRIVAL_TURNS = {1, 4, 7, 10, 13, 16};

    private int nextCustomerId = 1;

    /** Returns the customer arriving this turn, or null if nobody arrives. */
    public Customer getArrival(int turn) {
        for (int arrivalTurn : ARRIVAL_TURNS) {
            if (arrivalTurn == turn) {
                return new Customer(nextCustomerId++);
            }
        }
        return null;
    }
}
