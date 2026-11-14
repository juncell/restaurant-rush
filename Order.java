// the class that tracks every individual order 

public class Order {
    // defined states of an order that can exist 
    public enum Status {
        PLACED, READY, SERVED, PAID, CANCELLED 
    }

    // already determined, cannot change further 
    private final Customer customer;
    private final MenuItem menuItem;
    private final int lockedPriceCents;

    // order tracking set in this class 
    private int remainingPrepUnits;
    private Status status;
    private boolean finishedReadyThisTurn;

    // constructor class 
    public Order(Customer customer, MenuItem menuItem) {
        this.customer = customer;
        this.menuItem = menuItem;
        this.remainingPrepUnits = menuItem.getPrepUnits();
        this.lockedPriceCents = calculateLockedPriceCents(customer, menuItem);
        this.status = Status.PLACED;  // if an order obj is created, it must have been placed first 
        this.finishedReadyThisTurn = false;
        this.customer.setCurrentOrder(this);  
    }

    // to calculate the final price in cents so that we can return the right amount in change 
    private int calculateLockedPriceCents(Customer customer, MenuItem item) {
        // the customer's own discount (VIP 10% off)
        double price = item.getPrice() * (1.0 - customer.getDiscount());
        return (int) Math.round(price);
    }

    public boolean advancePrep() {
        // if order has not been placed or all the prep is done -> dont prep 
        if (this.status != Status.PLACED || this.remainingPrepUnits <= 0) {
            return false;
        }
        // prep 1 unit 
        this.remainingPrepUnits--;
        if (this.remainingPrepUnits == 0) {
            this.status = Status.READY; // if all prepped -> ready to be served 
            this.finishedReadyThisTurn = true;
            this.customer.setReadyToServe(true);
        }
        return true;
    }

    public void clearTurnFlags() {
        this.finishedReadyThisTurn = false;
    }

    public boolean canBeServed() {
        // if food was prepped in the last turn and is ready to be served -> can be served 
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
