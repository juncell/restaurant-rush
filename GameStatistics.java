import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

/** Revenue, served count, unhappy departures, payment records and the victory check. */
public class GameStatistics {
    private static final int MIN_PAID_CUSTOMERS = 4;
    private static final int MIN_REVENUE_CENTS = 4500;
    private static final int MIN_AVG_SATISFACTION = 60;

    private int revenueCents;
    private int servedCount;
    private int unhappyDepartures;
    private List<PaymentRecord> payments = new ArrayList<>();

    public void recordPayment(PaymentRecord record) {
        payments.add(record);
        revenueCents += record.getAmountCents();
    }

    public void recordServed() {
        servedCount++;
    }

    public void recordUnhappyDeparture() {
        unhappyDepartures++;
    }

    public int getRevenueCents() {
        return revenueCents;
    }

    public int getPaidCount() {
        return payments.size();
    }

    public int getServedCount() {
        return servedCount;
    }

    public int getUnhappyDepartures() {
        return unhappyDepartures;
    }

    /** Average satisfaction recorded at payment; empty when nobody has paid (show N/A). */
    public OptionalDouble getAverageSatisfaction() {
        return payments.stream().mapToInt(PaymentRecord::getSatisfaction).average();
    }

    /** Victory: at least 4 paid AND revenue at least $45.00 AND average satisfaction at least 60. */
    public boolean isVictory() {
        OptionalDouble average = getAverageSatisfaction();
        return getPaidCount() >= MIN_PAID_CUSTOMERS
                && revenueCents >= MIN_REVENUE_CENTS
                && average.isPresent()
                && average.getAsDouble() >= MIN_AVG_SATISFACTION;
    }
}
