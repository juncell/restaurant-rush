public class Order {
    public enum Status {
        PLACED, READY, SERVED, PAID, CANCELLED
    }

    private final Customer customer;
    private final MenuItem menuItem;
    private int remainingPrepUnits;
    private final int lockedPriceCents;
    private Status status;
    private boolean finishedReadyThisTurn;

    public Order(Customer customer, MenuItem menuItem, HappyHour happyHour) {
        this.customer = customer;
        this.menuItem = menuItem;
        this.remainingPrepUnits = menuItem.getPrepUnits();
        this.lockedPriceCents = calculateLockedPriceCents(customer, menuItem, happyHour);
        this.status = Status.PLACED;
        this.finishedReadyThisTurn = false;
        this.customer.setCurrentOrder(this);
    }

    private int calculateLockedPriceCents(Customer customer, MenuItem item, HappyHour happyHour) {
        double price = item.getPrice() * (1.0 - customer.getDiscount());
        if (happyHour != null && happyHour.isActive()) {
            price = price * (1.0 - happyHour.getDiscount());
        }
        return (int) Math.round(price);
    }

    public boolean advancePrep() {
        if (this.status != Status.PLACED || this.remainingPrepUnits <= 0) {
            return false;
        }
        this.remainingPrepUnits--;
        if (this.remainingPrepUnits == 0) {
            this.status = Status.READY;
            this.finishedReadyThisTurn = true;
            this.customer.setReadyToServe(true);
        }
        return true;
    }

    public void clearTurnFlags() {
        this.finishedReadyThisTurn = false;
    }

    public boolean canBeServed() {
        return this.status == Status.READY && !this.finishedReadyThisTurn;
    }

    public void markServed() {
        this.status = Status.SERVED;
        this.customer.setReadyToServe(false);
        this.customer.setServed(true);
    }

    public void markPaid() {
        this.status = Status.PAID;
    }

    public void cancel() {
        this.status = Status.CANCELLED;
        this.customer.setReadyToServe(false);
    }

    public Customer getCustomer() {
        return this.customer;
    }

    public MenuItem getMenuItem() {
        return this.menuItem;
    }

    public int getRemainingPrepUnits() {
        return this.remainingPrepUnits;
    }

    public int getLockedPriceCents() {
        return this.lockedPriceCents;
    }

    public Status getStatus() {
        return this.status;
    }
}