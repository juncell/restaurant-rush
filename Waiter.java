public class Waiter extends Staff {

    // constructor 
    public Waiter(int ID) {
        super(ID);
    }


    public boolean seatCustomer(Customer customer, Table table) {
        // if waiter already busy, no customer/table, table already occupied -> cannot seat 
        if (this.hasActedThisTurn || customer == null || table == null || table.isOccupied()) {
            return false;
        }
        table.assignCustomer(customer); 
        this.hasActedThisTurn = true;
        return true;
    }

    public Order takeOrder(Customer customer, MenuItem chosenItem, HappyHour happyHour) {
        // if busy in turn, no customer, no choice, no customer asking for order -> dont take order 
        if (this.hasActedThisTurn || customer == null || chosenItem == null || customer.getCurrentOrder() != null) {
            return null;
        }
        Order order = new Order(customer, chosenItem, happyHour);
        this.hasActedThisTurn = true;
        return order;
    }

    public boolean serveOrder(Order order) {
        // if busy, no order or order cannot be served yet -> dont serve order 
        if (this.hasActedThisTurn || order == null || !order.canBeServed()) {
            return false;
        }
        order.markServed(); 
        this.hasActedThisTurn = true; 
        return true;
    }
}