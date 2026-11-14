import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

/** Revenue, served count, unhappy departures, payment records and the victory check. */
public class GameStatistics {
    // victory targets (base setting: 4 paid customers, $45.00 revenue, average satisfaction 60)
    private int minPaidCustomers = 4;
    private int minRevenueCents = 4500;
    private int minAvgSatisfaction = 60;

    private int revenueCents;
    private int servedCount;
    private int unhappyDepartures;
    private List<PaymentRecord> payments = new ArrayList<>();

    public int getMinPaidCustomers() {
        return minPaidCustomers;
    }

    public void setMinPaidCustomers(int minPaidCustomers) {
        if (minPaidCustomers < 0) {
            throw new IllegalArgumentException("Minimum paid customers cannot be negative");
        }
        this.minPaidCustomers = minPaidCustomers;
    }

    public int getMinRevenueCents() {
        return minRevenueCents;
    }

    public void setMinRevenueCents(int minRevenueCents) {
        if (minRevenueCents < 0) {
            throw new IllegalArgumentException("Minimum revenue cannot be negative");
        }
        this.minRevenueCents = minRevenueCents;
    }

    public int getMinAvgSatisfaction() {
        return minAvgSatisfaction;
    }

    public void setMinAvgSatisfaction(int minAvgSatisfaction) {
        if (minAvgSatisfaction < 0 || minAvgSatisfaction > 100) {
            throw new IllegalArgumentException("Minimum average satisfaction must be 0 to 100");
        }
        this.minAvgSatisfaction = minAvgSatisfaction;
    }

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

    /** Victory: meets the paid-customer, revenue and average-satisfaction targets. */
    public boolean isVictory() {
        OptionalDouble average = getAverageSatisfaction();
        return getPaidCount() >= minPaidCustomers
                && revenueCents >= minRevenueCents
                && average.isPresent()
                && average.getAsDouble() >= minAvgSatisfaction;
    }
}
