import java.util.Random;

/*
- A randomly generated party arrives at the start of turns 1, 4, 7, 10, 13 and 16
- Party size is 1..maxPartySize (the largest table's capacity)
 */
public class Stage4ArrivalSchedule {
    private static final int[] ARRIVAL_TURNS = {1, 4, 7, 10, 13, 16};

    private final Random random;
    private final int maxPartySize;
    private int nextGroupId = 1;

    public Stage4ArrivalSchedule(Random random, int maxPartySize) {
        this.random = random;
        this.maxPartySize = Math.max(1, maxPartySize);
    }

    /** Returns the party arriving this turn, or null if nobody arrives. */
    public CustomerGroup getArrival(int turn) {
        for (int arrivalTurn : ARRIVAL_TURNS) {
            if (arrivalTurn == turn) {
                return CustomerGroup.generateRandomGroup(nextGroupId++, 1, maxPartySize, random);
            }
        }
        return null;
    }
}
