import java.util.Random;

/*
- A randomly generated arrival comes at the start of turns 1, 4, 7, 10, 13 and 16
- Groups-only mode (original Stage 4): every arrival is a party of 1..maxPartySize (the largest table's capacity)
- Mixed mode: each arrival is either an individual customer (random type, may share a table with strangers)
  or a party of 2..maxPartySize (needs a table of its own)
- Individuals and parties share one ID counter, so every ID typed by the manager is unique
 */
public class Stage4ArrivalSchedule {
    private static final int[] ARRIVAL_TURNS = {1, 4, 7, 10, 13, 16};

    private final Random random;
    private final int maxPartySize;
    private final boolean allowIndividuals;
    private int nextId = 1;

    // groups-only mode (original Stage 4 behaviour)
    public Stage4ArrivalSchedule(Random random, int maxPartySize) {
        this(random, maxPartySize, false);
    }

    public Stage4ArrivalSchedule(Random random, int maxPartySize, boolean allowIndividuals) {
        this.random = random;
        this.maxPartySize = Math.max(1, maxPartySize);
        this.allowIndividuals = allowIndividuals;
    }

    /** Returns the individual or party arriving this turn, or null if nobody arrives. */
    public Customer getArrival(int turn) {
        for (int arrivalTurn : ARRIVAL_TURNS) {
            if (arrivalTurn == turn) {
                return createArrival();
            }
        }
        return null;
    }

    private Customer createArrival() {
        int id = nextId++;
        if (!allowIndividuals) {
            return CustomerGroup.generateRandomGroup(id, 1, maxPartySize, random);
        }
        // 50/50 individual or party (a party always has at least 2 people in mixed mode)
        if (maxPartySize < 2 || random.nextBoolean()) {
            return CustomerGroup.randomCustomer(id, random);
        }
        return CustomerGroup.generateRandomGroup(id, 2, maxPartySize, random);
    }
}
