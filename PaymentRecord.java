/** One payment. Immutable: satisfaction is captured at the moment of payment. */
public class PaymentRecord {
    private final int customerId;
    private final int amountCents;
    private final int satisfaction;
    private final int turnPaid;

    public PaymentRecord(int customerId, int amountCents, int satisfaction, int turnPaid) {
        this.customerId = customerId;
        this.amountCents = amountCents;
        this.satisfaction = satisfaction;
        this.turnPaid = turnPaid;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getAmountCents() {
        return amountCents;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public int getTurnPaid() {
        return turnPaid;
    }
}
