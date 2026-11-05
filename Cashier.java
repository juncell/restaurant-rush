// the Cashier automatically handles at most one payment per turn
public class Cashier extends Staff {

    // constructor
    public Cashier(int ID) {
        super(ID);
    }

    public boolean processPayment(Order order) {
        // if already acted this turn, no order, or the order has not been served -> cannot take payment
        if (this.hasActedThisTurn || order == null || order.getStatus() != Order.Status.SERVED) {
            return false;
        }
        order.markPaid();
        this.hasActedThisTurn = true;
        return true;
    }
}
