public class Chef extends Staff {

    public Chef(int ID) {
        super(ID);
    }

    public boolean prepOrder(Order order) {
        // if busy, no order, order hasnt been placed --> dont prep order 
        if (this.hasActedThisTurn || order == null || order.getStatus() != Order.Status.PLACED) {
            return false;
        }

        
        boolean prepped = order.advancePrep(); 
        if (prepped) {
            this.hasActedThisTurn = true;
        }
        return prepped;
    }
}