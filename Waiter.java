public class Waiter extends Staff {

    public Waiter(String name) {
        super(name);
    }

    public boolean seatCustomer(Customer customer, Table table) {
        if (this.hasActedThisTurn || customer == null || table == null || table.isOccupied()) {
            return false;
        }
        table.assignCustomer(customer);
        this.hasActedThisTurn = true;
        return true;
    }

    public Order takeOrder(Customer customer, MenuItem chosenItem, HappyHour happyHour) {
        if (this.hasActedThisTurn || customer == null || chosenItem == null || customer.getCurrentOrder() != null) {
            return null;
        }
        Order order = new Order(customer, chosenItem, happyHour);
        this.hasActedThisTurn = true;
        return order;
    }

    public boolean serveOrder(Order order) {
        if (this.hasActedThisTurn || order == null || !order.canBeServed()) {
            return false;
        }
        order.markServed();
        this.hasActedThisTurn = true;
        return true;
    }
}